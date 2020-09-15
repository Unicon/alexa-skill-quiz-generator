package org.unicon.lex.utils;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.unicon.lex.intents.response.LexResponse;

public class Base64Utils {
    private static ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LogManager.getLogger(Base64Utils.class.getName());

    public static String encode(LexResponse response) {
        String abc = "";
        try {
            log.error("LexResponse [{}]",  response);
            String responseStr = mapper.writeValueAsString(response);
            log.error("response length {}, as string [{}]",  responseStr.length(), responseStr);
            byte[] encodedBytes = Base64.encodeBase64(responseStr.getBytes());
            abc = new String(encodedBytes);
            log.error("encodedBytes length [{}], as str [{}]", abc.length(), abc);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return abc;
    }

    public static LexResponse decode(String response) {
        LexResponse abc = null;
        byte[] decodedLexResponseBytes = Base64.decodeBase64(response.getBytes());
        log.error("decodedLexResponseBytes [{}]",  new String(decodedLexResponseBytes));
        try {
            abc = mapper.readValue(new String(decodedLexResponseBytes), LexResponse.class);
            log.error("abc type [{}]",  abc);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return abc;
    }

}
