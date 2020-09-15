package net.unicon.pdfscanner;

import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static org.apache.commons.csv.QuoteMode.ALL;

public class MCQuestionCSVWriter implements Writer<MCQuestion> {

    private static final Logger LOGGER = LogManager.getLogger(MCQuestionCSVWriter.class);

    private final File file;

    private final String subject;
    private final String chapter;

    public MCQuestionCSVWriter(File file, String subject, String chapter) {
        this.file = file;
        this.subject = subject.toLowerCase();
        this.chapter = chapter;
    }

    @Override
    public void write(Collection<MCQuestion> collection) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8)), DEFAULT.withQuoteMode(ALL))) {
            collection.stream().forEach(e -> {
                try {
                    List<String> record = new ArrayList<>();
                    JSONObject quizData = new JSONObject();
                    record.add(subject);
                    record.add(chapter);
                    quizData.put("type", "multiple-choice");
                    quizData.put("question", e.getQuestion());
                    quizData.put("choices", e.getChoicesList().toArray());
                    quizData.put("answer", e.getAnswer());
                    record.add(quizData.toString());
                    printer.printRecord(record.get(0), record.get(1), record.get(2));
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            });
        }
    }
}
