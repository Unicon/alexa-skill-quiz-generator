package org.unicon.lex.model;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnrollmentMessageContent extends MessageContent {
    final Logger log = LogManager.getLogger(getClass());

    private List<String> subjects;
    public EnrollmentMessageContent(List<String> subjects) {
        this.subjects = subjects;
    }
    public List<String> getSubjects() {
        return this.subjects;
    }
}
