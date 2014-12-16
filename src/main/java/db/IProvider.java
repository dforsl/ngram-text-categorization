package db;

import app.Author;
import app.Ngram;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by daniel on 2014-12-16.
 */
public interface IProvider {
    public static final int BUFFER_LIMIT = 100;

    public static int MIN_WORD_LENGTH = 2;
    public static Set<String> stopList = new HashSet<String>();

    public List<Author> getAuthors();

    public int getNrAuthorsForNgram(Ngram ngram);

    public long getNrAuthors();

    public HashMap<Ngram, Integer> getAuthorTf(Author author);

    /*
        SETTERS
     */
    public Object insertAuthor(String author);

    public Object insertNgram(Ngram ngram);

    public void insertAuthorNgram(Object authorId, Object ngramId);
}
