package org.unicon.lex.services.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazonaws.services.lexruntime.model.Button;
import com.amazonaws.services.lexruntime.model.GenericAttachment;
import com.amazonaws.services.lexruntime.model.ResponseCard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.model.QuizMessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.utils.Base64Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.unicon.lex.intents.response.LexResponseCardContentType.AWS_GENERIC_CARD;
import static org.unicon.lex.services.LexResponseHelper.createCloseAction;
import static org.unicon.lex.services.LexResponseHelper.createElicitSlotAction;
import static org.unicon.lex.services.LexResponseHelper.createPlainTextMessage;

public class QuizService extends AbstractLexService {
    public static final String NAME = "quiz";
    public static final String DEFINITION_TYPE = "definition";
    public static final String MC_TYPE = "multiple-choice";
    final Logger log = LogManager.getLogger(getClass());
    private List<String> singleResponseSlots = Arrays.asList("subject", "quizName", "numQuest");
    private ObjectMapper mapper = new ObjectMapper();
    private List<String> quizQuestions;
    private Map<String, String> slotNames = Stream.of(new Object[][] {
            { "subject", "subject" },
            { "quizName", "quiz name or chapter" },
            { "numQuest", "number of questions you want to include" }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));


    @Override
    public List<String> validate(LexRequest request) {
        String subject = getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        String quizName = getValueFromSlotOrSessionOrInputTranscript(request, "quizName");
        String numQuest = getValueFromSlotOrSessionOrInputTranscript(request, "numQuest");
        String answer = getValueFromSlotOrSessionOrInputTranscript(request, "answer");
        return validate(subject, quizName, numQuest, answer);
    }

    public List<String> validate(Intent intent) {
        String subject = intent.getSlots().get("subject").getValue();
        String quizName = intent.getSlots().get("quizName").getValue();
        String numQuest = intent.getSlots().get("numQuest").getValue();
        String answer = intent.getSlots().get("answer").getValue();
        return validate(subject, quizName, numQuest, answer);
    }

    private List<String> validate(String subject, String quizName, String numQuest, String answer) {
        List<String> results = new ArrayList<>();
        // Make sure that the slots are elicited in this order.  We want to handle
        // answer at the very end, after the other slots are filled
        if (StringUtils.isBlank(subject)) {
            results.add("subject");
            return results;
        }

        if (StringUtils.isBlank(quizName)) {
            results.add("quizName");
            return results;
        }

        if (StringUtils.isBlank(numQuest)) {
            results.add("numQuest");
            return results;
        }

        if (StringUtils.isBlank(answer)) {
            results.add("answer");
            return results;
        }
        return results;
    }

    @Override
    public LexResponse buildValidationResponse(LexRequest request, List<String> missingSlots) {
        String missingSlot = missingSlots.get(0);
        if (singleResponseSlots.contains(missingSlot)) {
            log.error("answer buildResponseForMissingSlot");
            LexResponse response = buildResponseForMissingSlot(request, missingSlot, slotNames.get(missingSlot));
            String encodedResponse = Base64Utils.encode(response);
            response.getSessionAttributes().put("lexResponse",  encodedResponse);
            return response;
        } else if ("answer".equals(missingSlot)) {
            log.error("answer buildResponseCardAnswers");
            LexResponse response = buildResponseCardAnswers(request);
            return response;
        }
        LexResponse response = buildResponseForMissingSlot(request, missingSlot, missingSlot);
        String encodedResponse = Base64Utils.encode(response);
        response.getSessionAttributes().put("lexResponse",  encodedResponse);
        return response;
    }

    public Optional<Response> buildValidationResponse(HandlerInput handlerInput, IntentRequest intentRequest, List<String> missingSlots) {
        String missingSlot = missingSlots.get(0);
        if (singleResponseSlots.contains(missingSlot)) {
            log.error("answer buildResponseForMissingSlot");
            return buildResponseForMissingSlot(handlerInput, missingSlot, slotNames.get(missingSlot));
        } else if ("answer".equals(missingSlot)) {
            log.error("answer buildResponseCardAnswers");
            return buildResponseCardAnswers(handlerInput, intentRequest, "");
        }
        return buildResponseForMissingSlot(handlerInput, missingSlot, missingSlot);
    }

