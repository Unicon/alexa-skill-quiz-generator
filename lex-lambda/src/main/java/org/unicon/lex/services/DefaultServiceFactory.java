package org.unicon.lex.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.services.intent.LexService;
import org.unicon.lex.utils.ClassUtils;

public class DefaultServiceFactory implements LexServiceFactory {
    final Logger log = LogManager.getLogger(getClass());

    private Map<String, LexService> services = new HashMap<>();
    private Properties properties;
    public Properties getProperties() {
        return properties;
    }

    public DefaultServiceFactory(Properties properties) {
        this.properties = properties;
        ClassUtils classUtils = new ClassUtils();
        services = classUtils.buildLexServices(properties);
    }

    @Override
    public LexService getLexService(String intentName) {
        log.error("intentName [{}]", intentName);
        LexService service = services.get(intentName);
        if (service == null) {
            log.error("Service [{}] not found", intentName);
        }
        log.error("retrieved service {}",  service);
        return service;
    }
}
