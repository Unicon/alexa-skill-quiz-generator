package org.unicon.lex.services.intent;

import java.util.List;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class TermService extends AbstractLexService {
    public static final String NAME = "searchTerm";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        return new DefaultMessageContent("Done");
    }

    @Override
    public List<String> validate(LexRequest request) {
        request.getSessionAttributes().remove("term");
        List<String> results = super.validate(request);
        return results;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
