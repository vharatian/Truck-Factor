package aserg.gtf.Significance;

public class FileSignificance {
    public String path;
    public SignificanceIndicator[] indicators;

    public FileSignificance(String path, SignificanceIndicator[] indicators) {
        this.path = path;
        this.indicators = indicators;
    }
}
