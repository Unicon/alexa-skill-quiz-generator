package org.unicon.ask.handlers.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.services.DefaultServiceFactory;
import org.unicon.lex.services.LexServiceFactory;
import org.unicon.lex.services.PropertiesService;


public abstract class AbstractIntentRequestHandler {
    final Logger log = LogManager.getLogger(AbstractIntentRequestHandler.class.getName());

    protected LexServiceFactory serviceFactory;
    protected PropertiesService propertiesService;

    protected void init() {
        // lambda execution contexts will reuse these services once
        // they've been created
        if (propertiesService == null) {
            propertiesService = new PropertiesService();
        }

        if (serviceFactory == null) {
            serviceFactory = new DefaultServiceFactory(propertiesService.getProperties());
        }
    }
}
