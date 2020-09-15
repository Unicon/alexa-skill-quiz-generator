package org.unicon.lex.services;

import org.unicon.lex.services.intent.LexService;

public interface LexServiceFactory {

    LexService getLexService(String intentName);
}
