package org.unicon.lex;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.services.DefaultServiceFactory;
import org.unicon.lex.services.LexServiceFactory;
import org.unicon.lex.services.PropertiesService;
import org.unicon.lex.services.intent.LexService;

import java.util.List;

import static org.unicon.lex.services.LexResponseHelper.createElicitIntentAction;

public class App {
    final Logger log = LogManager.getLogger(getClass());
    
    private LexServiceFactory serviceFactory;
    private PropertiesService propertiesService;

    public Object handleRequest(LexRequest request, Context context) {
        // make sure all necessary services have been started.
        init();

        log.error("Incoming LexRequest [{}]",  request);

        // Find the service that processes the incoming intent
        // each intent can have its own service
        String intentName = request.getCurrentIntent().getName();
        if (StringUtils.isBlank(intentName)) {
            return buildMissingIntentResponse(request);
        }

        log.debug("intentName {}",  intentName);
        LexService service = serviceFactory.getLexService(intentName);
        if (service == null) {
            return buildMissingServiceResponse();
        }

        // Validate the request by making sure all slots have data,
        // either inside the slot structure or in the session attributes
        List<String> missingSlots = service.validate(request);
        if (missingSlots.size() > 0) {
            log.debug("missingSlots [" + missingSlots + "]");
            LexResponse response = service.buildValidationResponse(request, missingSlots);
            log.error("response [{}]", response);
            return response;
        }

        // The request has all of the required information,
        // build a response and return
        log.error("no missing slots");
        LexResponse response = service.getResponse(request);
        log.error("response [{}]", response);
        return response;
    }

    public LexResponse buildMissingIntentResponse(LexRequest request) {
        LexResponse response = new LexResponse();
        createElicitIntentAction(response);
        response.getSessionAttributes().putAll(request.getSessionAttributes());
        response.getSessionAttributes().remove("lexResponse");
        return response;
    }

    private LexResponse buildMissingServiceResponse() {
        // TODO create a valid response for this failure mode
        LexResponse response = new LexResponse();
        return response;
    }
    
    private void init() {
        // lambda execution contexts will reuse these services once
        // they've been created
        if (propertiesService == null) {
            log.error("Initializing propertiesService");
            propertiesService = new PropertiesService();
        }

        if (serviceFactory == null) {
            log.error("Initializing serviceFactory");
            serviceFactory = new DefaultServiceFactory(propertiesService.getProperties());
        }
    }

    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
    }
}
