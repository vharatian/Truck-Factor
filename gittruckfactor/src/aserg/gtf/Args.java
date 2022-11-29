package aserg.gtf;

public class Args {

    public static Args parse(String[] args){
        if (args.length < 3)
            throw new IllegalArgumentException("You should enter <repository path> <significance file path> <targetPathFile> <outputpath>");

        String repositoryPath = args[0];
        String significanceFile = args[1];
        String targetPathFile = args[2];
        String outputPath = args[3];
        String repositoryName = "";
        if (args.length > 4)
            repositoryName = args[4];

        repositoryPath = (repositoryPath.charAt(repositoryPath.length() - 1) == '/') ? repositoryPath : (repositoryPath + "/");
        if (repositoryName.isEmpty())
            repositoryName = repositoryPath.split("/")[repositoryPath.split("/").length - 1];

        return new Args(repositoryPath, significanceFile, repositoryName, targetPathFile, outputPath);
    }

    private String repositoryPath;
    private String significanceFile;
    private String repositoryName;
    private String targetPathFile;
    private String outputPath;

    private Args(String repositoryPath, String significanceFile, String repositoryName, String targetPathFile, String outputPath) {
        this.repositoryPath = repositoryPath;
        this.significanceFile = significanceFile;
        this.repositoryName = repositoryName;
        this.targetPathFile = targetPathFile;
        this.outputPath = outputPath;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getSignificanceFile() {
        return significanceFile;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getTargetPathFile() {
        return targetPathFile;
    }

    public String getOutputPath() {
        return outputPath;
    }
}
