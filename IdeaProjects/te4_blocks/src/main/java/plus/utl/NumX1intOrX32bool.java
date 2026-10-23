package plus.utl;

public class NumX1intOrX32bool implements NumFolder {
    private int src;

    public NumX1intOrX32bool(int src) {
        this.src = src;
    }

    public NumX1intOrX32bool(){
        this(0);
    }

    @Override
    public int getInt(int index) {
        return src;
    }

    @Override
    public void setInt(int index, int val) {
        src = val;
    }

    @Override
    public boolean getBool(int index) {
        return getBool(src, index);
    }

    @Override
    public void setBool(int index, boolean b) {
        insert(setBool(toInt(), index, b));
    }

    @Override
    public void insert(int i) {
        src = i;
    }

    @Override
    public int toInt() {
        return src;
    }

    @Override
    public String toString() {
        return String.valueOf(src);
    }


    protected static int setBool(int src, int index, boolean b){
        if(b){
            return src | (1<<index);
        } else {
            return src & ~((1)<<index);
        }
    }

    protected static boolean getBool(int index, int num){
        return (num>>index & 1) == 1;
    }
}
