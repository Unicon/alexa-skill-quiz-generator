package org.unicon.lex.model;

public class QuizMessageContent extends MessageContent {
    private String result;

    public QuizMessageContent(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public String toString() {
        return result;
    }
}
