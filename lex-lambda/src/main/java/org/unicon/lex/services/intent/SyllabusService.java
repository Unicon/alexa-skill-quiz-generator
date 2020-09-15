package org.unicon.lex.services.intent;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class SyllabusService extends AbstractLexService {
    public static final String NAME = "syllabus";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String message = "The topic for the current week is 'Synthesis of Biological Macromolecules'";
        return new DefaultMessageContent(message);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
