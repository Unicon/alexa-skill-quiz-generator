package org.unicon.ask.handlers.request;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import org.unicon.lex.services.intent.WriteQuizService;

import java.util.List;
import java.util.Optional;

public class WriteQuizIntentHandler extends AbstractIntentRequestHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        final RequestHelper helper = RequestHelper.forHandlerInput(handlerInput);
        return helper.getRequestType().equals("IntentRequest") &&
                helper.getIntentName().equals("writeQuiz");
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        final Intent currentIntent = intentRequest.getIntent();
        init();
        WriteQuizService service = (WriteQuizService) serviceFactory.getLexService("writeQuiz");
        List<String> missingSlots = service.validate(currentIntent);
        if (missingSlots.size() > 0) {
            return service.buildValidationResponse(handlerInput, missingSlots);
        }
        return service.getResponse(handlerInput, intentRequest);
    }

}
