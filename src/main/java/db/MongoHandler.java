package db;

import app.Author;
import app.Ngram;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 2014-12-16.
 */
public class MongoHandler implements IProvider {
    private static final String DB_NAME = "ngramdb";

    private static MongoHandler instance;

    private MongoClient mongoClient;
    private DB db;

    public static MongoHandler getInstance() {
        if(instance == null) {
            instance = new MongoHandler();
        }

        return instance;
    }

    private MongoHandler() {
        try {
            mongoClient = new MongoClient();
            db = mongoClient.getDB(DB_NAME);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /*
        GETTERS
     */
    @Override
    public List<Author> getAuthors() {
        DBCursor cursor = db.getCollection("authors").find();
        ArrayList<Author> authors = new ArrayList<Author>();
        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            authors.add(new Author(obj.get("_id"), (String) obj.get("name")));
        }

        return authors;
    }

    @Override
    public int getNrAuthorsForNgram(Ngram ngram) {
        BasicDBObject criteria = new BasicDBObject()
                .append("first", ngram.tokens[0])
                .append("second", ngram.tokens[1])
                .append("third", ngram.tokens[2]);
        DBCursor cursor = db.getCollection("ngrams").find(criteria).limit(1);
        if(cursor.hasNext()) {
            Object ngramId = cursor.next().get("_id");

            cursor = db.getCollection("authors").find();
            int count = 0;
            while(cursor.hasNext()) {
                HashMap<Object, Object> authorNgrams = (HashMap<Object, Object>) cursor.next().get("ngrams");
                if(authorNgrams.containsKey(ngramId.toString())) {
                    count++;
                }
            }

            return count;
        }

        return 0;
    }

    @Override
    public long getNrAuthors() {
        return db.getCollection("authors").count();
    }

    @Override
    public HashMap<Ngram, Integer> getAuthorTf(Author author) {
        System.out.println("Looking for author " + author);
        DBCursor cursor = db.getCollection("authors").find(new BasicDBObject("_id", new ObjectId(author.getId().toString()))).limit(1);
        HashMap<Object, Object> ngrams = (HashMap<Object, Object>) cursor.next().get("ngrams");
        HashMap<Ngram, Integer> result = new HashMap<Ngram, Integer>();
        for(Map.Entry<Object, Object> entry : ngrams.entrySet()) {
            String ngramId = (String) entry.getKey();
            Integer occurrences = (Integer) entry.getValue();

            DBCursor ngramsCursor = db.getCollection("ngrams").find(new BasicDBObject("_id", new ObjectId(ngramId))).limit(1);

            DBObject ngramObj = ngramsCursor.next();
            Ngram ngram = new Ngram((String) ngramObj.get("first"), (String) ngramObj.get("second"), (String) ngramObj.get("third"));
            result.put(ngram, occurrences);
        }

        return result;
    }

    /*
        INSERTIONS
     */

    @Override
    public ObjectId insertAuthor(String author) {
        DBCollection authors = db.getCollection("authors");
        DBCursor cursor = authors.find(new BasicDBObject("name", author)).limit(1);
        if(cursor.hasNext()) {
            return (ObjectId) cursor.next().get("_id");
        }

        BasicDBObject doc = new BasicDBObject("name", author)
                .append("ngrams", new HashMap<Object, Object>());

        authors.insert(doc);

        return (ObjectId) doc.get("_id");
    }

    @Override
    public ObjectId insertNgram(Ngram ngram) {
        DBCollection ngrams = db.getCollection("ngrams");
        BasicDBObject ngramDoc = new BasicDBObject()
                .append("first", ngram.tokens[0])
                .append("second", ngram.tokens[1])
                .append("third", ngram.tokens[2]);
        DBCursor cursor = ngrams.find(ngramDoc).limit(1);
        if(cursor.hasNext()) {
            return (ObjectId) cursor.next().get("_id");
        }

        ngrams.insert(ngramDoc);

        return (ObjectId) ngramDoc.get("_id");
    }

    @Override
    public void insertAuthorNgram(Object authorIdObj, Object ngramIdObj) {
        ObjectId authorId = (ObjectId) authorIdObj;
        ObjectId ngramId = (ObjectId) ngramIdObj;

        DBCollection authors = db.getCollection("authors");
        DBCursor cursor = authors.find(new BasicDBObject("_id", authorId)).limit(1);
        if(cursor.hasNext()) {
            DBObject author = cursor.next();
            HashMap<Object, Object> ngrams = (HashMap<Object, Object>) author.get("ngrams");
            if(ngrams.containsKey(ngramId)) {
                Integer occurrences = (Integer) ngrams.get(ngramId);
                ngrams.put(ngramId.toString(), (occurrences + 1));
            } else {
                ngrams.put(ngramId.toString(), 1);
            }

            author.put("ngrams", ngrams);
            authors.save(author);
        }
    }

    public static void setup() {
        try {
            MongoClient client = new MongoClient();
            DB db = client.getDB("ngramdb");

            db.createCollection("authors", new BasicDBObject("capped", false));
            db.createCollection("ngrams", new BasicDBObject("capped", false));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Setting up the database...");
        setup();
        System.out.println("Done!");
    }

}
