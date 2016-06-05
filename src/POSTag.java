/**
 * Created by Jay on 22/02/2016.
 */
public class POSTag {

    private final String word;
    private final String tag;

    public POSTag(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return word + "/" + tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        POSTag posTag = (POSTag) o;

        if (!word.equals(posTag.word)) return false;
        if (!tag.equals(posTag.tag)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }
}
