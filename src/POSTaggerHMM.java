import javafx.geometry.Pos;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class POSTaggerHMM {

    private final List<File> trainingSet;
    private final Map<POSTag, Integer> wordEmmisionDistribution;
    private final Map<Transition, Integer> tagTransitionDistribution;
    private final Map<String,Integer> categoryPopulation = new HashMap<>();

    private final SentenceExtractor sentenceExtractor = new SentenceExtractor();
    private Set<String> tagSet = new HashSet<>();
    private Set<String> wordSet = new HashSet<>();

    private static final String START_STRING = "**start**";

    public POSTaggerHMM(List<File> trainingSet) {
        this.trainingSet = trainingSet;
        this.wordEmmisionDistribution = new HashMap<>();
        this.tagTransitionDistribution = new HashMap<>();
    }

    public void learn() {

        trainingSet.forEach(this::buildWordEmmisionAndTagTransitionDistributions);

         wordEmmisionDistribution.keySet().stream().forEach(posTag -> {
             Integer count = categoryPopulation.get(posTag.getTag());
            categoryPopulation.put(posTag.getTag(),count == null ? wordEmmisionDistribution.get(posTag) : count + wordEmmisionDistribution.get(posTag));
        });
    }

    private boolean isUnknownWord(String word){
        return !wordSet.contains(word);
    }

    public double estimateWordEmmisionProbability(String word, String category) {
        if((word.equals(START_STRING) && !category.equals(START_STRING)) || (category.equals(START_STRING) && !word.equals(START_STRING))){
            return 0;
        }else{
            POSTag tagToFind = new POSTag(word, category);
            double occurrences = (wordEmmisionDistribution.get(tagToFind) == null ? 0: wordEmmisionDistribution.get(tagToFind))  ;
            return (occurrences ) / (categoryPopulation.get(category) );
        }
    }

    public double estimateTagTransitionProbability(String categoryTo, String categoryFrom) {
            Transition transitionToFind = new Transition(categoryFrom, categoryTo);
            double occurrences = (tagTransitionDistribution.get(transitionToFind) == null ? 0 : tagTransitionDistribution.get(transitionToFind));
            return (occurrences + 1) / (categoryPopulation.get(categoryFrom) + tagSet.size());
    }

    private void buildWordEmmisionAndTagTransitionDistributions(File file) {

        List<List<POSTag>> taggedSentences = sentenceExtractor.extractSentencesWithPOSTags(file);
        for (List<POSTag> sentence : taggedSentences) {
            POSTag previousWord = null;
            for (POSTag word : sentence) {
                if (wordEmmisionDistribution.keySet().contains(word)) {
                    wordEmmisionDistribution.put(word, wordEmmisionDistribution.get(word) + 1);
                } else {
                    wordEmmisionDistribution.put(word, 1);
                }

                Transition transition = new Transition(previousWord == null ? SentenceExtractor.START_STRING : previousWord.getTag(), word.getTag());
                if (tagTransitionDistribution.keySet().contains(transition)) {
                    tagTransitionDistribution.put(transition, tagTransitionDistribution.get(transition) + 1);
                } else {
                    tagTransitionDistribution.put(transition, 1);
                }

                previousWord = word;
                tagSet.add(previousWord.getTag());
                tagSet.add(word.getTag());
                wordSet.add(previousWord.getWord());
                wordSet.add(word.getWord());
            }
        }
    }

    public Map<File,List<List<POSTag>>> tagFiles(List<File> files){
        Map<File,List<List<POSTag>>> result = new HashMap<>();
        for(File file : files){
            result.put(file, sentenceExtractor.extractSentences(file).stream().map(sentence -> tagSentence(sentence.toArray(new String[sentence.size()]))).collect(Collectors.toList()));
        }
        return result;
    }

    public  List<POSTag> tagSentence(String[] words){

        String[] tags = tagSet.toArray(new String[tagSet.size()]);
        List<POSTag> result = new ArrayList<>();

        int K = tagSet.size();
        int N = words.length;
        Double[][] score = new Double[K][N];

        //Initialise
        for(int i = 0; i < K; i++){
            double estimatedWordEmmisionProbability = estimateWordEmmisionProbability(words[0],tags[i]);
            double estimatedTagTransitionProbability = estimateTagTransitionProbability(tags[i],START_STRING);
            score[i][0] = estimatedWordEmmisionProbability * estimatedTagTransitionProbability;
            //System.out.println(words[0] + " "+ tags[i] + " " + score[i][0]);
        }

        //Induction
        for(int j = 1; j < N; j++){
            for(int i = 0;i < K; i++ ){
                double maxScore = 0;
                for(int k = 0; k < K ; k++){
                    double estimatedWordEmmisionProbability = estimateWordEmmisionProbability(words[j],tags[i]);
                    double estimatedTagTransitionProbability = estimateTagTransitionProbability(tags[i],tags[k]);
                    double currentScore = score[k][j-1] * (isUnknownWord(words[j]) ?  estimatedTagTransitionProbability : estimatedTagTransitionProbability * estimatedWordEmmisionProbability);
                    maxScore = currentScore > maxScore ? currentScore : maxScore;
                }
                score[i][j] = maxScore;
            }

        }

        for(int j = 0; j < N; j++) {
            int maxIndex = 0;
            for (int i = 1; i < K; i++) {
                maxIndex = score[i][j] > score[maxIndex][j] ? i : maxIndex;
            }
            result.add(new POSTag(words[j],tags[maxIndex]));
        }

        return result;
    }
}
