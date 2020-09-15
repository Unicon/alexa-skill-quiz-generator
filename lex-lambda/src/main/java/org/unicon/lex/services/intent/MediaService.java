package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.ConfirmationStatus;
import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MediaMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.utils.MediaUtil;

import java.util.Map;

import static java.lang.String.format;
import static org.unicon.lex.services.LexResponseHelper.createCloseAction;
import static org.unicon.lex.services.LexResponseHelper.createPlainTextMessage;

public class MediaService extends AbstractLexService {

    public static final String NAME = "showMedia";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        Map<String, String> slots = request.getCurrentIntent().getSlots();
        log.error("Slots [{}]", slots);
        String subject = this.getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        String word = this.getValueFromSlotOrSessionOrInputTranscript(request, "term");
        AthenaService service = this.getAthenaService();
        if (service == null) {
            return new DefaultMessageContent("No database attached");
        }

        String html = null;
        String url = service.getMediaURL(subject, word);
        if (url != null) {
            try {
                html = MediaUtil.createMediaElement(url);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        MessageContent content;
        if (html == null) {
            content = new DefaultMessageContent(format("Sorry there is no media for '%s' in '%s'", word, subject));
        } else {
            content = new MediaMessageContent(html);
        }
        return content;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void customizeResponse(LexRequest request, LexResponse response) {
        String status = request.getCurrentIntent().getConfirmationStatus();
        boolean redirected = response.getSessionAttributes().get(INTENT_REDIRECT_NAME) != null;
        log.info("Confirmation Status: {}", status);
        if (ConfirmationStatus.DENIED.equals(status)) {
            createCloseAction(response, true, createPlainTextMessage("Okay, I'm here to help."));
        } else if (ConfirmationStatus.NONE.equals(status) && redirected) {
            response.getSessionAttributes().remove(INTENT_REDIRECT_NAME);
            createCloseAction(response, true, createPlainTextMessage("Sorry, I did not understand."));
        }
    }
}