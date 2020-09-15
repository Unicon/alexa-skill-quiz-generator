package org.unicon.lex.model;

public class DefaultMessageContent extends MessageContent {

    private String message;
    public DefaultMessageContent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
