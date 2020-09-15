package org.unicon.lex.services.intent;

import org.unicon.lex.intents.request.LexRequest;
import org.unicon.lex.intents.response.LexResponse;
import org.unicon.lex.model.MessageContent;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.services.external.CanvasService;
import org.unicon.lex.services.external.KinesisService;
import org.unicon.lex.services.external.RDSService;
import org.unicon.lex.services.external.S3Service;

import java.util.List;

public interface LexService {

    void setAthenaService(AthenaService athenaService);

    void setRDSService(RDSService rdsService);

    void setS3Service(S3Service s3Service);

    void setKinesisService(KinesisService kinesisService);

    void setCanvasService(CanvasService canvasService);

    /**
     * getResponse() returns a LexResponse with the result of the user's comments
     * in the chat.  This function calls getAnswer() retrieve the needed data and
     * then formats the response for the chatbot.
     * @param request
     * @return a LexResponse that fulfills the request, calling any necessary
     * external datasources to provide the answer to the user's chat comment
     */
    LexResponse getResponse(LexRequest request);

    /**
     * getAnswer() reads the data from the LexRequest and queries any external
     * datasources necessary to retrieve the answer.  This function is called by
     * getResponse().
     * @param request
     * @return the MessageContent answer to a question whose parameters are in the LexRequest
     */
    MessageContent getAnswer(LexRequest request);

    /**
     * validate() looks in the LexRequest for all slots passed and makes sure
     * that each slot either has a value or that the sessionAttrbutes has a value
     * for that slot
     * @param request
     * @return a list of slot names that do not have a value in either the slots
     * structure or the sessionAttributes structure
     */
    List<String> validate(LexRequest request);

    /**
     *
     * @param request
     * @param missingSlots
     * @return a LexResponse that the chatbot will use to prompt for missing information
     */
    LexResponse buildValidationResponse(LexRequest request, List<String> missingSlots);

    /**
     *
     * @return the intentName that this service handles
     */
    String getName();
}
