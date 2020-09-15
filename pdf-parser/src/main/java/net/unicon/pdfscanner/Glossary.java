package net.unicon.pdfscanner;

public class Glossary {

    private String term;

    private String definition;

    public Glossary(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "Glossary{" +
                "term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
