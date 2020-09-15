package org.unicon.lex.write.quiz.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.unicon.pdfscanner.Glossary;
import net.unicon.pdfscanner.GlossaryCSVWriter;
import net.unicon.pdfscanner.GlossaryQuizCSVWriter;
import net.unicon.pdfscanner.GlossaryScraper;
import net.unicon.pdfscanner.PDFManager;
import net.unicon.pdfscanner.PDFStyledTextStripper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.services.external.CanvasService;
import org.unicon.lex.services.external.S3Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class WriteQuizService {
    public static final String NAME = "writeQuiz";
    public static final String DEFINITION_TYPE = "definition";
    public static final String MC_TYPE = "multiple-choice";
    final Logger log = LogManager.getLogger(getClass());

    CanvasService canvasService;
    AthenaService athenaService;
    S3Service s3Service;

    private ObjectMapper mapper = new ObjectMapper();
    private List<String> quizQuestions;
    private Map<String, String> slotNames = Stream.of(new Object[][] {
            { "subject", "subject" },
            { "quizName", "quiz name or chapter" },
            { "numQuest", "number of questions you want to include" },
            { "examSource", "source you want the questions to come from" },
            { "canvasFileName", "name of the source file in Canvas" }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));

    public WriteQuizService(CanvasService canvasService, AthenaService athenaService, S3Service s3Service) {
        this.canvasService = canvasService;
        this.athenaService = athenaService;
        this.s3Service = s3Service;
    }

    private Map<String, Object> getQuizMap(String quizStr) {
        Map<String, Object> quizJson = new HashMap<>();
        try {
            quizJson = mapper.readValue(quizStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
        log.error("returning quiz with choices: " + quizJson.get("choices"));
        return quizJson;
    }

    public void createTestBankFromCanvas(String fileName, String subject, String quizName) {
        File sourceFile = canvasService.downloadFileFromCanvas(fileName);
        String outputGlossaryFileName = subject + ".csv";
        File outputGlossaryFile = new File("/tmp/" + outputGlossaryFileName);
        String outputQuizFileName = subject + "-" + quizName + "-quiz.csv";
        File outputQuizFile = new File("/tmp/" + outputQuizFileName);

        // TODO: This currently assumes Glossary file type - should expand to additional types and/or put in validations
        PDFManager pdfManager = new PDFManager();
        String startCaptureText = "KEY TERMS";
        String endCaptureText = "CHAPTER SUMMARY";
        boolean addBold = true;
        boolean twoColumns = true;

        log.info("{}", pdfManager);
        try {
            PDFStyledTextStripper pdfStripper = new PDFStyledTextStripper();
            //sort by position maintains correct text ordering in the most recent openstax courses
            pdfStripper.setSortByPosition(true);
            pdfStripper.setTwoColumns(twoColumns);
            pdfStripper.setStartCaptureText(startCaptureText);
            pdfStripper.setEndCaptureText(endCaptureText);
            //removes empty newlines
            pdfStripper.setAddMoreFormatting(false);

            pdfManager.setPdfTextStripper(pdfStripper);
            pdfManager.parse(sourceFile);

            String text = pdfManager.getText();
            log.debug("CAPTURED TEXT:\n{}", text);

            GlossaryScraper scrapper = new GlossaryScraper(startCaptureText, endCaptureText, addBold);
            log.info("{}", scrapper);
            Collection<Glossary> glossary = scrapper.scrape(text);
            log.error("Glossary size: " + glossary.size());

            new GlossaryCSVWriter(outputGlossaryFile, subject).write(glossary);
            log.error("glossary csv file size: " + outputGlossaryFile.getTotalSpace());
            MultipartFile multipartGlossaryFile = new MockMultipartFile(outputGlossaryFileName, outputGlossaryFile.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(outputGlossaryFile)));
            s3Service.uploadGlossary(multipartGlossaryFile);

            new GlossaryQuizCSVWriter(outputQuizFile, subject, quizName).write(glossary);
            log.error("quiz csv file size: " + outputQuizFile.getTotalSpace());
            MultipartFile multipartQuizFile = new MockMultipartFile(outputQuizFileName, outputQuizFile.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(outputQuizFile)));
            s3Service.uploadCSVTestBank(multipartQuizFile);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void writeExamFromTestBank(String subject, String quizName, String numQuest) {
        quizQuestions = athenaService.getQuizQuestions(subject, quizName, numQuest);
        if (quizQuestions == null || quizQuestions.isEmpty()) {
            log.error("I wasn't able to retrieve any quizzes with the information you provided.");
        }
        String examFileName = subject.replaceAll("\\s+","")
                + quizName.replaceAll("\\s+","")
                + new SimpleDateFormat("-MM-dd-yyyy-HHmmss").format(new Date())
                + ".txt";
        String fileContent = "";

        for (int i = 0; i < quizQuestions.size(); i++) {
            Map<String, Object> quiz = getQuizMap(quizQuestions.get(i));
            log.error(quiz);
            String term = (String) quiz.get("answer");
            String type = (String) quiz.get("type");
            if (StringUtils.isBlank(type)) {
                continue;
            }

            String question = "";
            if (StringUtils.equals(type, DEFINITION_TYPE)) {
                List<String> definitions = athenaService.getGlossaryAnswer(subject, term);
                question = String.join(", ", definitions);
            } else if (StringUtils.equals(type, MC_TYPE)) {
                question = Objects.toString(quiz.get("question"), "");
            }
            if (StringUtils.isBlank(question)) {
                log.error("question is blank");
            }

            List<String> choices = (List<String>) quiz.get("choices");
            if (choices.isEmpty()) {
                log.error("no choices in quiz [{}]", quiz);
            }

            fileContent = fileContent + (i + 1) + ". ";
            fileContent = fileContent + question;
            fileContent = fileContent + "\n";
            for (int j = 0; j < choices.size(); j++) {
                String choice = choices.get(j);
                if (!StringUtils.equals(choice, "null")) {
                    fileContent = fileContent + choices.get(j);
                    fileContent = fileContent + "\n";
                }
            }
            fileContent = fileContent + "\n";
        }

        log.error("Created quiz size: " + fileContent.length());
        MultipartFile multipartFile = new MockMultipartFile(examFileName, examFileName, "text/plain", fileContent.getBytes());

        s3Service.uploadExam(multipartFile);
        canvasService.uploadFileToCanvas(examFileName, multipartFile);
    }
}
