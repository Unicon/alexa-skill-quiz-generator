# Alexa Stream Handler

## Overview
This project provides the AlexaStreamHandler wrapper that will trigger the appropriate service inside the Lex Lambda project (lex-lambda) for the Quiz Alexa Skill.

## Build the Jar
This application has dependencies that must be available before running.  None of these dependencies are available from a Nexus Maven repository, so you need to download the source, build them locally, and install them into your local Maven repository before running this application.

### Dependencies
 * lex-lambda
 * lex-lambda-intents
 * lex-parent
 * lex-utils
 * pdfbox (Only needed for write-quiz-lambda)
 * pdf-parser (Only needed for write-quiz-lambda)
 * write-quiz-lambda (Only needed for write-quiz-lambda)

## Create the Lambda function for Alexa from the AWS Console
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
    - set the handler to 'org.unicon.ask.AlexaStreamHandler'
7. Copy the ARN for the Alexa Skill

## Connect Lambda function to Alexa Skill from the Alexa Developer Console
1. Go to the Alexa Developer Console: https://developer.amazon.com/alexa/console/ask
2. Click on the skill
3. Click on Endpoint
4. Paste the ARN for the Lambda function in the "Default Region" box