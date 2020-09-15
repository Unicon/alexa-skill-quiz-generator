package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.GlossaryMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;

import java.util.List;
import java.util.Map;

import static org.unicon.lex.services.LexResponseHelper.createConfirmIntentAction;

public class GlossaryService extends AbstractLexService {
    public static final String NAME = "define";
    final Logger log = LogManager.getLogger(getClass());

    @Override
    public MessageContent getAnswer(LexRequest request) {
        Map<String, String> slots = request.getCurrentIntent().getSlots();
        log.error("Slots [" + slots + "]");
        String subject = this.getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        String word = this.getValueFromSlotOrSessionOrInputTranscript(request, "term");
        AthenaService service = this.getAthenaService();
        if (service == null) {
            return new DefaultMessageContent("No database attached");
        }
        List<String> definitions = service.getGlossaryAnswer(subject, word);
        if (hasMedia(subject, word)) {
            definitions.add("Would you like to see additional media?");
        }
        MessageContent result = new GlossaryMessageContent(subject, word, definitions);
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void customizeResponse(LexRequest request, LexResponse response) {
        if (hasMedia(getValueFromSlotOrSessionOrInputTranscript(request, "subject"),
                getValueFromSlotOrSessionOrInputTranscript(request, "term"))) {
            //the only valid slots for showMedia are "subject" and "term"
            createConfirmIntentAction(response, MediaService.NAME, copySlotData(response.getSessionAttributes(),
                    "subject", "term"),
                    response.getDialogAction().getMessage());
            response.getSessionAttributes().put(INTENT_REDIRECT_NAME, NAME);
        }
    }

    private boolean hasMedia(String subject, String word) {
        AthenaService service = getAthenaService();
        if (service != null) {
            return service.getMediaURL(subject, word) == null ? false : true;
        }
        return false;
    }

}
