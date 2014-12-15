package parsers;

import app.Ngram;
import db.DbHandler;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by daniel on 2014-12-15.
 */
public class PublicationParser {
    private static final int BUFFER_LIMIT = 100;

    private static int MIN_WORD_LENGTH = 1;
    private static Set<String> stopList = new HashSet<String>(Arrays.asList(""));

    private DbHandler dbHandler;

    private int id;
    private int authorId;
    private String publicationName;
    private File publication;

    public PublicationParser(int authorId, String name, File publication) {
        dbHandler = DbHandler.getInstance();

        this.authorId = authorId;
        this.publicationName = name;
        this.publication = publication;
    }

    public boolean insertOrLoad() {
        try {
            id = dbHandler.insertPublication(authorId, publicationName);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean parse() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(publication));
            LinkedList<String> buffer = new LinkedList<String>();
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                for(String token : tokens) {
                    token = fixToken(token);

                    if(token.length() < MIN_WORD_LENGTH || stopList.contains(token)) {
                        continue;
                    }

                    buffer.add(fixToken(token));
                }

                if(buffer.size() > BUFFER_LIMIT) {
                    flush(buffer);
                }
            }

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private String fixToken(String token) {
        return token.replaceAll("[^a-zA-Z0-9]", "");
    }

    private void flush(LinkedList<String> buffer) {
        System.out.println("Flushing...");
        long start = System.currentTimeMillis();
        while(buffer.size() > 2) {
            String first = buffer.poll(),
                    second = buffer.peek(),
                    third = buffer.get(1);
            Ngram ngram = new Ngram(new String[] {first, second, third});

            try {
                int ngramId = dbHandler.insertNgram(ngram);

                dbHandler.addPublicationNgram(id, ngramId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done flushing! Took " + (System.currentTimeMillis() - start)/1000 + "s");
        System.out.println("Moving on...");
    }
}
