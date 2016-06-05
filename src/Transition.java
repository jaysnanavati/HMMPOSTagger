/**
 * Created by Jay on 22/02/2016.
 */
public class Transition {

    private String categoryFrom;
    private String categoryTo;

    public Transition(String categoryFrom, String categoryTo) {
        this.categoryFrom = categoryFrom;
        this.categoryTo = categoryTo;
    }

    public String getCategoryFrom() {
        return categoryFrom;
    }

    public String getCategoryTo() {
        return categoryTo;
    }

    @Override
    public String toString() {
        return categoryFrom + "->" + categoryTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        return categoryFrom.equals(that.categoryFrom) && categoryTo.equals(that.categoryTo);

    }

    @Override
    public int hashCode() {
        int result = categoryFrom.hashCode();
        result = 31 * result + categoryTo.hashCode();
        return result;
    }
}
