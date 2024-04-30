package plus.fserver.utls.collections;

import org.jetbrains.annotations.NotNull;
import java.util.*;


public class ArrayListE6<T> implements List<T> {
    public static final Object[] EMPTY_ARR = {};
    private static final Itr EMPTY_ITR = new Itr<>(EMPTY_ARR, 0);
    protected Object[] data = EMPTY_ARR;
    protected int size = 0;

    public ArrayListE6(){}


    public ArrayListE6(Object... data){
        this.data = data;
    }


    @Override
    public int size() {
        return size;
    }


    public int sizeC(){
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    public boolean isEmptyC(){
        return size == 0;
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


    public void fclear(){
        size = 0;
    }


    @Override
    public boolean contains(Object o) {
        for (int i=0;i<size;i++) if(data[i] != null && o.equals(data[i]))return true;
        return false;
    }


    public boolean containsRef(Object o){
        for (int i=0;i<size;i++) if(o == data[i])return true;
        return false;
    }


    @NotNull
    @Override
    public Itr<T> iterator() {
        return size==0?(Itr<T>)EMPTY_ITR :new Itr<>(data, size);
    }


    @NotNull
    @Override
    public Object[] toArray() {
        return data;
    }


    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        T1[] r = a.length >= size ? a :
                (T1[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size);

        for (int i=0;i<size;i++){
            r[i] = (T1) data[i];
        }
        return r;
    }


    private static class Itr<T> implements Iterator<T>{
        private final Object[] data;
        private final int max;
        private int cursor = 0;

        private Itr(Object[] data, int max) {
            this.data = data;
            this.max = max;
        }

        @Override
        public boolean hasNext() {
            return cursor < max;
        }


        @Override
        public T next() {
            return (T) data[cursor++];
        }


        public void restart(){
            cursor = 0;
        }
    }


    @Override
    public int indexOf(Object o) {
        for (int i=0;i<size;i++) if(data[i] != null && o.equals(data[i]))return i;
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        for (int index = 0; index < size; index++)
            if (o.equals(data[index])) {
                int numMoved = size - index - 1;
                if (numMoved > 0) System.arraycopy(data, index+1, data, index, numMoved);
                data[--size] = null;
                return true;
            }
        return false;
    }


    public boolean removeRef(Object o) {
        for (int index = 0; index < size; index++)
            if (o == data[index]) {
                int numMoved = size - index - 1;
                if (numMoved > 0) System.arraycopy(data, index+1, data, index, numMoved);
                data[--size] = null;
                return true;
            }
        return false;
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object obj:c) if(!contains(obj))return false;
        return true;
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        int newSize = size + c.size();
        if(newSize > data.length)data = Arrays.copyOf(data, newSize);
        for (Object obj:c)data[size++] = obj;
        return !c.isEmpty();
    }


    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        if(size + numNew > data.length)data = Arrays.copyOf(data, size + numNew);
        int numMoved = size - index;
        if (numMoved > 0) System.arraycopy(data, index, data, index + numNew, numMoved);
        System.arraycopy(a, 0, data, index, numNew);
        size += numNew;
        return numNew != 0;
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean rem = false;
        for (Object obj:c){
            rem = true;
            remove(obj);
        }
        return rem;
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public T remove(int index) {
        T oldValue = (T) data[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) System.arraycopy(data, index+1, data, index, numMoved);
        data[--size] = null;
        return oldValue;
    }


    @Override
    public boolean add(T obj){
        if(data.length<=size)data = newArrayAuto();
        data[size++] = obj;
        return true;
    }


    @Override
    public void add(int index, T element) {
        if(data.length<=size)data = newArrayAuto();
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }


    public void addF(Object obj){
        if(data.length<=size)data = newArrayAuto();
        data[size++] = obj;
    }


    public Object[] newArrayAuto(){
        return Arrays.copyOf(data, data == EMPTY_ARR ?6: data.length+10);
    }


    @Override
    public T get(int index) {
        return (T) data[index];
    }


    public T getSafe(int index){
        if(index >= data.length) return null;
        return (T) data[index];
    }


    @Override
    public void sort(Comparator<? super T> c) {
        Arrays.sort((T[]) data, 0, size, c);
    }
}