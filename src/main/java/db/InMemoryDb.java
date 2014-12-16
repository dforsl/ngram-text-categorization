package db;

import app.Author;
import app.Ngram;

import java.util.*;

/**
 * Created by daniel on 2014-12-16.
 */
public class InMemoryDb implements IProvider {
    private static IProvider instance;

    private HashMap<Author, Integer> authors;
    private HashMap<Ngram, Integer> ngrams;
    private HashMap<Integer, Ngram> idToNGram;
    private HashMap<Integer, HashMap<Integer, Integer>> authorNgrams;

    public static IProvider getInstance() {
        if(instance == null) {
            instance = new InMemoryDb();
        }

        return instance;
    }

    private InMemoryDb() {
        authors = new HashMap<Author, Integer>();
        authorNgrams = new HashMap<Integer, HashMap<Integer, Integer>>();
        ngrams = new HashMap<Ngram, Integer>();
        idToNGram = new HashMap<Integer, Ngram>();
    }

    @Override
    public List<Author> getAuthors() {
        return new ArrayList<Author>(authors.keySet());
    }

    @Override
    public int getNrAuthorsForNgram(Ngram ngram) {
        int count = 0;
        for(Author author : authors.keySet()) {
            Integer authorId = authors.get(author);
            Integer ngramId = ngrams.get(ngram);
            if(authorNgrams.get(authorId).containsKey(ngramId)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public long getNrAuthors() {
        return authors.size();
    }

    @Override
    public HashMap<Ngram, Integer> getAuthorTf(Author author) {
        HashMap<Ngram, Integer> result = new HashMap<Ngram, Integer>();
        for(Map.Entry<Integer, Integer> entry : authorNgrams.get(authors.get(author)).entrySet()) {
            result.put(idToNGram.get(entry.getKey()), entry.getValue());
        }

        return result;
    }

    @Override
    public Object insertAuthor(String author) {
        Author a = new Author(authors.keySet().size(), author);
        if(!authors.containsKey(a)) {
            authors.put(a, (Integer) a.getId());
        }

        return authors.get(a);
    }

    @Override
    public Object insertNgram(Ngram ngram) {
        if(!ngrams.containsKey(ngram)) {
            ngrams.put(ngram, ngrams.size());
            idToNGram.put(idToNGram.size(), ngram);
        }

        return ngrams.get(ngram);
    }

    @Override
    public void insertAuthorNgram(Object authorId, Object ngramId) {
        if(!authorNgrams.containsKey(authorId)) {
            authorNgrams.put((Integer) authorId, new HashMap<Integer, Integer>());
        }

        HashMap<Integer, Integer> ngrams = authorNgrams.get(authorId);
        if(ngrams.containsKey(ngramId)) {
            ngrams.put((Integer) ngramId, ngrams.get(ngramId) + 1);
        } else {
            ngrams.put((Integer) ngramId, 1);
        }
    }
}
