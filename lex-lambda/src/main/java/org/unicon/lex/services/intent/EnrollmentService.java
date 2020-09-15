package org.unicon.lex.services.intent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.EnrollmentMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;

import org.unicon.lex.intents.request.LexRequest;

public class EnrollmentService extends AbstractLexService {
    public static final String NAME = "enrollment";
    final Logger log = LogManager.getLogger(getClass());

    @Override
    public MessageContent getAnswer(LexRequest request) {
        AthenaService service = this.getAthenaService();
        if (service == null) {
            return new DefaultMessageContent("No database attached");
        }
        String userId = request.getUserId();
        return new EnrollmentMessageContent(service.getEnrollments(userId));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
