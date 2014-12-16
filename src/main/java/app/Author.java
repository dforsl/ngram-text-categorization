package app;

import db.IProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 2014-12-10.
 */
public class Author {
    private Object id;
    private String name;

    public Author(Object id, String name) {
        this.id = id;
        this.name = name;
    }

    public Object getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double distance(IProvider provider, HashMap<Ngram, Integer> data) {
        /*
            Calculate the weights - tfidf
         */
        System.out.println("Calculating weights for input...");
        HashMap<Ngram, Double> weightsIn = calculateWeights(provider, data);
        System.out.println("Calculating weights for author...");
        HashMap<Ngram, Double> weightsAuthor = calculateWeights(provider);

        /*
            COSINE SIMILARITY
         */
        System.out.println("Calculating cosine similarity...");
        double top = 0;
        for(Map.Entry<Ngram, Double> entry : weightsIn.entrySet()) {
            if(!weightsAuthor.containsKey(entry.getKey())) {
                continue;
            }

            top += entry.getValue() * weightsAuthor.get(entry.getKey());
        }
        System.out.println(top);

        double bottom1 = 0;
        for(Double w : weightsIn.values()) {
            bottom1 += Math.pow(w, 2);
        }
        double bottom2 = 0;
        for(Double w : weightsAuthor.values()) {
            bottom2 += Math.pow(w, 2);
        }

        return top / (Math.sqrt(bottom1) * Math.sqrt(bottom2));
    }

    /**
     * For input data to classify
     * @param provider
     * @param data
     * @return
     */
    private HashMap<Ngram, Double> calculateWeights(IProvider provider, HashMap<Ngram, Integer> data) {
        HashMap<Ngram, Double> weights = new HashMap<Ngram, Double>();

        int count = 0;
        for(Ngram ngram : data.keySet()) {
            count++;
            int tf = data.get(ngram);
            int df = provider.getNrAuthorsForNgram(ngram);

            double res = 0;
            if(df != 0) {
                res = tf * Math.log(provider.getNrAuthors() / df);
            }

            weights.put(ngram, res);
        }

        return weights;
    }

    /**
     * For the author
     * @param provider
     * @return
     */
    private HashMap<Ngram, Double> calculateWeights(IProvider provider) {
        HashMap<Ngram, Double> weights = new HashMap<Ngram, Double>();
        HashMap<Ngram, Integer> tf = provider.getAuthorTf(this);

        for(Ngram ngram : tf.keySet()) {
            int df = provider.getNrAuthorsForNgram(ngram);
            double res = tf.get(ngram) * Math.log(provider.getNrAuthors()/df);
            weights.put(ngram, res);
        }

        return weights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        if (name != null ? !name.equals(author.name) : author.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
