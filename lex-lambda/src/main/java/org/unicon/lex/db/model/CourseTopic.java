package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CourseTopic implements Comparable<CourseTopic> {

    @ToString.Exclude
    private Integer id;
    private String name;
    private Integer topicorder;

    @Override
    public int compareTo(CourseTopic courseTopic) {
        return this.getTopicorder() - courseTopic.getTopicorder();
    }
}
