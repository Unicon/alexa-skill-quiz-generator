package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@ToString
@Data
public class Enrollment {

    @ToString.Exclude
    private Integer id;
    private Student student;
    private Course course;
    private Set<TopicState> topicStates;
    private Integer state;
}
