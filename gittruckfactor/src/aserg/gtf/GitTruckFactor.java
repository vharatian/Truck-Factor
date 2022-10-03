package aserg.gtf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import aserg.gtf.significance.FileSignificanceLoader;
import aserg.gtf.significance.SignificanceGreedyTruckFactor;
import aserg.gtf.target.TargetDirectoryLoader;
import aserg.gtf.target.TargetTFInfo;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import aserg.gtf.model.LogCommitInfo;
import aserg.gtf.model.NewFileInfo;
import aserg.gtf.model.authorship.AuthorshipInfo;
import aserg.gtf.model.authorship.Developer;
import aserg.gtf.model.authorship.Repository;
import aserg.gtf.task.DOACalculator;
import aserg.gtf.task.NewAliasHandler;
import aserg.gtf.task.extractor.FileInfoExtractor;
import aserg.gtf.task.extractor.GitLogExtractor;
import aserg.gtf.task.extractor.LinguistExtractor;
import aserg.gtf.truckfactor.PrunedGreedyTruckFactor;
import aserg.gtf.truckfactor.TFInfo;
import aserg.gtf.truckfactor.TruckFactor;
import aserg.gtf.util.ConfigInfo;
import aserg.gtf.util.FileInfoReader;
import aserg.gtf.util.LineInfo;

public class GitTruckFactor {
    private static Logger LOGGER;
    private static Properties properties = new Properties();
    private static InputStream input = null;
    public static ConfigInfo config = null;

    public static void main(String[] stringArgs) {
        BasicConfigurator.configure();
        LOGGER = Logger.getLogger(GitTruckFactor.class);
        LOGGER.trace("GitTruckFactor starts");

        loadConfiguration();

        Args args = Args.parse(stringArgs);


        Map<String, List<LineInfo>> filesInfo;
        Map<String, List<LineInfo>> aliasInfo;
        Map<String, List<LineInfo>> modulesInfo;
        try {
            filesInfo = FileInfoReader.getFileInfo("repo_info/filtered-files.txt");
        } catch (IOException e) {
            LOGGER.warn("Not possible to read repo_info/filtered-files.txt file. File filter step will not be executed!");
            filesInfo = null;
        }
        try {
            aliasInfo = FileInfoReader.getFileInfo("repo_info/alias.txt");
        } catch (IOException e) {
            LOGGER.warn("Not possible to read repo_info/alias.txt file. Aliases treating step will not be executed!");
            aliasInfo = null;
        }
        try {
            modulesInfo = FileInfoReader.getFileInfo("repo_info/modules.txt");
        } catch (IOException e) {
            LOGGER.warn("Not possible to read repo_info/modules.txt file. No modules info will be setted!");
            modulesInfo = null;
        }


        FileInfoExtractor fileExtractor = new FileInfoExtractor(args.getRepositoryPath(), args.getRepositoryName());
        LinguistExtractor linguistExtractor = new LinguistExtractor(args.getRepositoryPath(), args.getRepositoryName());
        NewAliasHandler aliasHandler = aliasInfo == null ? null : new NewAliasHandler(aliasInfo.get(args.getRepositoryName()));
        GitLogExtractor gitLogExtractor = new GitLogExtractor(args.getRepositoryPath(), args.getRepositoryName());

        FileSignificanceLoader fileSignificanceLoader = new FileSignificanceLoader(args.getSignificanceFile());
        TargetDirectoryLoader targetDirectoryLoader = new TargetDirectoryLoader(args.getTargetPathFile());


        //Persist commit info
        //gitLogExtractor.persist(commits);


        try {
            List<TargetTFInfo> tfs = getTFInfo(args.getRepositoryPath(), args.getRepositoryName(), filesInfo, modulesInfo, fileExtractor, linguistExtractor, gitLogExtractor, aliasHandler, fileSignificanceLoader, targetDirectoryLoader);
            for (TargetTFInfo tf: tfs){
                LOGGER.info("\n" + tf);
            }
        } catch (Exception e) {
            LOGGER.error("TF calculation aborted!", e);
        }


        LOGGER.trace("GitTruckFactor end");
    }

