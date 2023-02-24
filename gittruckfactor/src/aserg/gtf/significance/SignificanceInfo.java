package aserg.gtf.significance;

import java.util.Map;

public class SignificanceInfo {
    public Map<String, FileSignificance> files;
    public String[] indicatorNames;

    public SignificanceInfo(Map<String, FileSignificance> files, String[] indicatorNames) {
        this.files = files;
        this.indicatorNames = indicatorNames;
    }
}
