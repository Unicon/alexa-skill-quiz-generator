package org.unicon.lex.services.intent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.model.SessionMessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class SessionService extends AbstractLexService {
    public static final String NAME = "session";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        if (request.getSessionAttributes().size() == 0) {
            return new DefaultMessageContent("No session information");
        }

        Map<String, String> sessionAttributes = new HashMap<>();
        for (Entry<String, String> attr : request.getSessionAttributes().entrySet()) {
            sessionAttributes.put(attr.getKey(), attr.getValue());
        }
        return new SessionMessageContent(sessionAttributes);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
