package org.unicon.lex.services.external;

import java.util.List;

public interface AthenaService {
    List<String> getLdaAnswer(String subject, String word);

    List<String> getGlossaryAnswer(String subject, String word);

    List<String> getEnrollments(String userId);

    List<String> getQuizQuestions(String subject, String quizName, String numQuest);

    String getMediaURL(String subject, String word);
}
