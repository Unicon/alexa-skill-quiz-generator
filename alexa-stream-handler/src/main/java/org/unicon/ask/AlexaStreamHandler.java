package org.unicon.ask;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import org.unicon.ask.handlers.exception.CatchAllExceptionHandler;
import org.unicon.ask.handlers.request.AlexaLaunchRequestHandler;
import org.unicon.ask.handlers.request.QuizIntentHandler;
import org.unicon.ask.handlers.request.WriteQuizIntentHandler;
import org.unicon.ask.requestinterceptors.RequestLogger;
import org.unicon.ask.responseinterceptors.ResponseLogger;

public class AlexaStreamHandler extends SkillStreamHandler {
    private static Skill getSkill() {
        return Skills.standard()
                .addRequestInterceptor(new RequestLogger())
                .addRequestHandlers(
                        new AlexaLaunchRequestHandler(),
                        new WriteQuizIntentHandler(),
                        new QuizIntentHandler()
                )
                .addResponseInterceptor(new ResponseLogger())
                .addExceptionHandler(new CatchAllExceptionHandler())
                .build();
    }

    public AlexaStreamHandler() {
        super(getSkill());
    }


}
