package org.unicon.lex.intents.response;

import com.amazonaws.services.lexruntime.model.ResponseCard;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LexDialogAction implements Serializable {

    private String type;
    @JsonInclude(NON_NULL)
    private String fulfillmentState;
    @JsonInclude(NON_NULL)
    private LexMessage message;
    @JsonInclude(NON_NULL)
    private String intentName;
    @JsonInclude(NON_NULL)
    private Map<String, String> slots;
    @JsonInclude(NON_NULL)
    private String slotToElicit;
    @JsonInclude(NON_NULL)
    private ResponseCard responseCard;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFulfillmentState() {
        return fulfillmentState;
    }

    public void setFulfillmentState(String fulfillmentState) {
        this.fulfillmentState = fulfillmentState;
    }

    public LexMessage getMessage() {
        return message;
    }

    public void setMessage(LexMessage message) {
        this.message = message;
    }

    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(String intentName) {
        this.intentName = intentName;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    public String getSlotToElicit() {
        return slotToElicit;
    }

    public void setSlotToElicit(String slotToElicit) {
        this.slotToElicit = slotToElicit;
    }

    public ResponseCard getResponseCard() {
        return responseCard;
    }

    public void setResponseCard(ResponseCard responseCard) {
        this.responseCard = responseCard;
    }

    @Override
    public String toString() {
        return "LexDialogAction{" +
                "type='" + type + '\'' +
                ", fulfillmentState='" + fulfillmentState + '\'' +
                ", message=" + message +
                ", intentName='" + intentName + '\'' +
                ", slots=" + slots +
                ", slotToElicit='" + slotToElicit + '\'' +
                ", responseCard=" + responseCard +
                '}';
    }
}
