package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Policy {

    private int id;
    private int courseId;
    private int state;
    private String content;
}
