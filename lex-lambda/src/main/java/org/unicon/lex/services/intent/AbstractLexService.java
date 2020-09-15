package org.unicon.lex.services.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazonaws.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexDialogAction;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.services.external.CanvasService;
import org.unicon.lex.services.external.KinesisService;
import org.unicon.lex.services.external.RDSService;
import org.unicon.lex.services.external.S3Service;
import org.unicon.lex.utils.Base64Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.lang.String.format;
import static org.unicon.lex.intents.response.LexDialogActionType.ELICIT_SLOT;
import static org.unicon.lex.services.LexResponseHelper.createCloseAction;
import static org.unicon.lex.services.LexResponseHelper.createElicitSlotAction;
import static org.unicon.lex.services.LexResponseHelper.createPlainTextMessage;

public abstract class AbstractLexService implements LexService {
    final Logger log = LogManager.getLogger(AbstractLexService.class.getName());

    private RDSService rdsService;

    protected static final String INTENT_REDIRECT_NAME = "intentRedirect";

    private AthenaService service;

    @Override
    public void setAthenaService(AthenaService service) {
        this.service = service;
    }
    public AthenaService getAthenaService() {
        return this.service;
    }

    @Override
    public void setRDSService(RDSService rdsService) {
        this.rdsService = rdsService;
    }

    public RDSService getRDSService() {
        return rdsService;
    }

    private S3Service s3Service;
    public void setS3Service(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public S3Service getS3Service() {
        return this.s3Service;
    }

    private KinesisService kinesisService;
    public void setKinesisService(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
    }
    public KinesisService getKinesisService() {
        return this.kinesisService;
    }

    private CanvasService canvasService;
    public void setCanvasService(CanvasService canvasService) {
        this.canvasService = canvasService;
    }
    public CanvasService getCanvasService() {
        return this.canvasService;
    }

    @Override
    public LexResponse getResponse(LexRequest request) {
        log.error("Inside getResponse");
        MessageContent content = this.getAnswer(request);
        String answer = content.convertToJsonString();
        log.debug(answer);

        LexResponse response = new LexResponse();

        // carry all session attribute data forward, as well as all slot data
        // in session attributes.  This way, the slots do not need to be
        // requeried
        response.getSessionAttributes().putAll(request.getSessionAttributes());
        for (Entry<String, String> entry : request.getCurrentIntent().getSlots().entrySet()) {
            log.error("entry [" + entry.getKey() + ":" + entry.getValue() + "]");
            if (!StringUtils.isNullOrEmpty(entry.getValue())) {
                response.getSessionAttributes().put(entry.getKey(), entry.getValue());
            }
        }
        response.getSessionAttributes().remove("lexResponse");

        createCloseAction(response, true, createPlainTextMessage(answer));
        customizeResponse(request, response);
        publishToKinesis(request, response, content);
        log.error("response [" + response + "]");
        return response;
    }

    @Override
    public List<String> validate(LexRequest request) {
        List<String> results = new ArrayList<>();
        Map<String, String> slots = request.getCurrentIntent().getSlots();
        for (String slot : slots.keySet()) {
            String value = getValueFromSlotOrSessionOrInputTranscript(request, slot);
            if (StringUtils.isNullOrEmpty(value)) {
                results.add(slot);
            }
        }
        return results;
    }

    private String getSlotValueFromSession(LexRequest request, String slotName) {
        String value = "";
        Map<String, String> sessionAttributes = request.getSessionAttributes();
        if (sessionAttributes.containsKey(slotName)) {
            value = sessionAttributes.get(slotName);
            log.error("value from sessionAttribute {}",  value);
        }
        return value;
    }

    private String getSlotValueFromInputTranscript(LexRequest request, String slotName) {
        String encodedLexResponse = request.getSessionAttributes().get("lexResponse");
        if (StringUtils.isNullOrEmpty(encodedLexResponse)) {
            return "";
        }
        LexResponse rebuiltLexResponse = Base64Utils.decode(encodedLexResponse);
        log.error("rebuiltLexResponse type [{}]",  rebuiltLexResponse);
        LexDialogAction dialogAction = rebuiltLexResponse.getDialogAction();
        String value = "";
        if (ELICIT_SLOT.equals(dialogAction.getType())) {
            String slotToElicit = dialogAction.getSlotToElicit();
            if (slotName.equals(slotToElicit)) {
                String inputTranscript = request.getInputTranscript();
                value = inputTranscript;
            }
        }
        return value;
    }

    public String getValueFromSlotOrSessionOrInputTranscript(LexRequest request, String name) {
        Map<String, String> slots = request.getCurrentIntent().getSlots();
        String value = slots.get(name);
        log.error("value from slot {}",  value);
        if (StringUtils.isNullOrEmpty(value)) {
            value = getSlotValueFromSession(request, name);
        }
        if (StringUtils.isNullOrEmpty(value)) {
            value = getSlotValueFromInputTranscript(request, name);
            // TODO this semlls, shouldn't update request here.
            request.getCurrentIntent().getSlots().put(name, value);
        }
        log.error("returning value {}", value);
        return value;
    }

    protected LexResponse buildResponseForMissingSlot(LexRequest request, String slot, String slotName) {
        LexResponse response = new LexResponse();
        createElicitSlotAction(response, request.getCurrentIntent().getName(), request.getCurrentIntent().getSlots(),
               slot, createPlainTextMessage(format("What is the %s?", slotName)));
        response.getSessionAttributes().putAll(request.getSessionAttributes());
        response.getSessionAttributes().remove("lexResponse");
        return response;
    }

    protected Optional<Response> buildResponseForMissingSlot(HandlerInput handlerInput, String slot, String slotName) {
        return handlerInput.getResponseBuilder()
                .withSpeech(format("What is the %s?", slotName))
                .addDirective(ElicitSlotDirective.builder().withSlotToElicit(slot).build())
                .build();
    }

    @Override
    public LexResponse buildValidationResponse(LexRequest request, List<String> missingSlots) {
        String missingSlot = missingSlots.get(0);
        LexResponse response = buildResponseForMissingSlot(request, missingSlot, missingSlot);
        String encodedResponse = Base64Utils.encode(response);
        response.getSessionAttributes().put("lexResponse",  encodedResponse);
        return response;
    }

    public abstract String getName();

    protected Map<String, String> copySlotData(Map<String, String> sessionAttributes, String... slotNames) {
        Map<String, String> slots = new HashMap<>();
        for (String name : slotNames) {
            slots.put(name, sessionAttributes.get(name));
        }
        return slots;
    }

    public void customizeResponse(LexRequest request, LexResponse response) {
        // override in extending classes if needed
    }

    public void publishToKinesis(LexRequest request, LexResponse response, MessageContent content) {
        // override in extending classes if needed
    }

    protected String sanitized(String textToSanitize) {
        if (!StringUtils.isNullOrEmpty(textToSanitize)) {
            return textToSanitize.toLowerCase().replaceAll("\\s+", "");
        }
        return textToSanitize;
    }
}
