package net.unicon.pdfscanner;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static net.unicon.pdfscanner.PDFStyledTextStripper.BOLD;

public class MCScraper {

    private static final Logger LOGGER = LogManager.getLogger(MCScraper.class);


    private final String BOLD_START = "[" + BOLD + "]";
    private final String BOLD_END = "[]";

    private final String OPENSTAX_FOOTER = "This OpenStax";
    private static final Pattern CHAPTER_HEADER_PATTERN = Pattern.compile(".*\\bChapter\\b\\s[\\d]+.*");

    private final String startCapture;
    private final String endCapture;
    private final boolean addBold;

    private List<String> ignoreText = new ArrayList<>();
    private List<String> mcStartsWith = Arrays.asList("", "A)", "B)", "C)", "D)");
    private List<String> tfStartsWith = Arrays.asList("", "Answer: ");
    private char[] punctuation = {',', '?', '.', ';', ':'};

    public MCScraper(String startCapture, String endCapture, boolean addBold) {
        this.startCapture = startCapture;
        this.endCapture = endCapture;
        //special case to add bold for the very first definition
        this.addBold = addBold;
    }

    public List<MCQuestion> scrape(String text) {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter(lineSeparator());
        boolean captureOn = false;
        StringBuilder sbCapture = new StringBuilder();

        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.length() > 0) {
                if (Character.isDigit(token.charAt(0))) {
                    captureOn = true;
                } else if (token.startsWith(endCapture)) {
                    captureOn = false;
                }
            }

            if (captureOn) {
                if (!isHeaderOrFooter(token)) {
                    sbCapture.append(token + "\n");
                }
            }
        }

        String capturedText = getCapturedText(sbCapture.toString());
        List<MCQuestion> mcQuestions = new LinkedList<>();

        for (String line : capturedText.split(startCapture)) {
            if (StringUtils.isNotBlank(line)) {
                List<String> question = Arrays.asList(line.split(lineSeparator()));
                question = question.stream().map(String::trim).collect(Collectors.toList());
                question.removeAll(Arrays.asList("", null));
                question.removeAll(ignoreText);
                if (question.size() > 7) {
                    for (int k = 0; k < question.size(); k++) { // account for "removing" a line only to replace it with another faulty line
                        for (int i = 0; i < question.size(); i++) {
                            if (question.get(i).startsWith("Copyright") || NumberUtils.isNumber(question.get(i))) {
                                ignoreText.add(question.get(i));
                                question.remove(i);
                            } else if (question.get(i).startsWith("Skill: ")) {
                                for (int j = i; j < question.size(); j++) {
                                    question.remove(j);
                                }
                            }
                        }
                    }
                }
                if (question.size() > 6) {
                    ensureQuestionsAndAnswersInSingleStringEach(question, mcStartsWith);
                }
                String answerLine = question.get(question.size() - 1);
                String answer = "";
                // if answer line holds answer and question doesn't start with punctuation
                if (answerLine.startsWith("Answer: ") && !ArrayUtils.contains(punctuation, question.get(0).charAt(0))) {
                    answer = answerLine.substring(answerLine.indexOf(" ")).trim(); // Assume answer is in form "Answer: <answer>"
                    if (answer.equalsIgnoreCase("true") || answer.equalsIgnoreCase("false")) {
                        if (question.size() > 2) {
                            ensureQuestionsAndAnswersInSingleStringEach(question, tfStartsWith);
                        }
                        mcQuestions.add(new MCQuestion(question.get(0), "TRUE", "FALSE", null, null, null, answer));
                    }
                    if (question.size() == 7 && question.get(1).startsWith("A)")) { // if large enough for MC question and in format of MC question
                            mcQuestions.add(new MCQuestion(question.get(0), question.get(1), question.get(2), question.get(3), question.get(4), question.get(5), answer));
                    } else if (question.size() == 6 && question.get(1).startsWith("A)")) {
                        mcQuestions.add(new MCQuestion(question.get(0), question.get(1), question.get(2), question.get(3), question.get(4), null, answer));
                    }
                }
            }
        }
        return mcQuestions;
    }

    private void ensureQuestionsAndAnswersInSingleStringEach(List<String> question, List<String> startsWith) {
        if (question.size() > 2) {
            for (int i = 1; i < question.size(); i++) {
                if (i < startsWith.size()) {
                    while (question.size() > i && !question.get(i).startsWith(startsWith.get(i))) {
                        String fullLine = question.get(i - 1) + " " + question.get(i);
                        question.set(i - 1, fullLine);
                        question.remove(i);
                    }
                } else if (question.get(i).startsWith("Answer: ") && question.size() > i + 1) {
                    for (int j = i + 1; j < question.size(); j++) {
                        question.remove(j);
                    }
                } else if (i == 5) {
                    while (!(question.get(i).startsWith("E)") || question.get(i).startsWith("Answer: "))) {
                        String fullLine = question.get(i - 1) + " " + question.get(i);
                        question.set(i - 1, fullLine);
                        question.remove(i);
                    }
                } else if (i == 6) {
                    while (question.size() > 6 && !question.get(i).startsWith("Answer: ")) {
                        String fullLine = question.get(i - 1) + " " + question.get(i);
                        question.set(i - 1, fullLine);
                        question.remove(i);
                    }
                } else {
                    String fullLine = question.get(i - 1) + " " + question.get(i);
                    question.set(i - 1, fullLine);
                    question.remove(i);
                }
            }
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
        return "MCScraper{" +
                "startCapture='" + startCapture + '\'' +
                ", endCapture='" + endCapture + '\'' +
                ", addBold=" + addBold +
                '}';
    }
}
