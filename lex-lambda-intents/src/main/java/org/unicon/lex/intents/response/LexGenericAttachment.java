package org.unicon.lex.intents.response;

import java.util.List;

public class LexGenericAttachment {

    private String title;
    private String subTitle;
    private String imageUrl;
    private String attachmentLinkUrl;
    private List<LexResponseCardButton> buttons;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAttachmentLinkUrl() {
        return attachmentLinkUrl;
    }

    public void setAttachmentLinkUrl(String attachmentLinkUrl) {
        this.attachmentLinkUrl = attachmentLinkUrl;
    }

    public List<LexResponseCardButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<LexResponseCardButton> buttons) {
        this.buttons = buttons;
    }

    @Override
    public String toString() {
        return "LexGenericAttachment{" +
                "title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", attachmentLinkUrl='" + attachmentLinkUrl + '\'' +
                ", buttons=" + buttons +
                '}';
    }
}
