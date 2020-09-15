package net.unicon.pdfscanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PDFStyledTextStripper extends PDFTextStripper {

    //see https://github.com/mkl-public/testarea-pdfbox1/blob/master/src/main/java/mkl/testarea/pdfbox1/extract/PDFStyledTextStripper.java#L39

    private Set<String> currentStyle = Collections.singleton("UNDEFINED");

    public static final String BOLD = "BOLD";

    public static final String[] BOLD_INDICATORS = {"BOLD", "DELSQY+CAIROFONT-1-0"};

    private boolean twoColumns = false;

    private boolean startCapture = false;

    private String startCaptureText;

    private String endCaptureText;

    private List<List<WordWithTextPositions>> secondColumnLines = new ArrayList<>();

    public PDFStyledTextStripper() throws IOException {
        super();
    }

    @Override
    protected void writeLine(List<WordWithTextPositions> line) throws IOException {
        int numberOfStrings = line.size();
        if (twoColumns) {
            if (startCapture) {
                if (StringUtils.equals(endCaptureText, line.get(0).getText())) {
                    outputSecondColumn();
                }
                for (int i = 0; i < numberOfStrings; i++) {
                    WordWithTextPositions word = line.get(i);
                    writeWord(word, i, numberOfStrings);
                    if ((i == 0 || i == 1) && !isBold(word.getTextPositions().get(0))) { // if indication that remaining words belong in second column
                        List<WordWithTextPositions> secondColumnLine = new ArrayList<>();
                        for (int j = i + 1; j < numberOfStrings; j++) {
                            secondColumnLine.add(line.get(j));
                        }
                        secondColumnLines.add(secondColumnLine);
                        break;
                    }
                }
                if (endOfPage(line.get(0).getTextPositions().get(0))) {
                    outputSecondColumn();
                }
            } else if (StringUtils.equals(startCaptureText, line.get(0).getText())) {
                writeWord(line.get(0), 0, 1);
                startCapture = true;
            }
        } else {
            writeLineForColumn(line);
        }
    }

    private void outputSecondColumn() throws IOException {
        for (List<WordWithTextPositions> secondColumnLine : secondColumnLines) {
            writeLineForColumn(secondColumnLine);
        }
        secondColumnLines = new ArrayList<>();
    }

    private void writeLineForColumn(List<WordWithTextPositions> line) throws IOException {
        int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++) {
            WordWithTextPositions word = line.get(i);
            writeWord(word, i, numberOfStrings);
        }
    }

    private void writeWord(WordWithTextPositions word, int i, int numberOfStrings) throws IOException{
        writeString(word.getText(), word.getTextPositions());
        if (i < numberOfStrings - 1) {
            writeWordSeparator();
        } else if (!word.getText().endsWith(getLineSeparator())) {
            writeLineSeparator();
        }
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        for (TextPosition textPosition : textPositions) {
            Set<String> style = determineStyle(textPosition);
            if (!style.equals(currentStyle)) {
                output.write(style.toString());
                currentStyle = style;
            }
            output.write(textPosition.getUnicode());
        }
    }

    private boolean endOfPage(TextPosition textPosition) {
        float endY = textPosition.getEndY();
        if (endY < 75) {
            return true;
        }
        return false;
    }

    private boolean isBold(TextPosition textPosition) {
        String fontName = textPosition.getFont().getName().toUpperCase();

        if (Arrays.stream(BOLD_INDICATORS).parallel().anyMatch(fontName::contains)) {
            return true;
        }

        return false;
    }

    private Set<String> determineStyle(TextPosition textPosition) {
        Set<String> styles = new HashSet<>();
        String fontName = textPosition.getFont().getName().toUpperCase();

        if (Arrays.stream(BOLD_INDICATORS).parallel().anyMatch(fontName::contains)) {
            styles.add(BOLD);
        }
        return styles;
    }

    public boolean isTwoColumns() {
        return twoColumns;
    }

    public void setTwoColumns(boolean twoColumns) {
        this.twoColumns = twoColumns;
    }

    public String getStartCaptureText() {
        return startCaptureText;
    }

    public void setStartCaptureText(String startCaptureText) {
        this.startCaptureText = startCaptureText;
    }

    public String getEndCaptureText() {
        return endCaptureText;
    }

    public void setEndCaptureText(String endCaptureText) {
        this.endCaptureText = endCaptureText;
    }
}
