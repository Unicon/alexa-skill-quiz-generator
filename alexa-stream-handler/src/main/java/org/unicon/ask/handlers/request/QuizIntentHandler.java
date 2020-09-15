package org.unicon.ask.handlers.request;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import org.unicon.lex.services.intent.QuizService;

import java.util.List;
import java.util.Optional;

public class QuizIntentHandler extends AbstractIntentRequestHandler implements IntentRequestHandler {
    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        final RequestHelper helper = RequestHelper.forHandlerInput(handlerInput);
        return helper.getRequestType().equals("IntentRequest") &&
                helper.getIntentName().equals("quiz");
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        final Intent currentIntent = intentRequest.getIntent();
        init();
        QuizService service = (QuizService) serviceFactory.getLexService("quiz");
        List<String> missingSlots = service.validate(currentIntent);
        if (missingSlots.size() > 0) {
            return service.buildValidationResponse(handlerInput, intentRequest, missingSlots);
        }
        return service.getResponse(handlerInput, intentRequest);
    }
}
