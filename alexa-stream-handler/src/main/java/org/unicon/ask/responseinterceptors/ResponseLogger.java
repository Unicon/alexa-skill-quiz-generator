package org.unicon.ask.responseinterceptors;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.interceptor.ResponseInterceptor;
import com.amazon.ask.model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ResponseLogger implements ResponseInterceptor {

    private static final Logger log = LogManager.getLogger(ResponseLogger.class);

    @Override
    public void process(HandlerInput handlerInput, Optional<Response> response) {
        log.info(String.format("Response: %s", response));
    }
}
