package org.unicon.lex.model;

import lombok.Getter;

public class RecommendMessageContent extends MessageContent {

    @Getter private String recommendation;
    @Getter private String url;
    @Getter private int contentId;

    public RecommendMessageContent(String recommendation, String url, int contentId) {
        this.recommendation = recommendation;
        this.url = url;
        this.contentId = contentId;
    }
}
