package org.unicon.lex.services.intent;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class GradeService extends AbstractLexService {
    public static final String NAME = "grade";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String message = "Your grade is a B+ (88.7%)";
        return new DefaultMessageContent(message);    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return NAME;
    }

}
