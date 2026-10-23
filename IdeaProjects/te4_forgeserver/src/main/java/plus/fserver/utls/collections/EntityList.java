package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import plus.fserver.api.Keep;
import java.util.*;
import java.util.function.Consumer;


@Keep
public class EntityList<T extends Entity> implements List<T> {
    public static final Entity[] EMPTY_ARR = {};
    private static final Itr EMPTY_ITR = new Itr<>(EMPTY_ARR, 0);
    protected Entity[] data = EMPTY_ARR;
    protected int size = 0;

    public EntityList(){}

    public EntityList(int size){
        data = new Entity[size];
    }


    public EntityList(Entity... data){
        this.data = data;
        this.size = data.length;
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
        Object[] data = this.data;
        for (int i = 0, s = size; i < s; i++) data[i] = null;
        size = 0;
    }


    public void clearAll(){
        Object[] data = this.data;
        for (int i = 0, s = data.length; i < s; i++) data[i] = null;
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


    @Override
    public void forEach(Consumer<? super T> action) {
        Object[] data = this.data;
        for (int i = 0, size = this.size; i < size; i++) action.accept((T) data[i]);
    }


    @NotNull
    @Override
    public Itr<T> iterator() {
        return size==0?(Itr<T>)EMPTY_ITR :new Itr<>(data, size);
    }


    @Override
    public Entity[] toArray() {
        int size;
        Entity[] arr = new Entity[size = this.size];
        System.arraycopy(data, 0, arr, 0, size);
        return arr;
    }


    public Entity[] raw(){
        return data;
    }


    public void trimToSize(){
        int curSize;
        if((curSize = size) == 0){
            data = EMPTY_ARR;
            return;
        }
        Entity[] curData = this.data;
        if(curData.length == curSize)return;

        Entity[] result = new Entity[curSize];
        System.arraycopy(curData, 0, result, 0, curSize);
        this.data = result;
    }


    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        T1[] r = a.length >= size ? a :
                (T1[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size);

        for (int i = 0, s = size; i < s; i++){
            r[i] = (T1) data[i];
        }
        return r;
    }


    public Iterator<T> remIterator(){
        return new SItr<>(this);
    }


    private static class SItr<T extends Entity> implements Iterator<T>{
        private final EntityList<T> src;
        private int cursor = 0;

        private SItr(EntityList<T> src) {
            this.src = src;
        }


        @Override
        public boolean hasNext() {
            return cursor < src.size();
        }


        @Override
        public T next() {
            return src.get(cursor++);
        }


        @Override
        public void remove() {
            src.removeF(cursor);
        }
    }


    public static final class Itr<T> implements Iterator<T>{
        private final Object[] data;
        private final int max;
        private int cursor = 0;

        Itr(Object[] data, int max) {
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


        public Object nextObj(){
            return data[cursor++];
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
        Object[] data = this.data;
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
        final Object[] data = this.data;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                if (c.contains(data[r])) data[w++] = data[r];
        } finally {
            if (r != size) {
                System.arraycopy(data, r, data, w, size - r);
                w += size - r;
            }
            if (w != size) {
                for (int i = w; i < size; i++)
                    data[i] = null;
                size = w;
                modified = true;
            }
        }
        return modified;
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


    public void removeF(int index){
        int numMoved = size - index - 1;
        if (numMoved > 0) System.arraycopy(data, index+1, data, index, numMoved);
        data[--size] = null;
    }


    @Override
    public boolean add(T obj){
        Entity[] data;
        if((data = this.data).length <= size){
            data = this.data = newArrayAuto();
        }
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


    public void addF(Entity obj){
        if(data.length<=size)data = newArrayAuto();
        data[size++] = obj;
    }


    public Entity[] newArrayAuto(){
        Entity[] data;
        if((data = this.data) == EMPTY_ARR)return new Entity[6];
        return expand(data, data.length + 10);
    }


    protected static Entity[] expand(Entity[] src, int newSize){
        int prevLen;
        if((prevLen = src.length) == 0)return new Entity[newSize];
        Entity[] newArr = new Entity[newSize];
        System.arraycopy(src, 0, newArr, 0, prevLen);
        return newArr;
    }


    @Override
    public T get(int index) {
        return (T) data[index];
    }


    public Entity getC(int index){
        return data[index];
    }


    public T getSafe(int index){
        return index >= data.length? null: (T) data[index];
    }


    @Override
    public void sort(Comparator<? super T> c) {
        Arrays.sort((T[]) data, 0, size, c);
    }
}