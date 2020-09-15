package net.unicon.pdfscanner;

import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static org.apache.commons.csv.QuoteMode.ALL;

public class GlossaryCSVWriter implements Writer<Glossary> {

    private static final Logger LOGGER = LogManager.getLogger(GlossaryCSVWriter.class);

    private final File file;

    private final String subject;

    public GlossaryCSVWriter(File file, String subject) {
        this.file = file;
        this.subject = subject.toLowerCase();
    }

    @Override
    public void write(Collection<Glossary> collection) throws IOException {
        try (CSVPrinter printer =
                     new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8)),
                             DEFAULT.withQuoteMode(ALL))) {
            collection.stream().forEach(e -> {
                try {
                    String term = GlossaryFormatter.format(e.getTerm()).toLowerCase();
                    String stemmed = GlossaryStemmer.stem(term);
                    printer.printRecord(subject, term, stemmed, e.getDefinition());
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            });
        }
    }
}
