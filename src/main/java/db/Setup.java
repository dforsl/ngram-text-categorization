package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by daniel on 2014-12-09.
 */
public class Setup {

    public static void main(String[] args) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:ngrams.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE authors " +
                    "(id INTEGER PRIMARY KEY NOT NULL, " +
                    "name CHAR(50) NOT NULL," +
                    "UNIQUE(name) ON CONFLICT ABORT);";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE publications " +
                    "(id INTEGER PRIMARY KEY NOT NULL, " +
                    "author_id INTEGER NOT NULL, " +
                    "name CHAR(50) NOT NULL, " +
                    "UNIQUE(author_id, name) ON CONFLICT ABORT, " +
                    "FOREIGN KEY(author_id) REFERENCES authors(id));";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE ngrams " +
                    "(id INTEGER PRIMARY KEY NOT NULL," +
                    " first CHAR(50) NOT NULL, " +
                    " second CHAR(50) NOT NULL," +
                    " third CHAR(50) NOT NULL, " +
                    " UNIQUE(first, second, third) ON CONFLICT IGNORE);" +
                    "CREATE INDEX ngrams_first_idx ON ngrams(first); " +
                    "CREATE INDEX ngrams_second_idx ON ngrams(second);";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE publication_ngrams " +
                    "(publication_id INTEGER NOT NULL, " +
                    "ngram_id INTEGER NOT NULL, " +
                    "nrOccurrences INTEGER NOT NULL, " +
                    "UNIQUE(publication_id, ngram_id) ON CONFLICT IGNORE, " +
                    "FOREIGN KEY(publication_id) REFERENCES publications(id), " +
                    "FOREIGN KEY(ngram_id) REFERENCES ngrams(id));" +
                    "CREATE INDEX publication_id_idx ON publication_ngrams(publication_id); " +
                    "CREATE INDEX ngram_id_idx ON publication_ngrams(ngram_id);";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
}
