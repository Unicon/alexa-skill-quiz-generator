package org.unicon.lex.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MessageContent {
    final Logger log = LogManager.getLogger(MessageContent.class.getName());
    private ObjectMapper mapper = new ObjectMapper();

    public String convertToJsonString() {
        String results = "";
        try {
            results = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            DefaultMessageContent error = new DefaultMessageContent(e.getMessage());
            try {
                log.error(e.getMessage(), e);
                results = mapper.writeValueAsString(error);
            } catch (JsonProcessingException e1) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }
        return results;
    }
}
