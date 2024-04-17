package plus.fserver.utls.collections;


public class ArrayListEV<T> extends ArrayListE6<T>{
    private final int exp;

    public ArrayListEV(int exp) {
        this.exp = exp;
    }

    @Override
    public int newSizeAuto() {
        return data.length + exp;
    }
}