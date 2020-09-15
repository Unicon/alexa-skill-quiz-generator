package net.unicon.pdfscanner;

import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

// See: https://radixcode.com/pdfbox-example-code-how-to-extract-text-from-pdf-file-with-java

public class PDFManager {

    private String text;

    private String filePath;

    private Integer startPage;

    private Integer endPage;

    private PDFTextStripper pdfTextStripper;

    public void parse() throws IOException {
        parse(new File(filePath));
    }

    public void parse(File file) throws IOException {
        PDFParser parser;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            parser = new PDFParser(randomAccessFile);
            parser.parse();
        }

        if (parser != null) {
            try (PDDocument pdDoc = new PDDocument(parser.getDocument())) {
                if (pdfTextStripper == null) {
                    pdfTextStripper = new PDFTextStripper();
                }

                int firstPage = startPage == null ? 0 : startPage;
                pdfTextStripper.setStartPage(firstPage);
                pdfTextStripper.setEndPage(endPage == null ? pdDoc.getNumberOfPages() : endPage);
                text = pdfTextStripper.getText(pdDoc);
            }
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public void setPdfTextStripper(PDFTextStripper pdfTextStripper) {
        this.pdfTextStripper = pdfTextStripper;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "PDFManager{" +
                "filePath='" + filePath + '\'' +
                ", startPage=" + startPage +
                ", endPage=" + endPage +
                '}';
    }
}
