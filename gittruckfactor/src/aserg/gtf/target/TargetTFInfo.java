package aserg.gtf.target;

import aserg.gtf.truckfactor.TFInfo;

import java.util.List;

public class TargetTFInfo {
    private String targetPath;
    private List<TFInfo> info;

    public TargetTFInfo(String targetPath, List<TFInfo> info) {
        this.targetPath = targetPath;
        this.info = info;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public List<TFInfo> getInfo() {
        return info;
    }

    @Override
    public String toString() {
        String result = "\n" +
                "****************************\n" +
                "*******Target: " + targetPath + "\n" +
                "****************************\n";

        for (TFInfo i : info) {
            result += "\n";
            result += i.toString();
        }

        return result;
    }
}
