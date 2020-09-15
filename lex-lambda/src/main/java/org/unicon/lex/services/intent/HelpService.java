package org.unicon.lex.services.intent;

import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;

import org.unicon.lex.intents.request.LexRequest;

public class HelpService extends AbstractLexService {
    public static final String NAME = "help";

    private static final String HELP_MESSAGE = "You can ask for information by typing different commands.\n" +
            "'define' provides a glossary definition of a term.\n" +
            "'related' gives a list of terms related to your term.\n" +
            "'exit' ends the conversation.\n" +
            "\n" +
            "Many of these commands are expecting you to provide a subject and a term.\n" +
            "'list subjects' will list available subjects.\n" +
            "'list terms' will list available terms in the current subject.\n" +
            "'current session' will show you what subject and term you are using, if they are set.\n" +
            "\n" +
            "A term is a word or words that you can be found in the course material.  For example,\n" +
            "in the 'astronomy' subject, 'perihelion' and 'asteroid belt' are both terms.\n" +
            "\n" +
            "You will be prompted to enter a subject or a term the first time this information is needed,\n" +
            "and for all later requests, that subject and term will be used.\n" +
            "\n" +
            "'change subject' will prompt you for a new subject.\n" +
            "'change term' will prompt you for a new term.\n" +
            "\n" +
            "'help' of course gives you this information";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        return new DefaultMessageContent(HELP_MESSAGE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
