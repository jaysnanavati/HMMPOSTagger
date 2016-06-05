import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jay on 05/03/2016.
 */
public class SentenceExtractor {

    public static final String START_STRING = "**start**";
    private static final String BACKSLASH_ESCAPE = "@@@";

    public List<List<String>> extractSentences(File file){
        return extractSentencesWithPOSTags(file).stream().map(sentence -> sentence.stream().map(POSTag::getWord).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public List<List<POSTag>> extractSentencesWithPOSTags(File file){

        List<List<POSTag>> result = new ArrayList<>();

        try {

            StringBuilder sb = new StringBuilder("");

            Files.readAllLines(file.toPath()).forEach(sb::append);
            String[] sentences = sb.toString().split("/\\.");

            for (String s : sentences) {
                List<POSTag> taggedSentence = new ArrayList<>();
                taggedSentence.add(new POSTag(START_STRING,START_STRING));
                s = s.replaceAll("\\\\\\/", BACKSLASH_ESCAPE);

                Matcher matcher = Pattern.compile("[(\\w+((.|')\\w+)+)|\\w|&|%|@|\\*|\\-]+/([A-Z]+)\\$?")
                        .matcher(s);

                while (matcher.find()) {

                    String[] item = matcher.group().trim().split("/");

                    for (int k = 0; k < item.length; k++) {
                        item[k] = item[k].replaceAll(BACKSLASH_ESCAPE, "\\\\\\/");
                    }

                    taggedSentence.add(new POSTag(item[0], item[1]));
                }
                result.add(taggedSentence);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
