package org.unicon.lex.services;

import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexDialogAction;
import org.unicon.lex.intents.response.LexMessage;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.GlossaryMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.services.external.AthenaServiceImpl;
import org.unicon.lex.services.intent.GlossaryService;
import org.unicon.lex.utils.Base64Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlossaryServiceTest {
    final Logger log = LogManager.getLogger(getClass());

    private GlossaryService service;
    private PropertiesService propertiesService;

    @Before
    public void setup() {
        service = new GlossaryService();
        propertiesService = new PropertiesService();
    }

    @Test
    public void properties() {
        Properties properties = propertiesService.getProperties();
        String database = properties.getProperty("athena.database");
        assertEquals("openstax2", database);
    }

    @Test
    public void getAnswerNoService() {
        LexRequest request = new LexRequest();
        MessageContent answer = service.getAnswer(request);
        assertEquals(DefaultMessageContent.class, answer.getClass());
        DefaultMessageContent error = (DefaultMessageContent) answer;
        assertEquals("No database attached", error.getMessage());
    }

    @Test
    public void getGlossary() {
        AthenaServiceImpl databaseService = mock(AthenaServiceImpl.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        List<String> definitions =
                Arrays.asList(definition);
        GlossaryMessageContent expectedAnswer = new GlossaryMessageContent(subject,term, definitions);
        when(databaseService.getGlossaryDefinition(subject, term, "glossary")).thenReturn(definition);
        when(databaseService.getGlossaryDefinitionMode(subject, term)).thenReturn("ADD_FIRST");
        when(databaseService.getGlossaryAnswer(subject, term)).thenCallRealMethod();
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(expectedAnswer.getDefinitions(), result.getDefinitions());
    }

    @Test
    public void getCustomGlossaryOverride() {
        AthenaServiceImpl databaseService = mock(AthenaServiceImpl.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        String customDefinition = "a new definition of a planet";
        List<String> definitions =
                Arrays.asList(customDefinition);
        GlossaryMessageContent expectedAnswer = new GlossaryMessageContent(subject,term, definitions);
        when(databaseService.getGlossaryDefinition(subject, term, "glossary")).thenReturn(definition);
        when(databaseService.getGlossaryDefinition(subject, term, "custom_glossary")).thenReturn(customDefinition);
        when(databaseService.getGlossaryDefinitionMode(subject, term)).thenReturn("OVERRIDE");
        when(databaseService.getGlossaryAnswer(subject, term)).thenCallRealMethod();
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(expectedAnswer.getDefinitions(), result.getDefinitions());
    }

    @Test
    public void getCustomGlossary() {
        AthenaServiceImpl databaseService = mock(AthenaServiceImpl.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        String customDefinition = "a new definition of a planet";
        List<String> definitions =
                Arrays.asList(customDefinition, definition);
        GlossaryMessageContent expectedAnswer = new GlossaryMessageContent(subject,term, definitions);
        when(databaseService.getGlossaryDefinition(subject, term, "glossary")).thenReturn(definition);
        when(databaseService.getGlossaryDefinition(subject, term, "custom_glossary")).thenReturn(customDefinition);
        when(databaseService.getGlossaryDefinitionMode(subject, term)).thenReturn("ADD_FIRST");
        when(databaseService.getGlossaryAnswer(subject, term)).thenCallRealMethod();
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(expectedAnswer.getDefinitions(), result.getDefinitions());
    }

    @Test
    public void getCustomGlossaryReversed() {
        AthenaServiceImpl databaseService = mock(AthenaServiceImpl.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        String customDefinition = "a new definition of a planet";
        List<String> definitions =
                Arrays.asList(definition, customDefinition);
        GlossaryMessageContent expectedAnswer = new GlossaryMessageContent(subject,term, definitions);
        when(databaseService.getGlossaryDefinition(subject, term, "glossary")).thenReturn(definition);
        when(databaseService.getGlossaryDefinition(subject, term, "custom_glossary")).thenReturn(customDefinition);
        when(databaseService.getGlossaryDefinitionMode(subject, term)).thenReturn("ADD_LAST");
        when(databaseService.getGlossaryAnswer(subject, term)).thenCallRealMethod();
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(expectedAnswer.getDefinitions(), result.getDefinitions());
    }

    @Test
    public void getNoGlossary() {
        AthenaServiceImpl databaseService = mock(AthenaServiceImpl.class);
        String subject = "astronomy";
        String term = "planet";
        List<String> definitions =
                Arrays.asList(String.format("No definitions found for [%s] in subject [%s]", term, subject));
        GlossaryMessageContent expectedAnswer = new GlossaryMessageContent(subject,term, definitions);
        when(databaseService.getGlossaryDefinitionMode(subject, term)).thenReturn("ADD_FIRST");
        when(databaseService.getGlossaryAnswer(subject, term)).thenCallRealMethod();
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(expectedAnswer.getDefinitions(), result.getDefinitions());
    }

    @Test
    public void getAnswer() {
        AthenaService databaseService = mock(AthenaService.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        List<String> expectedAnswer = Arrays.asList(definition);
        when(databaseService.getGlossaryAnswer(subject, term))
                .thenReturn(expectedAnswer);
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        MessageContent answer = service.getAnswer(request);
        assertEquals(GlossaryMessageContent.class, answer.getClass());
        GlossaryMessageContent result = (GlossaryMessageContent) answer;
        assertEquals(Arrays.asList(definition), result.getDefinitions());
    }

    @Test
    public void getResonseNominal() {
        AthenaService databaseService = mock(AthenaService.class);
        String subject = "astronomy";
        String term = "planet";
        String definition = "definition of a planet";
        List<String> expectedAnswer = Arrays.asList(definition);
        when(databaseService.getGlossaryAnswer(subject, term))
                .thenReturn(expectedAnswer);
        service.setAthenaService(databaseService);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", term);
        request.getCurrentIntent().getSlots().put("subject", subject);
        LexResponse response = service.getResponse(request);
        assertEquals(term, response.getSessionAttributes().get("term"));
        assertEquals(subject, response.getSessionAttributes().get("subject"));
        LexDialogAction action = response.getDialogAction();
        assertEquals("Close", action.getType());
        assertEquals("Fulfilled", action.getFulfillmentState());
        LexMessage message = action.getMessage();
        String messageContent = message.getContent();
        ObjectMapper mapper = new ObjectMapper();
        try {
            GlossaryMessageContent content = mapper.readValue(messageContent, GlossaryMessageContent.class);
            assertEquals(term, content.getTerm());
            assertEquals(subject, content.getSubject());
            assertEquals(Arrays.asList(definition), content.getDefinitions());
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
    }

    @Test
    public void validateNominal() {
        LexRequest request = new LexRequest();
        List<String> result = service.validate(request);
        // This test assumes that the basic LexRequest basically has nothing in it
        assertEquals(0, result.size());
    }

    @Test
    public void validateRequestHasSlots() {
        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", "planet");
        List<String> result = service.validate(request);
        // passes because term has a value
        assertEquals(0, result.size());
    }

    @Test
    public void validateRequestHasSlotsNoValue() {
        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", "");
        List<String> result = service.validate(request);
        // fails because term has no value
        assertEquals(1, result.size());
    }

    @Test
    public void validateRequestHasSlotsNoValueButSessionDoes() {
        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", "");
        Map<String, String> sessionAttributes = new HashMap<>();
        sessionAttributes.put("term", "planet");
        request.setSessionAttributes(sessionAttributes);
        List<String> result = service.validate(request);
        // succeeds because session has a value for the term
        assertEquals(0, result.size());
    }

    @Test
    public void validateRequestHasSlotsNoValueFounddInInputTranscript() {
        LexResponse response = new LexResponse();
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType("ElicitSlot");
        dialogAction.setSlotToElicit("term");
        response.setDialogAction(dialogAction);
        String encodedResponse = Base64Utils.encode(response);

        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", "");
        request.setInputTranscript("asteroid belt");
        request.getSessionAttributes().put("lexResponse", encodedResponse);

        List<String> result = service.validate(request);
        // succeeds because session has a value for the term inputTranscript
        assertEquals(0, result.size());
    }

    @Test
    public void buildValidationResponse() {
        LexRequest request = new LexRequest();
        request.getCurrentIntent().getSlots().put("term", "");
        List<String> missingSlots = new ArrayList<>();
        missingSlots.add("term");
        LexResponse response = service.buildValidationResponse(request, missingSlots);

        LexDialogAction dialogAction = response.getDialogAction();
        assertEquals("ElicitSlot", dialogAction.getType());
        assertEquals("term", dialogAction.getSlotToElicit());
        Map<String, String> slots = dialogAction.getSlots();
        assertEquals("", slots.get("term"));
    }

    @Test
    public void encode() {
        LexResponse response = new LexResponse();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String responseStr = mapper.writeValueAsString(response);
            log.error("reesponseStr [{}]", responseStr);
            byte[] encodedStr = Base64.encode(responseStr.getBytes());
            log.error("responseStrEncoded [{}]",  encodedStr);
            byte[] decodedStr = Base64.decode(encodedStr);
            log.error("decodedAgain [{}]",  decodedStr);
            LexResponse newResponse = mapper.readValue(new String(decodedStr), LexResponse.class);
            log.error("newResponse [{}]",  newResponse);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
