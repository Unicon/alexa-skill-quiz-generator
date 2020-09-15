package org.unicon.lex.model;

import java.util.List;

public class LdaMessageContent extends MessageContent {

    private List<String> relatedTerms;
    public LdaMessageContent(List<String> relatedTerms) {
        this.relatedTerms = relatedTerms;
    }

    public List<String> getRelatedTerms() {
        return this.relatedTerms;
    }
}