    private static List<TargetTFInfo> getTFInfo(String repositoryPath,
                                                String repositoryName,
                                                Map<String, List<LineInfo>> filesInfo,
                                                Map<String, List<LineInfo>> modulesInfo,
                                                FileInfoExtractor fileExtractor,
                                                LinguistExtractor linguistExtractor,
                                                GitLogExtractor gitLogExtractor,
                                                NewAliasHandler aliasHandler,
                                                FileSignificanceLoader fileSignificanceLoader,
                                                TargetDirectoryLoader targetDirectoryLoader) throws Exception {

        Map<String, LogCommitInfo> commits = gitLogExtractor.execute();
        if (aliasHandler != null)
            commits = aliasHandler.execute(repositoryName, commits);


        List<TargetTFInfo> results = new ArrayList<>();
        for (String targetPath :  targetDirectoryLoader.readTargetDirectories()) {
            List<NewFileInfo> files = fileExtractor.execute();
            files = linguistExtractor.setNotLinguist(files);
            if (filesInfo != null && filesInfo.size() > 0) {
                if (filesInfo.containsKey(repositoryName)) {
                    applyFilterFiles(filesInfo.get(repositoryName), files);
                } else {
                    LOGGER.warn("No filesInfo for " + repositoryName);
                }
            }

            applyTargetPath(targetPath, files);

            if (modulesInfo != null && modulesInfo.containsKey(repositoryName))
                setModules(modulesInfo.get(repositoryName), files);

            DOACalculator doaCalculator = new DOACalculator(repositoryPath, repositoryName, commits.values(), files);
            Repository repository = doaCalculator.execute();

            repository = fileSignificanceLoader.AddInfo(repository);

            // get TF result for all indicators
            List<TFInfo> tfResults = calculateTfInfo(repository);
            results.add(new TargetTFInfo(targetPath, tfResults));
        }

        return results;

    }

    private static List<TFInfo> calculateTfInfo(Repository repository){
        List<TFInfo> results = new ArrayList<>();

        // Add the vanilla TF evaluation result
        try {
            TruckFactor truckFactor = new PrunedGreedyTruckFactor(config.getMinPercentage());
            TFInfo info = truckFactor.getTruckFactor(repository);
            info.setSignificanceIndicator("None");
            results.add(info);
        } catch (Exception e) {
            LOGGER.error("TF calculation aborted for vanilla TF Calculation", e);
        }


        String[] significanceIndicators = repository.getSignificanceIndicators();
        for (int i = 0; i< significanceIndicators.length; i++){
            String significanceIndicator = significanceIndicators[i];
            try{
                TruckFactor truckFactor = new SignificanceGreedyTruckFactor(config.getMinPercentage(), i);
                TFInfo info = truckFactor.getTruckFactor(repository);
                info.setSignificanceIndicator(significanceIndicator);
                results.add(info);
            }catch (Exception e){
                LOGGER.error("TF calculation aborted for indicator " + significanceIndicator, e);
            }
        }

        return results;
    }

    private static void applyTargetPath(String targetPath, List<NewFileInfo> files) {
        applyRegexFilter(files, "^(?!" + targetPath +").+");
    }

