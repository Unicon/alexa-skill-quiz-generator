package org.unicon.lex.intents.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LexBot {

    private String botName;
    private String botAlias;
    private String botVersion;

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getBotAlias() {
        return botAlias;
    }

    public void setBotAlias(String botAlias) {
        this.botAlias = botAlias;
    }

    public String getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(String botVersion) {
        this.botVersion = botVersion;
    }

    @Override
    public String toString() {
        return "LexBot{" +
                "botName='" + botName + '\'' +
                ", botAlias='" + botAlias + '\'' +
                ", botVersion='" + botVersion + '\'' +
                '}';
    }
}
