# Quiz Generator Alexa Skill

## Overview
This repository includes all of the projects for creating the alexa-stream-handler Lambda and the write-quiz-lambda Lambda,
the two Lambdas needed for the Quiz Generator Alexa Skill project. The Alexa Stream Handler uses the services provided
by the lex-lambda project to trigger the write-quiz-lambda Lambda, which will pull the file from Canvas and create a text
document quiz from it.

## Build
```
mvn clean install
```

## AWS Lambdas
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
    - set the handler appropriately
