package plus.fserver.utls.collections;

public class ArrayListE2<T> extends ArrayListE6<T>{
    @Override
    public int newSizeAuto() {
        return data== EMPTY_ARR ?1: data.length+2;
    }
}