package org.unicon.lex.write.quiz;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.services.PropertiesService;
import org.unicon.lex.services.external.AthenaServiceImpl;
import org.unicon.lex.services.external.CanvasServiceImpl;
import org.unicon.lex.services.external.S3ServiceImpl;
import org.unicon.lex.write.quiz.services.WriteQuizService;

import java.util.Map;
import java.util.Properties;

public class App {
    final Logger log = LogManager.getLogger(getClass());
    private String[] bankSourcePhrases = {"bank", "testbank"};
    private String[] canvasSourcePhrases = {"canvas", "canvascourse"};

    private PropertiesService propertiesService;
    private WriteQuizService writeQuizService;

    public String handleRequest(Map<String, String> event, Context context) {
        // make sure all necessary services have been started.
        init();

        String source = sanitized(event.get("examSource"));
        String fileName = event.get("canvasFileName");
        String subject = sanitized(event.get("subject"));
        String quizName = sanitized(event.get("quizName"));
        String numQuest = event.get("numQuest");

        if (ArrayUtils.contains(bankSourcePhrases, source)) {
            writeQuizService.writeExamFromTestBank(subject, quizName, numQuest);
        } else if (ArrayUtils.contains(canvasSourcePhrases, source)) {
            writeQuizService.createTestBankFromCanvas(fileName, subject, quizName);
            writeQuizService.writeExamFromTestBank(subject, quizName, numQuest);
        }

        return "200 OK";
    }
    

    private void init() {
        // lambda execution contexts will reuse these services once
        // they've been created
        if (propertiesService == null) {
            log.error("Initializing propertiesService");
            propertiesService = new PropertiesService();
        }

        Properties properties = propertiesService.getProperties();
        writeQuizService = new WriteQuizService(new CanvasServiceImpl(properties), new AthenaServiceImpl(properties), new S3ServiceImpl(properties));

    }

    private String sanitized(String textToSanitize) {
        if (!StringUtils.isBlank(textToSanitize)) {
            return textToSanitize.toLowerCase().replaceAll("\\s+", "");
        }
        return textToSanitize;
    }

    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
    }
}
