# Lex Lambda

## Overview
Lex Lambda is the source that provides the DialogCodeHook and FulfillmentCodeHook
lambda functions available to any intent on an AWS Lex Chatbot.
Lex Lambda also provides the WriteQuizService and QuizService classes for the Alexa Stream Handler. While the QuizService
returns an existing quiz for the user to answer via the device, the WriteQuizService triggers the write-quiz-lambda to create
a text document quiz in Canvas from a pdf document in Canvas. An AWS Lambda of this project is not needed for the Alexa Skill
as this project is merely a dependency of the alexa-stream-handler project.

## Build the Jar
This application has dependencies that must be available before running.  None of these dependencies are available from a Nexus Maven repository, so you need to download the source, build them locally, and install them into your local Maven repository before running this application

### Dependencies
 * lex-lambda-intents
 * lex-parent
 * lex-utils
 * pdfbox (Only needed for write-exam-lambda)
 * pdf-parser (Only needed for write-exam-lambda)
 * write-exam-lambda (Only needed for write-exam-lambda)

## Create the Lambda function for Lex from the AWS Console
The jar that you just created must be deployed to an AWS Lambda.  Follow the instructions below to create the Lambda function and deploy the jar.

To create the AWS Lambda from the console:

1. Go to the AWS Lambda console
2. Press the Create function button
3. Choose Author from Scratch
4. Fill out the form
    - Name something meaningful to you
    - Runtime Java 11
    - Role Choose an existing role
    - Existing Role mdg-lambda-glue (once AWS security becomes important, create a role with better limits on its capabilities).
5. Press the Create Function button
6. Under Function Code
    - Code entry type is Upload a .zip or jar
    - Upload the jar file you've already created.
    - set the handler to 'org.unicon.lex.App::handleRequest'

## Features

* handleRequest
* Class Loader
* Databases - Postgres (AWS RDS) and AWS Glue/Athena
* Kinesis
* LexRequest and LexResponse
* SessionAttributes
* AbstractLexService and Intents

#### handleRequest
The main method handleRequests is located in the App.java class.  It basically is reading in an object of type LexRequest, which is a POJO that maps to the AWS Lex JSON structure for Lex Requests.  The main processing loop works in two parts.

1. Examine the slots that have data for the given intent.  If any slots are not filled, send back an ElicitSlot LexResponse to get the missing data.  This is the DialogHookLambda processing.
1. Once all of slots for the intent are filled, call the relevant service for the intent and build a LexResponse that has all of the expected data.  This is the FulfillmentHookLambda processing.

#### Class Loader
I wanted to make it as simple as possible to create new services.  This application doesn't have any Spring annotation or application configuration support, so I wrote a utility called ClassUtils that runs during the DefaultServiceFactory startup.  ClassUtils scans the classpath for any class that implements the LexService interface and isn't abstract.  It then registers each of these classes with the DefaultServiceFactory automatically.  This way, the next developer can simply create a class that either implements LexService or extends AbstractLexService, and they won't need to do anything else to use it.

#### Databases - Postgres (AWS RDS) and AWS Glue/Athena
When this project started, the goal was to use AWS Glue as the datasource, based on AWS S3 CSV files.  AWS Athena would be used by the Intent services to read from the database.  Unfortunately, the accompanying Spring Boot application wasn't able to use AWS Glue/Athena due to assumed roles issues from Cognito.  Therefore you will see that some services use Glue/Athena and others use JDBC to access Postgres in AWS RDS.  From a scalability issue, using Postgres is probably a better long-term solution.  However, as a proof of concept, I had no performance issues using Glue/Athena, and it was pretty simple to use.  I'd suggest eliminating Glue/Athena at some point, but that's not critical.  The only real downside at this point is that the Lambda jar is getting large.  Removing Athena would save some space.

#### Kinesis
A Kinesis firehose was added to this basically as a proof of concept.  We have two applications that currently write to the firehose.  When a user requests the chatbot to recommend content, the recommendation is published to the Kinesis firehose.  When the user takes an assessment from the Spring Boot application, the result is also published.  The purpose is in support of building a better Reinforcement Learning (RL) model.  The before state, the content, and the after state are needed to build the model, so we need to capture that information.  For details on how that actually, happens, refer to the rl-lambda and the rl-modeler projects.

The Kinesis firehose can obviously support other types of messages.  At this point, only the RecommendService uses it.

#### LexRequest and LexResponse
We created specific POJOs for these two JSON structures, to make it easier to manipulate.  They are currently in the lex-lambda-intents project because at one point we had another project that used the same data types.

#### SessionAttributes
During the DialogHookLambda phase (mentioned above), I said that we verify that all slots required by an intent have the needed data.  In order to address a limitation we identified with AWS Lex's ability to identify multi-word terms, we heavily leverage SessionAttributes.

If the AWS Lex utterance maps the intent correctly but can't map the slot correctly, it will leave the slot blank.  We've configured the service to look in three places for the slot data.  If the Slot is blank, it looks in the SessionAttribute named for the slot.  If that is blank, it looks in the SessionAttribute for the last LexResponse sent.  If that LexResponse is an ElicitSlot for the same slot we're checking, the program checks the InputTranscript field and uses that for the slot data.  The InputTranscript field contains all of the text that the user sent to Lex, even if it is unable to map successfully to a slot.

It is possible that we simply need to create more Utterances so that the AWS Lex parser gets a better idea of how to recognize multiple word terms.  It is also possible that the AWS Lex parser will improve over time, making this method obsolete.

#### AbstractLexService and Intents
The AWS Lex Chatbot can support multiple Intents.  I made an early design decision that each intent would be processed by its own service.  This way, the main processing loop would look identical for each intent being processed.  Quite a bit of code has been consolidated into AbstractLexService since they services need a very similar workflow.

First, the service verifies it has the information it needs, which is part of the DialogHookLambda.  Then, the service needs to generate a valid LexResponse for the FulfillmentHookLambda.  Each service needs opportunities to create custom behavior, and AbstractLexService provides a few places to handle that functionality.

* validate() handles the slot data validation, and that code is identical for all services.
* buildValidationResponse() handles what to do if slot data is missing.  That code is also identical for all services.
* getResponse() is responsible for building the actual LexResponse, and it has a couple of obvious places to handle custom processing.
* getAnswer() is an abstract method that must be overridden.  It actually does the heavy lifting, calling any external systems or databases if needed.
* customizeResponse() is a stub method that gives the programmer the opportunity to make changes to the LexResponse before returning it.
* publishToKinesis() is a stub method that gives the programmer the opportunity to publish to the Kinesis firehose if desired.
* getName() is an abstract method that must be overridden.  It contains the name of the AWS Lex Intent that it handles.  Currently each service only handles one intent.  That might be something to change in the future.

If you look at the GreetingService for example, you'll get a sense of how easy it is to write a service that renders static content.  Check out the LdaService to see a fairly simple service that reads data from an AWS Glue/Athena repository.  Note, here we do need to call an external database service, so while this service is fairly small, other services may be called in to do heavier lifting.
