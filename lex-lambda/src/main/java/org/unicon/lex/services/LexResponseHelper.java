package org.unicon.lex.services;

import com.amazonaws.services.lexruntime.model.ResponseCard;
import org.unicon.lex.intents.response.LexDialogAction;
import org.unicon.lex.intents.response.LexMessage;
import org.unicon.lex.intents.response.LexResponse;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.unicon.lex.intents.response.LexDialogActionFulfillmentState.FAILED;
import static org.unicon.lex.intents.response.LexDialogActionFulfillmentState.FULFILLED;
import static org.unicon.lex.intents.response.LexDialogActionType.CLOSE;
import static org.unicon.lex.intents.response.LexDialogActionType.CONFIRM_INTENT;
import static org.unicon.lex.intents.response.LexDialogActionType.DELEGATE;
import static org.unicon.lex.intents.response.LexDialogActionType.ELICIT_INTENT;
import static org.unicon.lex.intents.response.LexDialogActionType.ELICIT_SLOT;
import static org.unicon.lex.intents.response.LexMessageContentType.CUSTOM_PAYLOAD;
import static org.unicon.lex.intents.response.LexMessageContentType.PLAIN_TEXT;
import static org.unicon.lex.intents.response.LexMessageContentType.SSML;

public class LexResponseHelper {

    private LexResponseHelper() {
    }

    public static LexMessage createMessage(String messageContent, String type) {
        LexMessage message = new LexMessage();
        message.setContentType(type);
        message.setContent(requireNonNull(messageContent));
        return message;
    }

    public static LexMessage createPlainTextMessage(String messageContent) {
        return createMessage(messageContent, PLAIN_TEXT);
    }

    public static LexMessage createSSMLMessage(String messageContent) {
        return createMessage(messageContent, SSML);
    }

    public static LexMessage createCustomMessage(String messageContent) {
        return createMessage(messageContent, CUSTOM_PAYLOAD);
    }

    /**
     * Close — Informs Amazon Lex not to expect a response from the user.
     * For example, "Your pizza order has been placed" does not require a response.
     * The message and responseCard fields are optional.
     * If you don't specify a message,
     * Amazon Lex uses the goodbye message or the follow-up message configured for the intent.
     */
    public static void createCloseAction(LexResponse response, boolean fulfilled, LexMessage message, ResponseCard card) {
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType(CLOSE);
        if (fulfilled) {
            dialogAction.setFulfillmentState(FULFILLED);
        } else {
            dialogAction.setFulfillmentState(FAILED);
        }
        dialogAction.setMessage(message);
        dialogAction.setResponseCard(card);
        response.setDialogAction(dialogAction);
    }

    public static void createCloseAction(LexResponse response, boolean fulfilled, LexMessage message) {
        createCloseAction(response, fulfilled, message, null);
    }

    public static void createCloseAction(LexResponse response, boolean fulfilled, ResponseCard card) {
        createCloseAction(response, fulfilled, null, card);
    }

    public static void createCloseAction(LexResponse response, boolean fulfilled) {
        createCloseAction(response, fulfilled, null, null);
    }

    /**
     * ConfirmIntent — Informs Amazon Lex that the user is expected to give a yes or no answer to confirm or deny the
     * current intent.
     * You must include the intentName and slots fields.
     * The slots field must contain an entry for each of the slots configured for the specified intent.
     * If the value of a slot is unknown, you must set it to null.
     * You must include the message field if the intent's confirmationPrompt field is null.
     * If you specify both the message field and the confirmationPrompt field,
     * the response includes the contents of the confirmationPrompt field.
     * The responseCard field is optional.
     */
    public static void createConfirmIntentAction(LexResponse response, String intentName, Map<String, String> slots,
                                                 LexMessage message, ResponseCard card) {
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType(CONFIRM_INTENT);
        dialogAction.setIntentName(intentName);
        dialogAction.setSlots(slots);
        dialogAction.setMessage(message);
        dialogAction.setResponseCard(card);
        response.setDialogAction(dialogAction);
    }

    public static void createConfirmIntentAction(LexResponse response, String intentName,
                                                 Map<String, String> slots, LexMessage message) {
        createConfirmIntentAction(response, intentName, slots, message, null);
    }


    public static void createConfirmIntentAction(LexResponse response, String intentName,
                                                 Map<String, String> slots, ResponseCard card) {
        createConfirmIntentAction(response, intentName, slots, null, card);
    }

    public static void createDelegateAction(LexResponse response, Map<String, String> slots) {
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType(DELEGATE);
        dialogAction.setSlots(slots);
        response.setDialogAction(dialogAction);
    }

    /**
     * ElicitIntent — Informs Amazon Lex that the user is expected to respond with an utterance that includes an intent.
     * For example, "I want a large pizza," which indicates the OrderPizzaIntent. The utterance "large,"
     * on the other hand, is not sufficient for Amazon Lex to infer the user's intent.
     * The message and responseCard fields are optional.
     * If you don't provide a message, Amazon Lex uses one of the bot's clarification prompts.
     */
    public static void createElicitIntentAction(LexResponse response, LexMessage message, ResponseCard card) {
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType(ELICIT_INTENT);
        dialogAction.setMessage(message);
        dialogAction.setResponseCard(card);
        response.setDialogAction(dialogAction);
    }

    public static void createElicitIntentAction(LexResponse response, LexMessage message) {
        createElicitIntentAction(response, message, null);
    }

    public static void createElicitIntentAction(LexResponse response, ResponseCard card) {
        createElicitIntentAction(response, null, card);
    }

    public static void createElicitIntentAction(LexResponse response) {
        LexMessage lexMessage = new LexMessage();
        lexMessage.setContent("How can I help you?");
        lexMessage.setContentType("PlainText");
        createElicitIntentAction(response, lexMessage, null);
    }

    /**
     * ElicitSlot — Informs Amazon Lex that the user is expected to provide a slot value in the response.
     * The intentName, slotToElicit, and slots fields are required. The slots field must include all of the slots
     * specified for the requested intent. The message and responseCard fields are optional. If you don't specify a
     * message, Amazon Lex uses one of the slot elicitation prompts configured for the slot.
     */
    public static void createElicitSlotAction(LexResponse response, String intentName, Map<String, String> slots,
                                              String slotToElicit, LexMessage message, ResponseCard card) {
        LexDialogAction dialogAction = new LexDialogAction();
        dialogAction.setType(ELICIT_SLOT);
        dialogAction.setIntentName(intentName);
        dialogAction.setSlots(slots);
        dialogAction.setSlotToElicit(slotToElicit);
        dialogAction.setMessage(message);
        dialogAction.setResponseCard(card);
        response.setDialogAction(dialogAction);
    }

    public static void createElicitSlotAction(LexResponse response, String intentName, Map<String, String> slots,
                                              String slotToElicit, ResponseCard card) {
        createElicitSlotAction(response, intentName, slots, slotToElicit, null, card);
    }

    public static void createElicitSlotAction(LexResponse response, String intentName, Map<String, String> slots,
                                              String slotToElicit, LexMessage message) {
        createElicitSlotAction(response, intentName, slots, slotToElicit, message, null);
    }

    public static void createElicitSlotAction(LexResponse response, String intentName, Map<String, String> slots,
                                              String slotToElicit) {
        createElicitSlotAction(response, intentName, slots, slotToElicit, null, null);
    }

}
