package app;

import db.IProvider;
import db.InMemoryDb;
import parsers.AuthorParser;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by daniel on 2014-12-16.
 */
public class Classifier {
    public static final boolean STOP_LIST = false;

    private IProvider provider = InMemoryDb.getInstance();

    public Classifier() {
    }

    public String classify(File file) {
        System.out.println("Parsing input...");
        HashMap<Ngram, Integer> data = parseFile(file);
        System.out.println(data.size());

        /*
            Calculate the distance to each author
         */
        System.out.println("Retrieving authors...");
        List<Author> authors = provider.getAuthors();

        HashMap<Author, Double> results = new HashMap<Author, Double>();
        double top = 0;
        Author a = null;
        for(Author author : authors) {
            System.out.println("Comparing with " + author);
            double similarity = author.distance(provider, data);
            results.put(author, similarity);

            if(similarity > top) {
                top = similarity;
                a = author;
            }
        }

        System.out.println(results);

        return a.getName();
    }

    private HashMap<Ngram, Integer> parseFile(File file) {
        HashMap<Ngram, Integer> data = new HashMap<Ngram, Integer>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            LinkedList<String> buffer = new LinkedList<String>();
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                for(String token : tokens) {
                    token = fixToken(token);

                    if(token.length() < IProvider.MIN_WORD_LENGTH || IProvider.stopList.contains(token)) {
                        flush(buffer, data);
                        buffer.clear();
                        continue;
                    }

                    buffer.add(fixToken(token));
                }

                if(buffer.size() > IProvider.BUFFER_LIMIT) {
                    flush(buffer, data);
                }
            }

            return data;
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

        return null;
    }

    private String fixToken(String token) {
        return token.replaceAll("[^a-zA-Z0-9']", "").toLowerCase();
    }

    private void flush(LinkedList<String> buffer, HashMap<Ngram, Integer> data) {
        while(buffer.size() > 2) {
            String first = buffer.poll(),
                    second = buffer.peek(),
                    third = buffer.get(1);
            Ngram ngram = new Ngram(new String[] {first, second, third});

            if(data.containsKey(ngram)) {
                data.put(ngram, data.get(ngram) + 1);
            } else {
                data.put(ngram, 1);
            }
        }
    }

    public static void loadConf() {
        if(STOP_LIST) {
            System.out.println("Reading stop list...");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(Classifier.class.getResource("../stoplist.txt").toURI())));
                String word;
                while ((word = reader.readLine()) != null) {
                    IProvider.stopList.add(word);
                }

                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadAuthors(String[] writers) {
        for(String writer : writers) {
            writer = writer.trim();
            String folderName = "../" + writer.toLowerCase().replace(" ", "_");
            System.out.println("Loading " + writer + "...");
            AuthorParser parser = new AuthorParser(writer);
            parser.insertOrLoad();
            File folder = null;
            try {
                folder = new File(AuthorParser.class.getResource(folderName).toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            System.out.println("Analyzing publications...");
            parser.parse(folder.listFiles());
        }
        System.out.println("Writers and publications successfully loaded!");
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("Starting up...");
        loadConf();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Classifier classifier = new Classifier();
        String command;

        boolean hasLoaded = false;
        while(!hasLoaded) {
            System.out.println("Enter available writers, separated by comma:");
            command = reader.readLine();

            if(command.equals("exit")) {
                break;
            }

            if(command.equals("default")) {
                command = "Charles Dickens, Henry Van Dyke, Jane Austen, Jules Verne, Mark Twain, Edith Nesbit";
            }

            String[] writers = command.split(",");
            loadAuthors(writers);
            hasLoaded = true;
        }

        while(true && hasLoaded) {
            command = reader.readLine();

            if(command.equals("exit")) {
                break;
            }

            File file = new File(Classifier.class.getResource(command).toURI());
            if(!file.exists()) {
                System.out.println("Bad file. Try again!");
                continue;
            }

            String clazz = classifier.classify(file);
            System.out.println("Classified the text to: " + clazz);
        }

        reader.close();
    }
}
