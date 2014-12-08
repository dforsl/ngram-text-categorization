import java.io.*;
import java.util.*;

/**
 * Created by daniel on 2014-12-08.
 */
public class Profile {
    private final File file;
    private final Set<String> blacklistCharacters = new HashSet<String>(Arrays.asList(".", "!", "?"));

    private HashMap<Ngram, Integer> ngramOccurrences;

    public Profile(File file) {
        this.file = file;
        ngramOccurrences = new HashMap<Ngram, Integer>();
    }

    public void classify() {
        /*
            Load all ngrams
         */
        LinkedList<String> queue = new LinkedList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while((line = reader.readLine()) != null) {
                for(String word : line.split(" ")) {
                    if (blacklistCharacters.contains(word.substring(word.length() - 1))) {
                        queue.add(word.replaceAll("^[a-zA-Z0-9]", ""));
                        createNgrams(queue);
                        queue.clear();
                        continue;
                    }

                    queue.add(word.replaceAll("^[a-zA-Z0-9]", ""));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Failed to create file reader.");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
            Fetch ngram data
         */
        NgramSearcher searcher = NgramSearcher.getInstance();
        for(Ngram ngram : ngramOccurrences.keySet()) {
            try {
                searcher.search(ngram);
            } catch (IOException e) {
                System.out.println("Error when searching for " + ngram);
                e.printStackTrace();
                continue;
            }
        }

        System.out.println("Done searching!");

        //TODO
    }

    private void createNgrams(LinkedList<String> queue) {
        while(queue.size() > 2) {
            List<String> wordList = new ArrayList<String>(Arrays.asList(queue.poll(), queue.peek(), queue.get(1)));
            Ngram ngram = new Ngram(wordList);
            if(ngramOccurrences.containsKey(ngram)) {
                ngramOccurrences.put(ngram, (ngramOccurrences.get(ngram) + 1));
            } else {
                ngramOccurrences.put(ngram, 1);
            }
        }
    }

    public int getProfileClass() {
        return 0;
    }
}