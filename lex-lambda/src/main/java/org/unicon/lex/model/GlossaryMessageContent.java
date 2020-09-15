package org.unicon.lex.model;

import java.util.List;

public class GlossaryMessageContent extends MessageContent {
    private String subject;
    private String term;
    private List<String> definitions;

    public GlossaryMessageContent(){
    }

    public GlossaryMessageContent(String subject, String term, List<String> definitions) {
        this.subject = subject;
        this.term = term;
        this.definitions = definitions;
    }

    public String getSubject() {
        return this.subject;
    }
    public String getTerm() {
        return this.term;
    }
    public List<String> getDefinitions() {
        return this.definitions;
    }
}
