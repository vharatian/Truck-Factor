package aserg.gtf.target;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetDirectoryLoader {

    private final String filePath;

    public TargetDirectoryLoader(String targetPathFile) {
        this.filePath = targetPathFile;
    }

    public List<String> readTargetDirectories() throws IOException {
        List<String> result = new ArrayList<>();

        try (FileReader f = new FileReader(filePath)) {
            StringBuffer sb = new StringBuffer();
            while (f.ready()) {
                char c = (char) f.read();
                if (c == '\n') {
                    result.add(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }
            if (sb.length() > 0) {
                result.add(sb.toString());
            }
        }

        return result;
    }
}
