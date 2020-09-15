package org.unicon.lex.services.intent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.db.model.Content;
import org.unicon.lex.db.model.Course;
import org.unicon.lex.db.model.Enrollment;
import org.unicon.lex.db.model.Student;
import org.unicon.lex.model.DefaultMessageContent;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.model.RecommendMessageContent;
import org.unicon.lex.services.CourseAgent;
import org.unicon.lex.services.CourseAgentImpl;
import org.unicon.lex.services.external.KinesisEvent;

import com.amazonaws.util.StringUtils;
import com.github.chen0040.rl.learning.qlearn.QLearner;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;

public class RecommendService extends AbstractLexService {
    private final Logger log = LogManager.getLogger(getClass());
    public static final String NAME = "recommend";

    private final Map<String, CourseAgent> courseAgents = new HashMap<>();

    @Override
    public MessageContent getAnswer(LexRequest request) {
        String userId = request.getUserId();
        String subject = this.getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        List<Enrollment> enrollments = getRDSService().getEnrollmentsByStudentAndCourse(userId, subject);
        Enrollment enrollment = enrollments.get(0);

        CourseAgent agent = this.getCourseAgent(subject);
        int stateId = enrollment.getState();
        Content content = agent.getContentForState(stateId);
        if (content != null) {
            String message = content.getName();
            String url = content.getUrl();
            return new RecommendMessageContent(message, url, content.getId());
        };
        return new DefaultMessageContent("No available content found");
    }

    private CourseAgent getCourseAgent(String courseName) {
        CourseAgent agent = courseAgents.get(courseName);
        if (agent == null) {
            agent = buildNewAgent(courseName);
            courseAgents.put(courseName, agent);
        }
        return agent;
    }

    private CourseAgent buildNewAgent(String courseName) {
        CourseAgentImpl agent = new CourseAgentImpl();

        Course course = getRDSService().getCourseByName(courseName);
        List<Content> contents = getRDSService().getContentByCoursename(courseName);
        String json = getS3Service().getCourseModel(courseName);

        QLearner learner = null;
        if (!StringUtils.isNullOrEmpty(json)) {
            learner = QLearner.fromJson(json);
        } else {
            int numberOfTopics = course.getTopics().size();
            int numberOfStates = 4;
            int stateCount = (int) Math.pow(numberOfTopics, numberOfStates);

            int contentCount = contents.size();
            learner = new QLearner(stateCount, contentCount);
        }
        agent.setLearner(learner);
        agent.setCourse(course);
        agent.setContents(contents);
        return agent;
    }

    @Override
    public void publishToKinesis(LexRequest request, LexResponse response, MessageContent messageContent) {
        if (!(messageContent instanceof RecommendMessageContent)) {
            log.error("Attempted to publish non-RecommendMessageContent from RecommendService");
            return;
        }
        RecommendMessageContent recommendContent = (RecommendMessageContent) messageContent;

        KinesisEvent kinesisEvent = new KinesisEvent();
        kinesisEvent.setEventType(getName());
        kinesisEvent.setEventTimestamp(new Date());
        Map<String, Object> after = new HashMap<>();
        String userId = request.getUserId();
        Student student = this.getRDSService().getStudentByUserName(userId);
        String subject = this.getValueFromSlotOrSessionOrInputTranscript(request, "subject");
        Course course = this.getRDSService().getCourseByName(subject);
        Content content = this.getRDSService().getContentById(recommendContent.getContentId());
        after.put("student", student);
        after.put("course",  course);
        after.put("content", content);
        kinesisEvent.setAfter(after);
        getKinesisService().publish(kinesisEvent);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