    private GenericAttachment buildChoices(Map<String, Object> quiz) {
        String type = Objects.toString(quiz.get("type"), "");
        List<String> choices = (List<String>) quiz.get("choices");
        if (choices.isEmpty()) {
            log.error("no choices in quiz [{}]", quiz);
        }

        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            String choice = choices.get(i);

            Button button = new Button();
            button.setText(choice);
            if (StringUtils.equals(type, DEFINITION_TYPE)) {
                button.setValue(choice);
            } else if (StringUtils.equals(type, MC_TYPE)) {
                String letter = Character.toString(choice.charAt(0));
                button.setValue(letter);
            }
            buttons.add(button);
        }
        GenericAttachment attachment = new GenericAttachment();
        attachment.setTitle("Choices");
        attachment.setButtons(buttons);
        return attachment;
    }

    private Optional<Response> buildResponseCardAnswers(HandlerInput handlerInput, IntentRequest intentRequest, String prevResult) {
        Intent intent = intentRequest.getIntent();
        String subject = sanitized(intent.getSlots().get("subject").getValue());
        String quizName = sanitized(intent.getSlots().get("quizName").getValue());
        String numQuest = intent.getSlots().get("numQuest").getValue();
        int itNum = getItNum(handlerInput);

        Map<String, Object> quiz = getQuiz(subject, quizName, numQuest, itNum);
        String question = getQuestion(subject, quiz);
        String type = Objects.toString(quiz.get("type"), "");
        List<String> choices = (List<String>) quiz.get("choices");
        String response = StringUtils.isNotBlank(prevResult) ?
                prevResult + " Here is your next question. " :
                StringUtils.equals(type, MC_TYPE) ?
                        "This will be a multiple choice quiz. Please answer each question with the letter of your answer choice. " :
                        "This will be a vocabulary quiz. Please answer each question with the word that matches best with the given definition. ";

        if (StringUtils.equalsIgnoreCase(type, DEFINITION_TYPE)) {
            response =  response + "Which of the following words means: " + question + ". The choices are: ";
            for (int i = 0; i < choices.size() - 1; i++) {
                response = response + choices.get(i) + ", ";
            }
            response = response + choices.get(choices.size() -1) + ".";
        } else if (StringUtils.equalsIgnoreCase(type, MC_TYPE)) {
            response = response + question + "\n";
            for (int i = 0; i < choices.size(); i++) {
                if (StringUtils.isNotBlank(choices.get(i))) {
                    response = response + choices.get(i) + "\n";
                }
            }
        }

        return handlerInput.getResponseBuilder()
                .withSpeech(response)
                .addDirective(ElicitSlotDirective.builder().withSlotToElicit("answer").build())
                .build();
    }

    private LexResponse buildResponseCardAnswers(LexRequest request) {
        String subject = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "subject"));
        String quizName = sanitized(getValueFromSlotOrSessionOrInputTranscript(request, "quizName"));
        String numQuest = getValueFromSlotOrSessionOrInputTranscript(request, "numQuest");
        int itNum = getItNum(request);

        Map<String, Object> quiz = getQuiz(subject, quizName, numQuest, itNum);

        String question = getQuestion(subject, quiz);

        LexResponse response = new LexResponse();
        response.getSessionAttributes().putAll(request.getSessionAttributes());
        response.getSessionAttributes().remove("lexResponse");

        GenericAttachment attachment = this.buildChoices(quiz);
        ResponseCard card = new ResponseCard();
        card.setVersion("1");
        card.setContentType(AWS_GENERIC_CARD);
        card.setGenericAttachments(Arrays.asList(attachment));
        createElicitSlotAction(response, request.getCurrentIntent().getName(),
                request.getCurrentIntent().getSlots(), "answer", createPlainTextMessage(question), card);
        return response;
    }

    private String getQuestion(String subject, Map<String, Object> quiz) {
        AthenaService service = this.getAthenaService();
        String term = (String) quiz.get("answer");

        String type = Objects.toString(quiz.get("type"), "");
        if (StringUtils.isBlank(type)) {
            log.error("quiz type is blank");
        }

        String question = "";
        if (StringUtils.equals(type, DEFINITION_TYPE)) {
            List<String> definitions = service.getGlossaryAnswer(subject, term);
            question = String.join(", ", definitions);
        } else if (StringUtils.equals(type, MC_TYPE)) {
            question = Objects.toString(quiz.get("question"), "");
        }

        if (StringUtils.isBlank(question)) {
            log.error("question is blank");
        }

        return question;
    }

    private Map<String, Object> getQuiz(String subject, String quizName, String numQuest, int itNum) {
        AthenaService service = this.getAthenaService();
        if (quizQuestions == null) {
            log.error("quizQuestions was null");
            quizQuestions = service.getQuizQuestions(subject, quizName, numQuest);
            log.error("Size of quiz questions: " + quizQuestions.size());
        }

        return getQuizMap(quizQuestions.get(itNum));
    }

    private int getItNum(LexRequest request) {
        String itNumString = request.getSessionAttributes().get("itNum");
        if (itNumString == null) {
            itNumString = "0";
            request.getSessionAttributes().put("itNum", itNumString);
        }
        return Integer.parseInt(itNumString);
    }

    private int getItNum(HandlerInput handlerInput) {
        String itNumString = (String) handlerInput.getRequestEnvelope().getSession().getAttributes().get("itNum");
        log.debug("itNumString: " + itNumString);
        if (itNumString == null) {
            itNumString = "0";
            handlerInput.getRequestEnvelope().getSession().getAttributes().put("itNum", itNumString);
        }

        return Integer.parseInt(itNumString);
    }

    private Map<String, Object> getQuizMap(String quizStr) {
        Map<String, Object> quizJson = new HashMap<>();
        try {
            quizJson = mapper.readValue(quizStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
        log.debug("returning quiz with choices: " + quizJson.get("choices"));
        return quizJson;
    }

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String studentAnswer = request.getCurrentIntent().getSlots().get("answer");
        return new QuizMessageContent(getAnswer(studentAnswer, getItNum(request)));
    }

    private String getAnswer(String studentAnswer, int itNum) {
        AthenaService service = this.getAthenaService();
        if (service == null) {
            return "No database attached";
        }
        Map<String, Object> quiz = getQuizMap(quizQuestions.get(itNum));
        String answer = (String) quiz.get("answer");
        return StringUtils.equalsIgnoreCase(answer, studentAnswer) ? "Your answer is correct!" : "Your answer is incorrect. The correct answer is " + answer + ".";
    }

    public Optional<Response> getResponse(HandlerInput handlerInput, IntentRequest intentRequest) {
        log.error("inside alexa QuizService getResponse");
        String studentAnswer = intentRequest.getIntent().getSlots().get("answer").getValue();
        int itNum = getItNum(handlerInput);
        String content = getAnswer(studentAnswer, itNum);

        // TODO: Fix to send progressive directive with results before asking next question.
//        SpeakDirective speakDirective = SpeakDirective.builder().withSpeech(content).build();
//        DirectiveServiceClient directiveServiceClient = handlerInput.getServiceClientFactory().getDirectiveService();
//        directiveServiceClient.enqueue(SendDirectiveRequest.builder().withDirective(speakDirective).build());

        // increment iteration number
        itNum = itNum + 1;
        handlerInput.getRequestEnvelope().getSession().getAttributes().replace("itNum", String.valueOf(itNum));

        // carry all session attribute data forward, as well as all slot data
        // in session attributes.  This way, the slots do not need to be
        // requeried
        if (quizQuestions.size() > itNum) { // if we haven't asked all questions yet, keep all slot data except answer
            return buildResponseCardAnswers(handlerInput, intentRequest, content);
        }

        return handlerInput.getResponseBuilder()
                .withSpeech(content + " Thanks for quizzing with me. Let me know if you want to be quizzed again. Goodbye.")
                .withShouldEndSession(true)
                .build();
    }

    @Override
    public LexResponse getResponse(LexRequest request) {
        log.error("Inside QuizService getResponse");
        MessageContent content = this.getAnswer(request);
        String answer = content.toString();
        log.debug(answer);

        LexResponse response = new LexResponse();

        // increment iteration number
        String itNumString = request.getSessionAttributes().get("itNum");
        int itNum = Integer.parseInt(itNumString) + 1;
        request.getSessionAttributes().replace("itNum", String.valueOf(itNum));

        // carry all session attribute data forward, as well as all slot data
        // in session attributes.  This way, the slots do not need to be
        // requeried
        if (quizQuestions.size() > itNum) { // if we haven't asked all questions yet, keep all slot data except answer
            response.getSessionAttributes().putAll(request.getSessionAttributes());
            for (Map.Entry<String, String> entry : request.getCurrentIntent().getSlots().entrySet()) {
                log.error("entry [" + entry.getKey() + ":" + entry.getValue() + "]");
                if (!StringUtils.isBlank(entry.getValue()) && !StringUtils.equals(entry.getKey(), "answer")) {
                    response.getSessionAttributes().put(entry.getKey(), entry.getValue());
                }
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
    public String getName() {
        return NAME;
    }

    @Override
    public void customizeResponse(LexRequest request, LexResponse response) {
        response.getSessionAttributes().remove("answer");
    }
}
