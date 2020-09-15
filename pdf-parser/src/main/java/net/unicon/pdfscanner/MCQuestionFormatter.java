package net.unicon.pdfscanner;

import java.util.regex.Pattern;

public class MCQuestionFormatter {

    private static final Pattern FORWARD_SLASH_PATTERN = Pattern.compile("/");

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}||\\p{P}&&[^-]]");

    private MCQuestionFormatter() {
    }

    public static String format(String term) {
        String result = FORWARD_SLASH_PATTERN.matcher(term).replaceAll(" ");
        result = PUNCTUATION_PATTERN.matcher(result).replaceAll("");
        return result;
    }
}
