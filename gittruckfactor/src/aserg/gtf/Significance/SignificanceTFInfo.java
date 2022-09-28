package aserg.gtf.Significance;
import aserg.gtf.truckfactor.TFInfo;

import java.util.ArrayList;
import java.util.List;

public class SignificanceTFInfo extends TFInfo {

    public List<TotalAuthorshipInfo> authorshipInfos;

    public List<TotalAuthorshipInfo> getAuthorshipInfos() {
        return authorshipInfos;
    }

    public void addAuthorshipInfo(TotalAuthorshipInfo info){
        if (this.authorshipInfos == null){
            this.authorshipInfos = new ArrayList<>();
        }

        authorshipInfos.add(info);
    }

    @Override
    public String toString() {
        String retStr = String.format("Significance = %s TF = %d (coverage = %.2f%%)\n", getSignificanceIndicator(), getTf(), getCoverage()*100);
        retStr += "TF authors (Developer;Files;Percentage):\n";
        for (TotalAuthorshipInfo info : getAuthorshipInfos()) {
            int devFiles = info.getNumberOfFile();
            retStr += String.format("%s;%d;%.2f\n",info.getDeveloper().getName(),devFiles,info.getCoverage()*100);
        }
        return retStr;
    }
}
