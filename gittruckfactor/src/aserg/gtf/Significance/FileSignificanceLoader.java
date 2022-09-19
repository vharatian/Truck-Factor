package aserg.gtf.Significance;

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

                    indicatorNames = new String[record.size() - 1];
                    for (int i=1; i<record.size(); i++){
                        indicatorNames[i-1] = record.get(i);
                    }

                    header = false;

                }else{
                    SignificanceIndicator[] indicators = new SignificanceIndicator[record.size() - 1];
                    for (int i=1; i<record.size(); i++){
                        indicators[i-1] = new SignificanceIndicator(indicatorNames[i-1], Double.parseDouble(record.get(i)));
                    }

                    String path = record.get(0);
                    files.put(path, new FileSignificance(path, indicators));
                }
            }

            return new SignificanceInfo(files, null);
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

        return repo;
    }
}
