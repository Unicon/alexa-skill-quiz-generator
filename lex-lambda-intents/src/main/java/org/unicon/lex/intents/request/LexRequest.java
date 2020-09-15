package org.unicon.lex.intents.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LexRequest {

    private LexIntent currentIntent;
    private LexBot bot;
    private String userId;
    private String inputTranscript;
    private String invocationSource;
    private String outputDialogMode;
    private String messageVersion;
    private Map<String, String> sessionAttributes;
    private Map<String, String> requestAttributes;

    public LexRequest() {
        currentIntent = new LexIntent();
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
    }

    public LexIntent getCurrentIntent() {
        return currentIntent;
    }

    public void setCurrentIntent(LexIntent currentIntent) {
        this.currentIntent = currentIntent;
    }

    public LexBot getBot() {
        return bot;
    }

    public void setBot(LexBot bot) {
        this.bot = bot;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInputTranscript() {
        return inputTranscript;
    }

    public void setInputTranscript(String inputTranscript) {
        this.inputTranscript = inputTranscript;
    }

    public String getInvocationSource() {
        return invocationSource;
    }

    public void setInvocationSource(String invocationSource) {
        this.invocationSource = invocationSource;
    }

    public String getOutputDialogMode() {
        return outputDialogMode;
    }

    public void setOutputDialogMode(String outputDialogMode) {
        this.outputDialogMode = outputDialogMode;
    }

    public String getMessageVersion() {
        return messageVersion;
    }

    public void setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
    }

    public Map<String, String> getSessionAttributes() {
        return sessionAttributes;
    }

    public void setSessionAttributes(Map<String, String> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public Map<String, String> getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(Map<String, String> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    @Override
    public String toString() {
        return "LexRequest{" +
                "currentIntent=" + currentIntent +
                ", bot='" + bot + '\'' +
                ", userId='" + userId + '\'' +
                ", inputTranscript='" + inputTranscript + '\'' +
                ", invocationSource=" + invocationSource +
                ", outputDialogMode=" + outputDialogMode +
                ", messageVersion='" + messageVersion + '\'' +
                ", sessionAttributes=" + sessionAttributes +
                ", requestAttributes=" + requestAttributes +
                '}';
    }
}