    public static void loadConfiguration() {
        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
            float normalizedDOA = Float.parseFloat((String) properties.get("normalizedDOA"));
            float absoluteDOA = Float.parseFloat((String) properties.get("absoluteDOA"));
            float tfCoverage = Float.parseFloat((String) properties.get("tfCoverage"));
            float minPercentage = Float.parseFloat((String) properties.get("minPercentage"));
            config = new ConfigInfo(normalizedDOA, absoluteDOA, tfCoverage, minPercentage);
        } catch (IOException e1) {
            LOGGER.error("Load configuration info aborted!", e1);
        }
    }

    private static void filterModule(List<NewFileInfo> files, String module) {
        int count = 0;
        for (NewFileInfo file : files) {
            if (file.getModule().equals(module)) {
                file.setFiltered(true);
                file.setFilterInfo("Module-filter");
            } else if (!file.getFiltered())
                count++;
        }
        LOGGER.info("Files without " + module + ": " + count);
    }

    private static void filterByModule(List<NewFileInfo> files, String module) {
        int count = 0;
        for (NewFileInfo file : files) {
            if (!file.getModule().equals(module)) {
                file.setFiltered(true);
                file.setFilterInfo("Module-filter");
            } else if (!file.getFiltered())
                count++;
        }
        LOGGER.info(module + " - files: " + count);
    }

    private static void setModules(List<LineInfo> modulesInfo,
                                   List<NewFileInfo> files) {
        Map<String, String> moduleMap = new HashMap<String, String>();
        for (LineInfo lineInfo : modulesInfo) {
            moduleMap.put(lineInfo.getValues().get(0), lineInfo.getValues().get(1));
        }
        for (NewFileInfo newFileInfo : files) {
            if (moduleMap.containsKey(newFileInfo.getPath()))
                newFileInfo.setModule(moduleMap.get(newFileInfo.getPath()));
            else
                LOGGER.warn("Alert: module not found for file " + newFileInfo.getPath());
        }

    }

    private static void printCoverage(Repository repository, List<String> devsName, Calendar cal) {
        int nFiles = getTotalFiles(repository.getDevelopers());
        Set<String> devsFiles = new HashSet<String>();
        for (String devName : devsName) {
            for (Developer developer : repository.getDevelopers()) {
                if (developer.getName().equalsIgnoreCase(devName)) {
                    devsFiles.addAll(getAuthorFiles(developer));

                }

            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Coverage;" + format.format(cal.getTime()) + ";" + devsFiles.size() + ";" + nFiles + ";" + (float) devsFiles.size() / nFiles * 100);
    }

    private static int getTotalFiles(List<Developer> developers) {
        Set<String> files = new HashSet<String>();
        for (Developer developer : developers) {
            for (AuthorshipInfo authorship : developer.getAuthorshipInfos()) {
                files.add(authorship.getFile().getPath());
            }
        }
        return files.size();
    }

    private static Set<String> getAuthorFiles(Developer developer) {
        Set<String> paths = new HashSet<String>();
        for (AuthorshipInfo authorship : developer.getAuthorshipInfos()) {
            if (authorship.isDOAAuthor())
                paths.add(authorship.getFile().getPath());
        }
        return paths;
    }

    private static Map<String, LogCommitInfo> filterCommitsByDate(
            Map<String, LogCommitInfo> commits, Date endDate) {
        Map<String, LogCommitInfo> newCommits = new HashMap<String, LogCommitInfo>(commits);
        for (Entry<String, LogCommitInfo> entry : commits.entrySet()) {
            if (entry.getValue().getAuthorDate().after(endDate))
                newCommits.remove(entry.getKey());
        }
        return newCommits;
    }

    private static void applyRegexFilter(List<NewFileInfo> files, String exp) {
        int count = 0;
        for (NewFileInfo newFileInfo : files) {
            if (!newFileInfo.getFiltered()) {
                if (newFileInfo.getPath().matches(exp)) {
                    count++;
                    newFileInfo.setFiltered(true);
                    newFileInfo.setFilterInfo("REGEX: " + exp);
                }
            }
        }
        LOGGER.info("REGEX FILTER = " + count);
    }

    private static void applyRegexSelect(List<NewFileInfo> files, String exp) {
        int count = 0;
        for (NewFileInfo newFileInfo : files) {
            if (!newFileInfo.getFiltered()) {
                if (!newFileInfo.getPath().matches(exp)) {
                    count++;
                    newFileInfo.setFiltered(true);
                    newFileInfo.setFilterInfo("REGEX: " + exp);
                }
            }
        }
        LOGGER.info("REGEX FILTER = " + count);
    }

    private static void applyFilterFiles(List<LineInfo> filteredFilesInfo, List<NewFileInfo> files) {
        if (filteredFilesInfo != null) {
            for (LineInfo lineInfo : filteredFilesInfo) {
                String path = lineInfo.getValues().get(0);
                for (NewFileInfo newFileInfo : files) {
                    if (newFileInfo.getPath().equals(path)) {
                        newFileInfo.setFiltered(true);
                        newFileInfo.setFilterInfo(lineInfo.getValues().get(1));
                    }
                }
            }
        }
    }

    //Test: calculates the authorship variation on time
    private static void printCoverageInTime(String repositoryPath,
                                            String repositoryName, Map<String, List<LineInfo>> filesInfo,
                                            FileInfoExtractor fileExtractor,
                                            LinguistExtractor linguistExtractor,
                                            GitLogExtractor gitLogExtractor, NewAliasHandler aliasHandler) throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        String devsA[] = {"Yoann Delouis", "yDelouis"};
        //String devsA[] = {"WonderCsabo", "Damien"};
        List<String> devs = Arrays.asList(devsA);


        //cal.add(Calendar.MONTH, -24);

        for (int i = 0; i < 24; i++) {
            Map<String, LogCommitInfo> commits = gitLogExtractor.execute();
            commits = aliasHandler.execute(repositoryName, commits);

            List<NewFileInfo> files = fileExtractor.execute();
            files = linguistExtractor.setNotLinguist(files);
            if (filesInfo != null)
                applyFilterFiles(filesInfo.get(repositoryName), files);

            Map<String, LogCommitInfo> newCommits = filterCommitsByDate(commits, cal.getTime());
            DOACalculator doaCalculator = new DOACalculator(repositoryPath, repositoryName, newCommits.values(), files);
            Repository repository = doaCalculator.execute();
            printCoverage(repository, devs, cal);

            cal.add(Calendar.MONTH, -1);
        }
    }
}
