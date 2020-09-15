package org.unicon.lex.services.intent;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class GreetingService extends AbstractLexService {
    public static final String NAME = "greeting";

    private static final String GREETING_MESSAGE = "Hello, %s.  What can I do for you today?";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String name = getValueFromSlotOrSessionOrInputTranscript(request, "name");
        String message = String.format(GREETING_MESSAGE, name);
        return new DefaultMessageContent(message);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
