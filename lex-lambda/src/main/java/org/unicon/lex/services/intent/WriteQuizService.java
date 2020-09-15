package org.unicon.lex.services.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lexruntime.model.Button;
import com.amazonaws.services.lexruntime.model.GenericAttachment;
import com.amazonaws.services.lexruntime.model.ResponseCard;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.model.QuizMessageContent;
import org.unicon.lex.services.external.CanvasService;
import org.unicon.lex.utils.Base64Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.unicon.lex.intents.response.LexResponseCardContentType.AWS_GENERIC_CARD;
import static org.unicon.lex.services.LexResponseHelper.createCloseAction;
import static org.unicon.lex.services.LexResponseHelper.createElicitSlotAction;
import static org.unicon.lex.services.LexResponseHelper.createPlainTextMessage;

public class WriteQuizService extends AbstractLexService {
    public static final String NAME = "writeQuiz";
    final Logger log = LogManager.getLogger(getClass());

    private String[] examSourcePhrases = {"bank", "testbank", "canvas", "canvascourse"};
    private String[] canvasSourcePhrases = {"canvas", "canvascourse"};

    private Map<String, String> slotNames = Stream.of(new Object[][] {
            { "subject", "subject" },
            { "quizName", "quiz name or chapter" },
            { "numQuest", "number of questions you want to include" },
            { "examSource", "source you want the questions to come from" },
            { "canvasFileName", "name of the source file in Canvas" }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));


    @Override
    public List<String> validate(LexRequest request) {
        String source = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "examSource"));
        String canvasFileName = getValueFromSlotOrSessionOrInputTranscript(request, "canvasFileName");
        String subject = getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        String quizName = getValueFromSlotOrSessionOrInputTranscript(request, "quizName");
        String numQuest = getValueFromSlotOrSessionOrInputTranscript(request, "numQuest");
        return validate(source, canvasFileName, subject, quizName, numQuest);
    }

    public List<String> validate(Intent intent) {
        String source = sanitized(intent.getSlots().get("examSource").getValue());
        String canvasFileName = intent.getSlots().get("canvasFileName").getValue();
        String subject = intent.getSlots().get("subject").getValue();
        String quizName = intent.getSlots().get("quizName").getValue();
        String numQuest = intent.getSlots().get("numQuest").getValue();
        return validate(source, canvasFileName, subject, quizName, numQuest);
    }

    private List<String> validate(String source, String canvasFileName, String subject, String quizName, String numQuest) {
        List<String> results = new ArrayList<>();
        // Make sure that the slots are elicited in this order.  We want to handle
        // answer at the very end, after the other slots are filled
        if (StringUtils.isBlank(source) || !ArrayUtils.contains(examSourcePhrases, source)) {
            results.add("examSource");
            return results;
        }

        if (ArrayUtils.contains(canvasSourcePhrases, source) && StringUtils.isBlank(canvasFileName)) {
            results.add("canvasFileName");
            return results;
        }

        if (StringUtils.isBlank(subject)) {
            results.add("subject");
            return results;
        }

        if (StringUtils.isBlank(quizName)) {
            results.add("quizName");
            return results;
        }

        if (StringUtils.isBlank(numQuest) || !StringUtils.isNumeric(numQuest)) {
            results.add("numQuest");
            return results;
        }
        return results;
    }

    @Override
    public LexResponse buildValidationResponse(LexRequest request, List<String> missingSlots) {
        String missingSlot = missingSlots.get(0);
        if (StringUtils.equals(missingSlot, "canvasFileName")) {
            return getCanvasFilesResponseCard(request);
        } else {
            LexResponse response = buildResponseForMissingSlot(request, missingSlot, slotNames.get(missingSlot));
            String encodedResponse = Base64Utils.encode(response);
            response.getSessionAttributes().put("lexResponse", encodedResponse);
            return response;
        }
    }

    public Optional<Response> buildValidationResponse(HandlerInput handlerInput, List<String> missingSlots) {
        String missingSlot = missingSlots.get(0);
        return buildResponseForMissingSlot(handlerInput, missingSlot, slotNames.get(missingSlot));
    }

    private LexResponse getCanvasFilesResponseCard(LexRequest request) {
        String canvasFileNamePrompt = "Which file would you like to use from Canvas?";
        LexResponse response = new LexResponse();
        response.getSessionAttributes().putAll(request.getSessionAttributes());
        response.getSessionAttributes().remove("lexResponse");

        CanvasService canvasService = getCanvasService();
        List<String> choices = canvasService.getFilesFromFolder();

        GenericAttachment attachment = this.buildCardChoices(choices);
        ResponseCard card = new ResponseCard();
        card.setVersion("1");
        card.setContentType(AWS_GENERIC_CARD);
        card.setGenericAttachments(Arrays.asList(attachment));
        createElicitSlotAction(response, request.getCurrentIntent().getName(),
                request.getCurrentIntent().getSlots(), "canvasFileName", createPlainTextMessage(canvasFileNamePrompt), card);
        return response;
    }

    private GenericAttachment buildCardChoices(List<String> choices) {
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            String choice = choices.get(i);

            Button button = new Button();
            button.setText(choice);
            button.setValue(choice);
            buttons.add(button);
        }
        GenericAttachment attachment = new GenericAttachment();
        attachment.setTitle("Choices");
        attachment.setButtons(buttons);
        return attachment;
    }

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String source = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "examSource"));
        String canvasFileName = getValueFromSlotOrSessionOrInputTranscript(request, "canvasFileName");
        String subject = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "subject"));
        String quizName = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "quizName"));
        String numQuest = getValueFromSlotOrSessionOrInputTranscript(request, "numQuest");

        triggerWriteExamLambda(source, canvasFileName, subject, quizName, numQuest);

        return new QuizMessageContent("Your quiz is being created.");

    }

    @Override
    public LexResponse getResponse(LexRequest request) {
        MessageContent content = this.getAnswer(request);
        String answer = content.toString();
        log.debug(answer);

        LexResponse response = new LexResponse();
        createCloseAction(response, true, createPlainTextMessage(answer));
        customizeResponse(request, response);
        publishToKinesis(request, response, content);
        log.debug("response [" + response + "]");
        return response;
    }

    public Optional<Response> getResponse(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String source = sanitized(intent.getSlots().get("examSource").getValue());
        String canvasFileName = intent.getSlots().get("canvasFileName").getValue();
        String subject = intent.getSlots().get("subject").getValue();
        String quizName = intent.getSlots().get("quizName").getValue();
        String numQuest = intent.getSlots().get("numQuest").getValue();

        triggerWriteExamLambda(source, canvasFileName, subject, quizName, numQuest);

        return handlerInput.getResponseBuilder()
                .withSpeech("Your quiz is being created.")
                .withShouldEndSession(true)
                .build();
    }

    private void triggerWriteExamLambda(String source, String canvasFileName, String subject, String quizName, String numQuest) {
        JSONObject payloadObject = new JSONObject();
        payloadObject.put("examSource", source);
        payloadObject.put("canvasFileName", canvasFileName);
        payloadObject.put("subject", subject);
        payloadObject.put("quizName", quizName);
        payloadObject.put("numQuest", numQuest);
        String payload = payloadObject.toString();

        AWSLambda client = AWSLambdaAsyncClient.builder().build();
        InvokeRequest writeExamRequest = new InvokeRequest();
        writeExamRequest.setInvocationType("Event");
        writeExamRequest.withFunctionName("test-write-quiz-lambda").withPayload(payload);
        client.invoke(writeExamRequest);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void customizeResponse(LexRequest request, LexResponse response) {
        response.getSessionAttributes().remove("answer");
    }
}
