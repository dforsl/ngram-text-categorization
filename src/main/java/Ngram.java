import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 2014-12-08.
 */
public class Ngram {
    public final List<String> words;
    private HashMap<Integer, YearData> data;

    public Ngram(List<String> words) {
        if(words == null) {
            this.words = new ArrayList<String>();
        } else {
            this.words = words;
        }
    }

    public int type() {
        return words.size();
    }

    public YearData getYearData(Integer year) {
        return data.get(year);
    }

    public void addData(Integer year, YearData yearData) {
        data.put(year, yearData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ngram ngram = (Ngram) o;

        if (!words.equals(ngram.words)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return words.hashCode();
    }

    @Override
    public String toString() {
        return words.toString();
    }
}
