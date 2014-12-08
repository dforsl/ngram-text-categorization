/**
 * Created by daniel on 2014-12-08.
 */
public class YearData {
    public final int year,
                    volumeCount,
                    pageCount,
                    matchCount;
    public final double volumeFraction;

    public YearData(int year, int volumeCount, double volumeFraction, int pageCount, int matchCount) {
        this.year = year;
        this.volumeCount = volumeCount;
        this.volumeFraction = volumeFraction;
        this.pageCount = pageCount;
        this.matchCount = matchCount;
    }
}
