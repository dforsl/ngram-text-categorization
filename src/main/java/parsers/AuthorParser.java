package parsers;

import db.IProvider;
import db.InMemoryDb;

import java.io.*;

/**
 * Created by daniel on 2014-12-10.
 */
public class AuthorParser {
    private IProvider provider;
    private String authorName;
    private Object id;

    public AuthorParser(String name) {
        provider = InMemoryDb.getInstance();
        authorName = name;
    }

    public void insertOrLoad() {
        id = provider.insertAuthor(authorName);
    }

    public void parse(File[] publications)  {
        for(File publication : publications) {
            PublicationParser parser = new PublicationParser(id, publication.getName(), publication);

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
            parser.insertOrLoad();
            if(parser.id == null) {
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
