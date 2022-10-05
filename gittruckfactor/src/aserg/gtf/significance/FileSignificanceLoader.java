package aserg.gtf.significance;

import aserg.gtf.model.authorship.File;
import aserg.gtf.model.authorship.Repository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class FileSignificanceLoader {
    public static final String INDICATOR_EQUAL = "AvelinoWithOnlyJavaFile";
    private final String significanceFilePath;
    public FileSignificanceLoader(String significanceFilePath) {
        this.significanceFilePath = significanceFilePath;
    }

    public SignificanceInfo LoadInfo() {
        try{
            Reader in = new FileReader(significanceFilePath);

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .parse(in);


            Map<String, FileSignificance> files = new HashMap<>();
            String[] indicatorNames = null;
            boolean header = true;
            for (CSVRecord record : records) {
                if (header){

                    indicatorNames = new String[record.size()];
                    for (int i=1; i<record.size(); i++){
                        indicatorNames[i-1] = record.get(i);
                    }

                    // The last indicator should be equal weight which represents the regular algorithm
                    indicatorNames[indicatorNames.length - 1] = INDICATOR_EQUAL;
                    header = false;

                }else{
                    SignificanceIndicator[] indicators = new SignificanceIndicator[record.size()];
                    for (int i=1; i<record.size(); i++){
                        indicators[i-1] = new SignificanceIndicator(indicatorNames[i-1], Double.parseDouble(record.get(i)));
                    }

                    // Add an equal weight to all the files
                    indicators[indicators.length - 1] = new SignificanceIndicator(INDICATOR_EQUAL, 1);

                    String path = record.get(0);
                    files.put(path, new FileSignificance(path, indicators));
                }
            }

            return new SignificanceInfo(files, indicatorNames);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public Repository AddInfo(Repository repo) {
        SignificanceInfo info = LoadInfo();

        for (File file: repo.getFiles()) {
            FileSignificance significance = info.files.getOrDefault(file.getPath(), null);
            file.setSignificance(significance);
        }

        repo.setSignificanceIndicators(info.indicatorNames);

        return repo;
    }
}
