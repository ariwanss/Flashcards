package flashcards;

public class Flashcard  implements Comparable<Flashcard> {
    private String term;
    private String definition;
    private int mistake;

    public Flashcard(String term, String definition) {
        this.term = term;
        this.definition = definition;
        this.mistake = 0;
    }

    public Flashcard(String term, String definition, int mistake) {
        this.term = term;
        this.definition = definition;
        this.mistake = mistake;
    }

    public void incrementMistake() {
        this.mistake += 1;
    }

    public void resetMistake() {
        this.mistake = 0;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public int getMistake() {
        return mistake;
    }

    @Override
    public int compareTo(Flashcard o) {
        return Integer.compare(this.getMistake(), o.getMistake());
    }
}
