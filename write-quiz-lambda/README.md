This lambda requires you to first `mvn clean install`:
 - lex-parent
 - pdf-parser

This lambda (write-exam-lambda) is invoked by the lex-lambda project (mdg-lambda-handler) in its WriteExamService.
In the request, it accepts the examSource (Canvas or test bank), canvasFileName, subject, quizName, and numQuest.
This information is then used to either write an exam directly from the test bank or to create a test bank from the canvas
file and then write the exam from there.