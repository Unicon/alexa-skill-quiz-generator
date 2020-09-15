# pdf-parser

This project provides the tools to convert pdf files into Glossary csv files, Multiple Choice quiz csv files, or Glossary quiz csv files.
Instantiate and setup the PDFManager and PDFStyledTextStripper objects for your needs, then use the appropriate Scraper
object to extract the text from the pdf, and finally use the appropriate Writer to create the csv file.

## Depends on:
This project relies on the pdfbox/pdfbox repo, which must be cloned and built locally from here: https://bitbucket.org/unicon/pdfbox/src/2.0/

## Is relied upon by:
write-quiz-lambda - Use `maven clean install` to create a jar that the write-quiz-lambda project can utilize.

The pdf courses can be found at https://openstax.org/subjects
Contact Mary Gwozdz for multiple choice test banks.