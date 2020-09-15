package org.unicon.lex.services;

import java.util.List;

import org.unicon.lex.db.model.Content;

public interface CourseAgent {

    List<Content> getContents();
    Content getContentForState(int stateId);

}
