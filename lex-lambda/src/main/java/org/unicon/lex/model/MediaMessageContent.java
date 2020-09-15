package org.unicon.lex.model;

public class MediaMessageContent extends MessageContent {

    private String html;

    public MediaMessageContent() {
    }

    public MediaMessageContent(String html) {
        this.html = html;
    }

    public String getHtml() {
        return html;
    }
}
