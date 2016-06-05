import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jay on 22/02/2016.
 */
public class Controller {

    private File rootDir;
    private static FileFilter EXTENSION_FILTER = (pathname -> {
        return pathname.getName().endsWith(".POS");
    });
    private static final SentenceExtractor SENTENCE_EXTRACTOR = new SentenceExtractor();

    public Controller(File rootDir) {
        this.rootDir = rootDir;
    }

    public static void main(String[] args) {

        Controller controller = new Controller(new File(args[0]));

        //10 Fold routine
        System.out.println("Going to perform a 10-Fold cross-validation routine");
        controller.perform10FoldCrossValidation(0.5);

    }

    private void perform10FoldCrossValidation(double trainingRatio){
        List<File> corpus = new ArrayList<>();
        if (rootDir.isDirectory()) {
            traverseFiles(rootDir.listFiles(),corpus);
        } else if (EXTENSION_FILTER.accept(rootDir)) {
            corpus.add(rootDir);
        } else {
            throw new IllegalArgumentException("Invalid file provided");
        }

        int testWindowSize = Double.valueOf(corpus.size() * (1 - trainingRatio)).intValue() - 1;
        int testWindowEndIndex = testWindowSize;

        while(testWindowEndIndex < corpus.size()){
            List<File> testingSet = new ArrayList<>();
            testingSet.addAll(testWindowSize <= 0 ? corpus : corpus.subList(testWindowEndIndex - testWindowSize, testWindowEndIndex));
            List<File> trainingSet = new ArrayList<>();
            trainingSet.addAll(testWindowSize <= 0 ? corpus : corpus.subList(testWindowEndIndex, corpus.size()));

            int wrapAround = Math.abs(corpus.size() - (trainingSet.size() + testingSet.size()));
            if(wrapAround > 0){
                trainingSet.addAll(0,corpus.subList(0,wrapAround));
            }

            //Train HMM using the training set
            POSTaggerHMM posTaggerHMM = new POSTaggerHMM(trainingSet);
            posTaggerHMM.learn();
            //Get results from tagging the test set
            evaluateResults(posTaggerHMM.tagFiles(testingSet));

            testWindowEndIndex += (testWindowSize <= 0 ? corpus.size() : testWindowSize);
        }

    }

    private void evaluateResults(Map<File,List<List<POSTag>>> testResult){
        double maxError = 0;
        double totalTags = 0;
        double errorTags = 0;
        for( File file : testResult.keySet()){
            //Get the actual tags first
            List<POSTag> actualTags = SENTENCE_EXTRACTOR.extractSentencesWithPOSTags(file).stream().flatMap(l -> l.stream())
                    .collect(Collectors.toList());
            List<POSTag> foundTags = testResult.get(file).stream().flatMap(l -> l.stream())
                    .collect(Collectors.toList());

            int i = 0;
            double errorCount = 0;
            while(i < foundTags.size() && i < actualTags.size()) {
                errorCount = actualTags.get(i).equals(foundTags.get(i)) ? errorCount : errorCount + 1;
                i++;
            }
            totalTags += actualTags.size();
            errorTags += errorCount;
            double error = (errorCount / actualTags.size());
            System.out.println(file.getPath() + " error: " + Math.round(error * 100) + "%");
            maxError = error > maxError ? error : maxError;
        }
        System.out.println("\n **************** STATISTICS **************** \n");
        System.out.println(" MAX ERROR : " + Math.round(maxError * 100) + "%");
        System.out.println(" AVERAGE ERROR : " + Math.round((errorTags / totalTags)*100)+ "%");
        System.out.println("\n ******************************************** \n");
    }

    private void traverseFiles(File[] files,List<File> corpus) {
        for (File file : files) {
            if (file.isDirectory()) {
                traverseFiles(file.listFiles(),corpus);
            } else if (file.isFile() && EXTENSION_FILTER.accept(file)) {
                corpus.add(file);
            }
        }
    }
}
