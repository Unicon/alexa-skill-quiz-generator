package org.unicon.ask.requestinterceptors;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.interceptor.RequestInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestLogger implements RequestInterceptor {
    private static final Logger log = LogManager.getLogger(RequestLogger.class);

    @Override
    public void process(HandlerInput handlerInput) {
        log.info(String.format("Request Envelope: %s", handlerInput.getRequestEnvelope()));
    }
}
