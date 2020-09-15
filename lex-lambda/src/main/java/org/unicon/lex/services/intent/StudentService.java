package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.db.model.Student;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.RDSService;

public class StudentService extends AbstractLexService {
    public static final String NAME = "student";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        RDSService rdsService = getRDSService();
        if (rdsService == null) {
            return new DefaultMessageContent("No Access to RDS");
        }
        String username = getValueFromSlotOrSessionOrInputTranscript(request, "username");
        Student student = rdsService.getStudentByUserName(username);
        if (student != null) {
            return new DefaultMessageContent(student.toString());
        }
        return new DefaultMessageContent("No results.");
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void customizeResponse(LexRequest request, LexResponse response) {
        response.getSessionAttributes().remove("username");
    }
}