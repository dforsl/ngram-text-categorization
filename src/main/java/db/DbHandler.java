package db;

import app.Ngram;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 2014-12-09.
 */
public class DbHandler {
    private static DbHandler instance;

    private Connection connection;

    public static DbHandler getInstance() {
        if(instance == null) {
            try {
                instance = new DbHandler();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

                return null;
            } catch (SQLException e) {
                e.printStackTrace();

                return null;
            }
        }

        return instance;
    }

    private DbHandler() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ngrams.db");
    }

    public int insertAuthor(String name) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO authors (name) VALUES (?)");

        stmt.setString(1, name);
        stmt.execute();

        stmt = connection.prepareStatement("SELECT id FROM authors WHERE name = ?");
        stmt.setString(1, name);
        ResultSet result = stmt.executeQuery();

        result.next();
        return result.getInt("id");
    }

    public int insertPublication(int authorId, String name) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO publications (author_id, name) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, authorId);
        stmt.setString(2, name);

        stmt.execute();

        stmt = connection.prepareStatement("SELECT id FROM publications WHERE author_id = ? AND name = ?");
        stmt.setInt(1, authorId);
        stmt.setString(2, name);
        ResultSet result = stmt.executeQuery();

        result.next();
        return result.getInt("id");
    }

    public int ngramExists(Ngram ngram) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT id FROM ngrams WHERE first = ? AND second = ? AND third = ?");

        for(int i = 0; i < ngram.tokens.length; i++) {
            stmt.setString((i + 1), ngram.tokens[i]);
        }

        ResultSet result = stmt.executeQuery();
        if(result.next()) {
            return result.getInt("id");
        }
        return -1;
    }

    public int insertNgram(Ngram ngram) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO ngrams (first, second, third) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);

        for(int i = 0; i < ngram.tokens.length; i++) {
            stmt.setString((i + 1), ngram.tokens[i]);
        }

        stmt.execute();

        if(stmt.getGeneratedKeys().next()) {
            return stmt.getGeneratedKeys().getInt(1);
        }

        return ngramExists(ngram);
    }

    public void addPublicationNgram(int publicationId, int ngramId) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO publication_ngrams " +
                "(publication_id, ngram_id, nrOccurrences) VALUES (?, ?, COALESCE((SELECT nrOccurrences FROM publication_ngrams " +
                "WHERE publication_id = ? AND ngram_id = ?), 0) + 1)");
        stmt.setInt(1, publicationId);
        stmt.setInt(2, ngramId);
        stmt.setInt(3, publicationId);
        stmt.setInt(4, ngramId);
        stmt.execute();
    }

    public List<Ngram> getAuthorNgrams(int id) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM author_ngrams WHERE author_id = ? AS a INNER JOIN (SELECT * FROM ngrams WHERE id = a.ngram_id) ORDER BY nrOccurences DESC");

        stmt.setInt(1, id);

        ResultSet result = stmt.executeQuery();
        List<Ngram> ngrams = new ArrayList<Ngram>();
        while(result.next()) {
            String[] tokens = {result.getString("first"), result.getString("second"), result.getString("third")};
            Ngram currentNgram = new Ngram(tokens, result.getString("nrPublications"), result.getString("nrOccurences"));
            ngrams.add(currentNgram);
        }
        return ngrams;
    }

    public List<Integer> getAuthorIDs() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT id FROM authors");

        ResultSet result = stmt.executeQuery();

        List<Integer> ids = new ArrayList<Integer>();

        while(result.next()) {
            ids.add(result.getInt("id"));
        }

        return ids;
    }
}
