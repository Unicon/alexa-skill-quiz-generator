package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.LdaMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;

import java.util.Map;

public class LdaService extends AbstractLexService {
    public static final String NAME = "findRelated";

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
        return new LdaMessageContent(service.getLdaAnswer(subject, word));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
