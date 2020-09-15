package org.unicon.lex.db.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Student {

    @ToString.Exclude
    private Integer id;
    private String userName;
    private String firstName;
    private String lastName;
}
