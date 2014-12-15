package parsers;

import app.Ngram;
import db.DbHandler;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by daniel on 2014-12-10.
 */
public class AuthorParser {
    private DbHandler dbHandler;
    private String authorName;
    private int id;

    public AuthorParser(String name) {
        dbHandler = DbHandler.getInstance();
        authorName = name;
    }

    public boolean insertOrLoad() {
        try {
            id = dbHandler.insertAuthor(authorName);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void parse(File[] publications) throws SQLException, IOException {
        for(File publication : publications) {
            PublicationParser parser = new PublicationParser(id, publication.getName(), publication);
            if(!parser.insertOrLoad()) {
                System.out.println("Failed to insert or load publication " + publication.getName());
                continue;
            }

            System.out.println("Parsing publication " + publication.getName());
            if(parser.parse()) {
                System.out.println("Successfully inserted the publication " + publication.getName());
            } else {
                System.out.println("Failed to insert the publication " + publication.getName());
            }
        }
    }

    public static void main(String[] args) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String command;
        while(true) {
            System.out.println("Enter author name (or exit):");
            command = reader.readLine();

            if(command.equals("exit")) {
                break;
            }

            AuthorParser parser = new AuthorParser(command);
            if(!parser.insertOrLoad()) {
                System.out.println("Failed to insert or load the author " + command);
                continue;
            }
            System.out.println(parser.id);
            System.out.println("Enter folder path:");
            File folder = new File(AuthorParser.class.getResource(reader.readLine()).toURI());
            if(!folder.isDirectory()) {
                System.out.println("Invalid folder path.");
                continue;
            }


            System.out.println("Analyzing publications...");
            parser.parse(folder.listFiles());
        }

        reader.close();
    }
}
