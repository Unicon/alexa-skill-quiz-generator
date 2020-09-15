package org.unicon.lex.services;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.db.model.Content;
import org.unicon.lex.db.model.Course;

import com.github.chen0040.rl.learning.qlearn.QLearner;
import com.github.chen0040.rl.utils.IndexValue;

import lombok.Getter;
import lombok.Setter;

public class CourseAgentImpl implements CourseAgent {
    private final Logger log = LogManager.getLogger(getClass());
    private final Random random = new Random();

    @Getter @Setter private QLearner learner;
    @Getter @Setter private Course course;
    @Getter @Setter private List<Content> contents;

    public Content getContentForState(int stateId) {
        Set<Integer> contentAtState = getContentAtState(contents, stateId);
          IndexValue index = learner.selectAction(stateId, contentAtState);
          log.error("index [" + index.getIndex() + "], value [" + index.getValue() + "]");
          if (contents == null || contents.isEmpty()) {
              return null;
          }
          if (index.getIndex() < 0) {
              return contents.get(random.nextInt(contents.size()));
          }
          Content content = contents.stream()
                  .filter(curContent -> curContent.getId() == index.getIndex())
                  .findFirst().orElse(null);
          return content;
      }

    // TODO this could get sophisticated, say we want to set prerequisites.
    private Set<Integer> getContentAtState(List<Content> contents, int stateId) {
        Set<Integer> actionsAtState = new HashSet<>();
        // TODO use lambda syntax
        for (Content content : contents) {
            actionsAtState.add(content.getId());
        }
        return actionsAtState;
    }
}
