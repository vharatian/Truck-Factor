package aserg.gtf.significance;

import aserg.gtf.model.authorship.AuthorshipInfo;
import aserg.gtf.model.authorship.Developer;
import aserg.gtf.model.authorship.File;
import aserg.gtf.model.authorship.Repository;
import aserg.gtf.truckfactor.TFInfo;
import aserg.gtf.truckfactor.TruckFactor;
import org.apache.log4j.Logger;

import java.util.*;

public class SignificanceGreedyTruckFactor extends TruckFactor {
    private static final Logger LOGGER = Logger.getLogger(TruckFactor.class);
    private final int significanceIndex;

    private SignificanceTFInfo tfInfo = new SignificanceTFInfo();
    private float minPercentage;

    public SignificanceGreedyTruckFactor(float minPercentage, int significanceIndex) {
        this.minPercentage = minPercentage;
        this.significanceIndex= significanceIndex;
    }

    @Override
    public TFInfo getTruckFactor(Repository repository) {
        //GREDDY TRUCK FACTOR ALGORITHM

        double totalSignificance = GetTotalSignificance(repository, significanceIndex);
        List<TotalAuthorshipInfo> authorshipInfos = getAuthorshipInfo(repository, significanceIndex, totalSignificance);
        int factor = 0;
        float coverage = 1;
        while(authorshipInfos.size()>0){
            coverage = getCoverage(totalSignificance, authorshipInfos, significanceIndex);
            if (coverage<0.5)
                break;
            removeTopAuthor(authorshipInfos);
            factor++;
        }
        tfInfo.setCoverage(getCoverage(totalSignificance, authorshipInfos, significanceIndex));
        tfInfo.setTf(factor);
        tfInfo.setTotalFiles(repository.getFiles().size());

        return pruneTF(tfInfo);
    }

    private double GetTotalSignificance(Repository repository, int significanceIndex) {
        double sum = 0;
        for (File file: repository.getFiles())
        {
            FileSignificance significance = file.getSignificance();
            // Null significance means the file is not important
            if (significance != null) {
                sum += significance.indicators[significanceIndex].indicator;
            }
        }

        return sum;
    }

    private TFInfo pruneTF(TFInfo tfInfo) {
        Developer topDev = getTopOneDev(tfInfo.getTfDevelopers());
        List<Developer> prunedDevs = new ArrayList<Developer>();
        for (Developer developer : tfInfo.getTfDevelopers()) {
            if ((float)developer.getAuthorshipFiles().size()/topDev.getAuthorshipFiles().size()<minPercentage)
                prunedDevs.add(developer);
        }
        for (Developer developer : prunedDevs) {
            tfInfo.getTfDevelopers().remove(developer);
        }
        tfInfo.setTf(tfInfo.getTf()-prunedDevs.size());
        return tfInfo;
    }

    private Developer getTopOneDev(List<Developer> tfDevelopers) {
        Developer topDev = null;
        for (Developer developer : tfDevelopers) {
            if (topDev ==null)
                topDev = developer;
            else if (developer.getAuthorshipFiles().size()>topDev.getAuthorshipFiles().size())
                topDev = developer;
        }
        return topDev;
    }

    private List<TotalAuthorshipInfo> getAuthorshipInfo(Repository repository, int significanceIndex, double totalSignificance){
        List<TotalAuthorshipInfo> results = new ArrayList<>();
        List<Developer> developers = repository.getDevelopers();
        for (Developer developer : developers) {
            TotalAuthorshipInfo info = getDeveloperAuthorshipInfo(significanceIndex, totalSignificance, developer);
            if (info != null) {
                results.add(info);
            }
        }

        return results;
    }

    private static TotalAuthorshipInfo getDeveloperAuthorshipInfo(int significanceIndex, double totalSignificance, Developer developer) {
        double significanceSum = 0;
        Set<File> devFiles = new HashSet<>();
        for (AuthorshipInfo authorshipInfo : developer.getAuthorshipInfos()) {
            if (authorshipInfo.isDOAAuthor()) {
                File file = authorshipInfo.getFile();
                if (file.getSignificance() != null) {
                    significanceSum += file.getSignificance().indicators[significanceIndex].indicator;
                    devFiles.add(file);
                }
            }
        }

        if (significanceSum<=0) {
            return null;
        }

        return new TotalAuthorshipInfo(significanceSum/totalSignificance, devFiles, developer);
    }

    private float getCoverage(double totalSignificance, List<TotalAuthorshipInfo> authorshipInfos, int significanceIndex) {
        if (totalSignificance == 0){
            return 0;
        }

        Set<File> authorsSet = new HashSet<File>();
        double significanceSum = 0;
        for (TotalAuthorshipInfo info : authorshipInfos) {
            for (File file : info.getFiles()) {
                if (!authorsSet.contains(file))
                {
                    authorsSet.add(file);

                    FileSignificance significance = file.getSignificance();
                    // null significance means the file is not important at all
                    if (significance != null){
                        significanceSum += significance.indicators[significanceIndex].indicator;
                    }
                }
            }
        }
        return (float) (significanceSum / totalSignificance);
    }

    private void removeTopAuthor(List<TotalAuthorshipInfo> infos) {
        double biggerNumber = 0;
        TotalAuthorshipInfo targetDeveloper = null;
        for (TotalAuthorshipInfo info : infos) {
            if (info.getCoverage()>biggerNumber){
                biggerNumber = info.getCoverage();
                targetDeveloper = info;
            }
            if (targetDeveloper!=null && info.getCoverage() == biggerNumber)
                if(info.getDeveloper().getDevChanges() > targetDeveloper.getDeveloper().getDevChanges())
                    targetDeveloper = info;


        }
        tfInfo.addAuthorshipInfo(targetDeveloper);
        infos.remove(targetDeveloper);
    }


//	//HELP METHODS:  Used only for tests propose

//	private void printAuthorsFile(Set<File> set) {
//		for (File file : set) {
//			System.out.println(file.getPath());
//		}
//
//	}
//
//	private void printAuthorsMap(Map<Long, Set<Long>> authorsMap) {
//		for (Entry<Long, Set<Long>> entry : authorsMap.entrySet()) {
//			System.out.print(entry.getKey() + ": ");
//			for (Long fileId : entry.getValue()) {
//				System.out.print(fileId + " ");
//			}
//			System.out.println();
//		}
//
//	}
//
//	private void printTruckMap(String repName, Map<Integer, Float> truckMap) {
//		Date date = new Date();
//		for (Entry<Integer, Float> entry : truckMap.entrySet()) {
//			System.out.format("%s;%d;%f\n", repName, entry.getKey(), entry.getValue());
//
//		}
//		System.out.println("TF Developers: ");
//		for (String tfInfo : tfAuthorInfo) {
//			System.out.println(tfInfo);
//		}
//	}
}
