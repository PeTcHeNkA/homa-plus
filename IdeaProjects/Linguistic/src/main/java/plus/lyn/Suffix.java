package plus.lyn;


public class Suffix {
    public final IdxString str;
    public final WordPartIndex.SPart[] parts;
    public final int[] weights;
    public final boolean maybeEnd;
    public final Type type;

    protected Suffix(IdxString str, WordPartIndex.SPart part, boolean maybeEnd, Type type) {
        this.str = str;
        this.parts = new WordPartIndex.SPart[]{part};
        this.weights = WordPartIndex.emptyWeights;
        this.maybeEnd = maybeEnd;
        this.type = type;
    }


    protected Suffix(IdxString str, WordPartIndex.SPart[] parts, int[] weights, boolean maybeEnd, Type type) {
        this.str = str;
        this.parts = parts;
        this.weights = weights;
        this.maybeEnd = maybeEnd;
        this.type = type;
    }


    public int length(){
        return str.length();
    }


    @Override
    public String toString() {
        return str.value;
    }


    public String toUTF8Str(){
        return str.toUTF8Str();
    }


    public enum Type{
        BUILDING, COLOR, ETC;
    }
}