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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static org.apache.commons.csv.QuoteMode.ALL;

public class GlossaryQuizCSVWriter implements Writer<Glossary> {

    private static final Logger LOGGER = LogManager.getLogger(GlossaryQuizCSVWriter.class);

    private final File file;

    private final String subject;

    private final String quizNameOrChapter;

    public GlossaryQuizCSVWriter(File file, String subject, String quizNameOrChapter) {
        this.file = file;
        this.subject = subject.toLowerCase();
        this.quizNameOrChapter = quizNameOrChapter.toLowerCase();
    }

    @Override
    public void write(Collection<Glossary> collection) throws IOException {
        try (CSVPrinter printer =
                     new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8)),
                             DEFAULT.withQuoteMode(ALL))) {
            Map<String, String> glossary = new HashMap<>();
            collection.stream().forEach(e -> {
                glossary.put(GlossaryFormatter.format(e.getTerm()).toLowerCase(), e.getDefinition());
            });
            List<String> words = new ArrayList<>(glossary.keySet());
            for (int i = 0; i < glossary.size(); i++) {
                try {
                    String term = words.get(i);
                    List<String> record = new ArrayList<>();
                    record.add(subject);
                    record.add(quizNameOrChapter);
                    JSONObject quizData = new JSONObject();
                    quizData.put("type", "definition");
                    quizData.put("choices", getChoices(term, (ArrayList<String>) ((ArrayList<String>) words).clone()));
                    quizData.put("answer", term);
                    record.add(quizData.toString());
                    printer.printRecord(record.get(0), record.get(1), record.get(2));
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private List<String> getChoices(String term, List<String> words) {
        Random rand = new Random();
        List<String> choices = new ArrayList<>();
        choices.add(term);
        for (int i = 0; i < 4; i++) {
            int randomIndex = rand.nextInt(words.size());
            choices.add(words.get(randomIndex));
            words.remove(randomIndex);
        }
        Collections.shuffle(choices);
        return choices;
    }
}
