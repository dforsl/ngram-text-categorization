package app;

import java.util.Arrays;
import java.util.List;

/**
 * Created by daniel on 2014-12-10.
 */
public class Ngram {
    private int id;
    public final String[] tokens;

    public Ngram(String first, String second, String third) {
        tokens = new String[]{first, second, third};
    }

    public Ngram(String[] tokens) {
        this.tokens = tokens;
    }

    public Ngram(String[] tokens, String nrPublications, String nrOccurences) {
        this.tokens = tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ngram ngram = (Ngram) o;

        if (!Arrays.equals(tokens, ngram.tokens)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return tokens != null ? Arrays.hashCode(tokens) : 0;
    }

    @Override
    public String toString() {
        return "Ngram{" +
                "id=" + id +
                ", tokens=" + Arrays.toString(tokens) +
                '}';
    }
}
