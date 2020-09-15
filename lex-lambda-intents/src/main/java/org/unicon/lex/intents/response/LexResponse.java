package org.unicon.lex.intents.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LexResponse implements Serializable {

    private Map<String, String> sessionAttributes;
    private LexDialogAction dialogAction;

    public LexResponse() {
        sessionAttributes = new HashMap<>();
    }

    public Map<String, String> getSessionAttributes() {
        return sessionAttributes;
    }

    public void setSessionAttributes(Map<String, String> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public LexDialogAction getDialogAction() {
        return dialogAction;
    }

    public void setDialogAction(LexDialogAction dialogAction) {
        this.dialogAction = dialogAction;
    }

    @Override
    public String toString() {
        return "LexResponse{" +
                "sessionAttributes=" + sessionAttributes +
                ", dialogAction=" + dialogAction +
                '}';
    }
}
