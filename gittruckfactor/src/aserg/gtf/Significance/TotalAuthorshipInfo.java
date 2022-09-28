package aserg.gtf.Significance;

import aserg.gtf.model.authorship.Developer;
import aserg.gtf.model.authorship.File;

import java.util.Set;

public class TotalAuthorshipInfo {
    private double coverage;
    private Set<File> files;
    private Developer developer;

    public TotalAuthorshipInfo(double coverage, Set<File> files, Developer developer) {
        this.coverage = coverage;
        this.files = files;
        this.developer = developer;
    }

    public int getNumberOfFile() {
        if (files == null || files.isEmpty()){
            return 0;
        }

        return files.size();
    }

    public Developer getDeveloper() {
        return developer;
    }

    public double getCoverage() {
        return coverage;
    }

    public Set<File> getFiles() {
        return files;
    }



}
