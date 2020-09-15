package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString
public class Course {

    @ToString.Exclude
    private Integer id;
    private String courseName;
    private String url;
    private Set<CourseTopic> topics;
}
