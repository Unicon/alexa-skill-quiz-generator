package org.unicon.lex.services.intent;

import java.util.List;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class SubjectService extends AbstractLexService {
    public static final String NAME = "searchSubject";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        return new DefaultMessageContent("Done");
    }

    @Override
    public List<String> validate(LexRequest request) {
        request.getSessionAttributes().remove("subject");
        List<String> results = super.validate(request);
        return results;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
