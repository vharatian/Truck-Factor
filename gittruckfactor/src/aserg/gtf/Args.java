package aserg.gtf;

public class Args {

    public static Args parse(String[] args){
        if (args.length < 3)
            throw new IllegalArgumentException("You should enter <repository path> <significance file path> <targetPathFile>");

        String repositoryPath = args[0];
        String significanceFile = args[1];
        String targetPathFile = args[2];
        String repositoryName = "";
        if (args.length > 3)
            repositoryName = args[3];

        repositoryPath = (repositoryPath.charAt(repositoryPath.length() - 1) == '/') ? repositoryPath : (repositoryPath + "/");
        if (repositoryName.isEmpty())
            repositoryName = repositoryPath.split("/")[repositoryPath.split("/").length - 1];

        return new Args(repositoryPath, significanceFile, repositoryName, targetPathFile);
    }

    private String repositoryPath;
    private String significanceFile;
    private String repositoryName;
    private String targetPathFile;

    private Args(String repositoryPath, String significanceFile, String repositoryName, String targetPathFile) {
        this.repositoryPath = repositoryPath;
        this.significanceFile = significanceFile;
        this.repositoryName = repositoryName;
        this.targetPathFile = targetPathFile;
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
}
