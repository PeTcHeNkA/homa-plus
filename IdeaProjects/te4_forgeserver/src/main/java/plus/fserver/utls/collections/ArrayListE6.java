package plus.fserver.utls.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;


public class ArrayListE6<T> extends AbstractList<T> {
    static final Object[] EMPTY_ARR = {};
    protected Object[] data = EMPTY_ARR;
    protected int size = 0;

    @Override
    public int size() {
        return size;
    }


    @Override
    public T set(int index, T element) {
        T prev = (T) data[index];
        data[index] = element;
        return prev;
    }


    @Override
    public void clear() {
        for (int i = 0; i < size; i++) data[i] = null;
        size = 0;
    }


    @Override
    public boolean add(T obj){
        if(data.length<=++size)data = Arrays.copyOf(data, data== EMPTY_ARR ?6: data.length+10);
        data[size-1] = obj;
        return true;
    }


    public int newSizeAuto() {
        return data== EMPTY_ARR ?6: data.length+10;
    }


    @Override
    public T get(int index) {
        return (T) data[index];
    }


    @Override
    public void sort(Comparator<? super T> c) {
        Arrays.sort((T[]) data, 0, size, c);
    }
}