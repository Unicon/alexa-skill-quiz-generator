package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.db.model.Course;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.RDSService;

public class CourseService extends AbstractLexService {
    public static final String NAME = "course";

    @Override
    public MessageContent getAnswer(LexRequest request) {
        RDSService rdsService = getRDSService();
        if (rdsService == null) {
            return new DefaultMessageContent("No Access to RDS");
        }
        String subject = getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        Course course = rdsService.getCourseByName(subject);
        if (course != null) {
            return new DefaultMessageContent(course.toString());
        }
        return new DefaultMessageContent("No results.");
    }

    @Override
    public String getName() {
        return NAME;
    }

}
