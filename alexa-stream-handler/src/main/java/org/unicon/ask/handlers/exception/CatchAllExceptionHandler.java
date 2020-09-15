package org.unicon.ask.handlers.exception;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class CatchAllExceptionHandler implements ExceptionHandler {

    private static final Logger log = LogManager.getLogger(CatchAllExceptionHandler.class);

    @Override
    public boolean canHandle(HandlerInput handlerInput, Throwable throwable) {
        return true;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, Throwable throwable) {
        log.error(throwable.getMessage());
        throwable.printStackTrace();
        final String speech = "Sorry, something has gone wrong. Please try again.";
        return handlerInput.getResponseBuilder()
                .withSpeech(speech)
                .withReprompt(speech)
                .build();
    }
}
