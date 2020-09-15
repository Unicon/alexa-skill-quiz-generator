package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class TopicState implements Comparable<TopicState> {

    @ToString.Exclude
    private Integer id;
    private CourseTopic topic;
    private CourseTopicState courseTopicState;

    @Override
    public int compareTo(TopicState topicState) {
        return this.getTopic().compareTo(topicState.getTopic());
    }

}
