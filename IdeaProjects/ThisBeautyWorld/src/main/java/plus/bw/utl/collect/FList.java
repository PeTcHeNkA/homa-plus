package plus.bw.utl.collect;

import java.util.*;


public class FList<T> implements Iterable<T>, Iterator<T>, List<T> {
    public static final Object[] EMPTY = new Object[]{};
    public Object[] items;
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


    public void clear(){
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


    public void free(){
        items = EMPTY;
    }


    public void addFast(T obj){
        items[cursor++] = obj;
    }



    @Override
    public boolean add(T t) {
        safeAdd(t);
        return true;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj:c)if(!contains(obj))return false;
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends T> c) {
        int newSize = cursor + c.size();
        if(newSize >= items.length){
            Object[] newArr = new Object[newSize];
            System.arraycopy(items, 0, newArr, 0, items.length);
            items = newArr;
        }
        for (T obj:c) addFast(obj);
        return true;
    }


    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int indexOf(Object o) {
        for (int i=0;i<cursor;i++){
            if(Objects.equals(items[i], o))return i;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        int index = -1;
        for (int i=0;i<cursor;i++){
            if(Objects.equals(items[i], o))index = i;
        }
        return index;
    }


    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    public void setFast(int i, T obj){
        items[i] = obj;
    }


    @Override
    public T set(int index, T element) {
        T prev = (T) items[index];
        items[index] = element;
        return prev;
    }
//
//
//    public T[] toArrayF(T[] src){
//        if(src.length != cursor) src = Arrays.copyOf(src, cursor);
//        for (int i=0; i<src.length; i++) src[i] = (T) items[i];
//        return src;
//    }


    @Override
    public <T1> T1[] toArray(T1[] src) {
        if(src.length != cursor) src = Arrays.copyOf(src, cursor);
        for (int i=0; i<src.length; i++) src[i] = (T1) items[i];
        return src;
    }


    public int size(){
        return cursor;
    }


    @Override
    public boolean isEmpty() {
        return cursor == 0;
    }


    @Override
    public boolean contains(Object o) {
        for (int i=0;i<cursor;i++){
            if(Objects.equals(items[i], o))return true;
        }
        return false;
    }


    public T first(){
        return (T) items[0];
    }


    public T get(int i){
        return (T) items[i];
    }


    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
        //return null;
    }


    @Override
    public Iterator<T> iterator() {
        iterC = 0;
        return this;
    }


    @Override
    public Object[] toArray() {
        return items;
    }


    public int iterCursor(){
        return iterC;
    }


    public void iterCursor(int c){
        this.iterC = c;
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
