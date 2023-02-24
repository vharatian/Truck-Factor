package aserg.gtf.target;

import aserg.gtf.model.authorship.Developer;
import aserg.gtf.significance.SignificanceTFInfo;
import aserg.gtf.significance.TotalAuthorshipInfo;
import aserg.gtf.truckfactor.TFInfo;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutputExporter {

    private static final Logger LOGGER = Logger.getLogger(OutputExporter.class);

    public void ExportOutput(List<TargetTFInfo> tfs, String outputPath) throws IOException {
        LOGGER.info("Start exporting results");
        List<OutputTargetTFInfo> output = new ArrayList<>();
        for (TargetTFInfo targetTFInfo : tfs) {
            List<OutputTFInfo> info = new ArrayList<>();
            for (TFInfo tfInfo : targetTFInfo.getInfo()) {
                List<OutputDeveloper> developers = new ArrayList<>();
                if (tfInfo instanceof SignificanceTFInfo) {
                    SignificanceTFInfo significanceTFInfo = (SignificanceTFInfo) tfInfo;
                    if (significanceTFInfo.authorshipInfos != null) {
                        for (TotalAuthorshipInfo authorshipInfo : significanceTFInfo.authorshipInfos) {
                            developers.add(new OutputDeveloper(authorshipInfo.getDeveloper().getName(),
                                    authorshipInfo.getDeveloper().getEmail(), 0, authorshipInfo.getNumberOfFile(),
                                    0.0, authorshipInfo.getCoverage()));
                        }
                    }
                } else {
                    for (Developer developer : tfInfo.getTfDevelopers()) {
                        int devFiles = developer.getAuthorshipFiles().size();
                        developers.add(new OutputDeveloper(developer.getName(), developer.getEmail(),
                                0, devFiles, 0.0, (devFiles * 1.0) / tfInfo.getTotalFiles()));
                    }
                }


                info.add(new OutputTFInfo(tfInfo.getTf(), tfInfo.getTotalFiles(), tfInfo.getCoverage(), developers, tfInfo.getSignificanceIndicator()));
            }

            output.add(new OutputTargetTFInfo(targetTFInfo.getTargetPath(), info));
        }

        for (OutputTargetTFInfo oti: output){
            for (OutputTFInfo oi: oti.info){
                if (Double.isNaN(oi.coverage)){
                    LOGGER.error("1-Found that shit");
                }
            }
        }


        FileWriter writer = new FileWriter(outputPath);
        new Gson().toJson(output, writer);
        writer.flush();
        writer.close();
    }

    public class OutputTargetTFInfo {
        public String path;
        public List<OutputTFInfo> info;

        public OutputTargetTFInfo(String targetPath, List<OutputTFInfo> info) {
            this.path = targetPath;
            this.info = info;
        }
    }

    public class OutputTFInfo {
        public int busFactor;
        public int totalFiles;
        public double coverage;
        public List<OutputDeveloper> developers;
        public String significanceIndicator;

        public OutputTFInfo(int tf, int totalFiles, double coverage, List<OutputDeveloper> tfDevelopers, String significanceIndicator) {
            this.busFactor = tf;
            this.totalFiles = totalFiles;
            this.coverage = coverage;
            this.developers = tfDevelopers;
            this.significanceIndicator = significanceIndicator;
        }
    }

    public class OutputDeveloper {
        public String name;
        public String email;
        public int numberOfAuthoredFiles;
        public int numberOfDOAFiles;
        public double authorshipCoverage;
        public double doaAuthorshipCoverage;

        public OutputDeveloper(String name, String email, int numberOfAuthoredFiles, int numberOfDOAFiles, double authorshipCoverage, double doaAuthorshipCoverage) {
            this.name = name;
            this.email = email;
            this.numberOfAuthoredFiles = numberOfAuthoredFiles;
            this.numberOfDOAFiles = numberOfDOAFiles;
            this.authorshipCoverage = authorshipCoverage;
            this.doaAuthorshipCoverage = doaAuthorshipCoverage;
        }
    }
}


