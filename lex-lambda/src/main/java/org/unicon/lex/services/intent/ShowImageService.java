package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MediaMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.utils.MediaUtil;
import static org.unicon.lex.utils.MediaUtil.Type.IMAGE;

import java.util.Map;

import static java.lang.String.format;

public class ShowImageService extends AbstractLexService {

    public static final String NAME = "showImage";

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
                html = MediaUtil.createMediaElement(url, IMAGE);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        MessageContent content;
        if (html == null) {
            content = new DefaultMessageContent(format("Sorry there is no image for '%s' in '%s'", word, subject));
        } else {
            content = new MediaMessageContent(html);
        }
        return content;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
