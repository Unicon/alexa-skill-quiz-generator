package net.unicon.pdfscanner;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class GlossaryStemmer {

    private GlossaryStemmer() {
    }

    public static String stem(String phrase) {
        SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

        StringBuilder sb = new StringBuilder();
        for (String word : phrase.split("\\s")) {
            sb.append(stemmer.stem(word)).append(" ");
        }
        return sb.toString().trim();
    }
}
