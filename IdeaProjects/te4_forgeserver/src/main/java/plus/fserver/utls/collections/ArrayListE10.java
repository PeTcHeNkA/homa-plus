package plus.fserver.utls.collections;


public class ArrayListE10<T> extends ArrayListE6<T>{
    @Override
    public int newSizeAuto() {
        return data.length + 10;
    }
}