package net.unicon.pdfscanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.lang.System.lineSeparator;
import static net.unicon.pdfscanner.PDFStyledTextStripper.BOLD;

public class GlossaryScraper {

    private static final Logger LOGGER = LogManager.getLogger(GlossaryScraper.class);


    private final String BOLD_START = "[" + BOLD + "]";
    private final String BOLD_END = "[]";

    private final String OPENSTAX_FOOTER = "This OpenStax";
    private static final Pattern CHAPTER_HEADER_PATTERN = Pattern.compile(".*\\bChapter\\b\\s[\\d]+.*");

    private final String startCapture;
    private final String endCapture;
    private final boolean addBold;

    public GlossaryScraper(String startCapture, String endCapture, boolean addBold) {
        this.startCapture = startCapture;
        this.endCapture = endCapture;
        //special case to add bold for the very first definition
        this.addBold = addBold;
    }

    public List<Glossary> scrape(String text) {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter(lineSeparator());
        boolean captureOn = false;
        StringBuilder sbCapture = new StringBuilder();

        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.contains(startCapture)) {
                captureOn = true;
                continue;
            } else if (token.contains(endCapture)) {
                captureOn = false;
                continue;
            }

            if (captureOn) {
                if (!isHeaderOrFooter(token)) {
                    sbCapture.append(token + "\n");
                }
            }
        }

        String capturedText = getCapturedText(sbCapture.toString());
        List<Glossary> terms = new LinkedList<>();

        for (String line : capturedText.split("\n")) {
            int boldStartIndex = line.indexOf(BOLD_START);

            if (boldStartIndex != -1) {
                addToGlossary(terms, line, boldStartIndex, line.indexOf(BOLD_END));
            } else {
                updateGlossaryDefinition(terms, line);
            }
        }
        return terms;
    }

    private void addToGlossary(List<Glossary> terms, String line, int boldStartIndex, int boldEndIndex) {
        int lineStartIndex = boldStartIndex + BOLD_START.length();
        try {
            String term = line.substring(lineStartIndex, boldEndIndex);
            String definition = line.substring(line.indexOf(BOLD_END) + BOLD_END.length());
            terms.add(new Glossary(term.trim(), definition.trim()));
        } catch (StringIndexOutOfBoundsException ex) {
            //skip some glossary definitions that do not follow pattern, i.e. have [BOLD] in the definition
            LOGGER.debug("unable to add glossary line \"{}\"", line);
            LOGGER.debug(ex.getMessage(), ex);
        }
    }

    private void updateGlossaryDefinition(List<Glossary> terms, String line) {
        String additionalLine = line.trim();
        if (!isHeaderOrFooter(additionalLine)) {
            //splice multi-lined definitions
            Glossary previousTerm = ((LinkedList<Glossary>) terms).getLast();
            previousTerm.setDefinition(previousTerm.getDefinition() + " " + additionalLine.replace(BOLD_END, ""));
        }
    }

    private String getCapturedText(String capturedText) {
        String text = capturedText;
        if (addBold) {
            //sometimes the first item does not start with [BOLD]
            text = BOLD_START + capturedText;
        }
        return text.trim();
    }

    private boolean isHeaderOrFooter(String line) {
        return line.startsWith(OPENSTAX_FOOTER) || CHAPTER_HEADER_PATTERN.matcher(line).matches();
    }

    @Override
    public String toString() {
        return "GlossaryScraper{" +
                "startCapture='" + startCapture + '\'' +
                ", endCapture='" + endCapture + '\'' +
                ", addBold=" + addBold +
                '}';
    }
}
