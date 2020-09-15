package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class CourseTopicState {

    @ToString.Exclude
    private Integer id;
    private String name;
    private Integer value;

}
