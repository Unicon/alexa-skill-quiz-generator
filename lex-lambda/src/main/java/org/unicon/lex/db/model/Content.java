package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Content {
    private Integer id;
    private Integer courseId;
    private String name;
    private String url;
}
