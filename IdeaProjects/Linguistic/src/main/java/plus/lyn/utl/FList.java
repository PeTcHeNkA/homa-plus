package plus.lyn.utl;

import java.util.Arrays;
import java.util.Iterator;


public class FList<T> implements Iterable<T>, Iterator<T> {
    private static final Object[] EMPTY = new Object[]{};
    Object[] items;
    private int iterC = 0;
    protected int cursor = 0;

    public FList(){
        this.items = EMPTY;
    }


    public FList(Object... items){
        this.items = items;
    }


    public void clearOfSize(int size){
        if(items.length<size){
            items = new Object[size];
        }
        cursor = 0;
    }


    public void safeAdd(T obj){
        if((++cursor)>=items.length){
            Object[] newArr = new Object[items.length+10];
            System.arraycopy(items, 0, newArr, 0, items.length);
            items = newArr;
        }
        items[cursor-1] = obj;
    }


    public void clear(){
        items = EMPTY;
    }


    public void add(T obj){
        items[cursor++] = obj;
    }


    public void set(int i, T obj){
        items[i] = obj;
    }


    public T[] toArray(T[] src){
        if(src.length != cursor) src = Arrays.copyOf(src, cursor);
        for (int i=0; i<src.length; i++) src[i] = (T) items[i];
        return src;
    }


    public int length(){
        return cursor;
    }


    public T first(){
        return (T) items[0];
    }


    public T get(int i){
        return (T) items[i];
    }


    @Override
    public Iterator<T> iterator() {
        iterC = 0;
        return this;
    }


    @Override
    public boolean hasNext() {
        return iterC < cursor;
    }


    @Override
    public T next() {
        return (T) items[iterC++];
    }
}
