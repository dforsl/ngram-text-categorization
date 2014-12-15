package app;

import java.util.List;

/**
 * Created by daniel on 2014-12-10.
 */
public class Ngram {
    private int id;
    public final String[] tokens;

    public Ngram(String[] tokens) {
        this.tokens = tokens;
    }

    public Ngram(String[] tokens, String nrPublications, String nrOccurences) {
        this.tokens = tokens;
    }
}
