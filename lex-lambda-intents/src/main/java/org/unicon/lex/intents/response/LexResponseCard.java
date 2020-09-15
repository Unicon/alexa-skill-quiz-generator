package org.unicon.lex.intents.response;

import java.util.List;

public class LexResponseCard {

    private String version;
    private String contentType;
    private List<LexGenericAttachment> genericAttachments;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setGenericAttachments(List<LexGenericAttachment> genericAttachments) {
        this.genericAttachments = genericAttachments;
    }

    public List<LexGenericAttachment> getGenericAttachments() {
        return genericAttachments;
    }

    @Override
    public String toString() {
        return "LexResponseCard{" +
                "version='" + version + '\'' +
                ", contentType='" + contentType + '\'' +
                ", genericAttachments=" + genericAttachments +
                '}';
    }
}
