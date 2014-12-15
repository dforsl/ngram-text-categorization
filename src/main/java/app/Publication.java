package app;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daniel on 2014-12-10.
 */
public class Publication {
    private Set<Ngram> ngrams;

    public Publication() {
        ngrams = new HashSet<Ngram>();
    }

    public void addNgram(Ngram ngram) {
        ngrams.add(ngram);
    }

    public boolean hasNgram(Ngram ngram) {
        return ngrams.contains(ngram);
    }

    public Set<Ngram> getNgrams() {
        return ngrams;
    }
}
