package org.unicon.lex.model;

import java.util.Map;

public class SessionMessageContent extends MessageContent {
    private Map<String, String> sessionAttributes;
    public SessionMessageContent(Map<String, String> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public Map<String, String> getSessionAttributes() {
        return this.sessionAttributes;
    }
}
