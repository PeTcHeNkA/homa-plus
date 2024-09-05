package plus.fserver.utls.collections;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaCV2;
import java.util.*;
import java.util.function.Consumer;


public class AABBList implements List<AxisAlignedBB> {
    public static final AxisAlignedBB[] EMPTY_ARR = {};
    private static final Itr EMPTY_ITR = new Itr(EMPTY_ARR, 0);
    protected AxisAlignedBB[] data = EMPTY_ARR;
    protected int size = 0;

    public AABBList(){}

    public AABBList(int size){
        data = new AxisAlignedBB[size];
    }


    public AABBList(AxisAlignedBB... data){
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
    public AxisAlignedBB set(int index, AxisAlignedBB element) {
        AxisAlignedBB prev = data[index];
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
    public void forEach(Consumer<? super AxisAlignedBB> action) {
        AxisAlignedBB[] data = this.data;
        for (int i = 0, size = this.size; i < size; i++) action.accept(data[i]);
    }


    @NotNull
    @Override
    public Itr iterator() {
        return size==0?EMPTY_ITR :new Itr(data, size);
    }


    @NotNull
    @Override
    public Object[] toArray() {
        return data;
    }


    public AxisAlignedBB[] raw(){
        return data;
    }


    public void trimToSize(){
        int curSize;
        if((curSize = size) == 0){
            data = EMPTY_ARR;
            return;
        }
        AxisAlignedBB[] curData = this.data;
        if(curData.length == curSize)return;

        AxisAlignedBB[] result = new AxisAlignedBB[curSize];
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


    public Iterator<AxisAlignedBB> remIterator(){
        return new SItr(this);
    }


    private static class SItr implements Iterator<AxisAlignedBB>{
        private final AABBList src;
        private int cursor = 0;

        private SItr(AABBList src) {
            this.src = src;
        }


        @Override
        public boolean hasNext() {
            return cursor < src.size();
        }


        @Override
        public AxisAlignedBB next() {
            return src.get(cursor++);
        }


        @Override
        public void remove() {
            src.removeF(cursor);
        }
    }


    public static final class Itr implements Iterator<AxisAlignedBB>{
        private final AxisAlignedBB[] data;
        private final int max;
        private int cursor = 0;

        private Itr(AxisAlignedBB[] data, int max) {
            this.data = data;
            this.max = max;
        }

        @Override
        public boolean hasNext() {
            return cursor < max;
        }


        @Override
        public AxisAlignedBB next() {
            return data[cursor++];
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
    public ListIterator<AxisAlignedBB> listIterator() {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public ListIterator<AxisAlignedBB> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public List<AxisAlignedBB> subList(int fromIndex, int toIndex) {
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
    public boolean addAll(@NotNull Collection<? extends AxisAlignedBB> c) {
        int newSize = size + c.size();
        if(newSize > data.length)data = Arrays.copyOf(data, newSize);
        AxisAlignedBB[] data = this.data;
        for (AxisAlignedBB obj:c)data[size++] = obj;
        return !c.isEmpty();
    }


    @Override
    public boolean addAll(int index, @NotNull Collection<? extends AxisAlignedBB> c) {
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
    public AxisAlignedBB remove(int index) {
        AxisAlignedBB oldValue = data[index];
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
    public boolean add(AxisAlignedBB obj){
        if(data.length<=size)data = newArrayAuto();
        data[size++] = obj;
        return true;
    }


    @Override
    public void add(int index, AxisAlignedBB element) {
        if(data.length<=size)data = newArrayAuto();
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }


    public void addF(AxisAlignedBB obj){
        if(data.length<=size)data = newArrayAuto();
        data[size++] = obj;
    }


    public void addIfIntersects(AxisAlignedBB entityBox, AxisAlignedBB blockBox, BlockPos pos){
        if(ElcaCV2.LIBBUILD) {
            throw new RuntimeException("THIS CLASS ONLY FOR COMPILE. NOT LOAD THIS");
        }
        AxisAlignedBB[] data = this.data;
        int index = this.size;
        if(index >= data.length){
            AxisAlignedBB src = blockBox.offset(pos);

            if (entityBox.intersects(src)) {
                this.data = data = newArrayAuto();
                data[size++] = src;
            }
        } else {
            AxisAlignedBB src = data[index];

            if(src == null){
                data[index] = src = blockBox.offset(pos);
            } else {
                blockBox.offsetTo(pos, src);
            }

            if (entityBox.intersects(src)) {
                ++size;
            }
        }
    }


    public AxisAlignedBB[] newArrayAuto(){
        return Arrays.copyOf(data, data == EMPTY_ARR ?6: data.length+10);
    }


    @Override
    public AxisAlignedBB get(int index) {
        return data[index];
    }


    public AxisAlignedBB getC(int index){
        return data[index];
    }


    public AxisAlignedBB getSafe(int index){
        return index >= data.length? null: data[index];
    }


    @Override
    public void sort(Comparator<? super AxisAlignedBB> c) {
        Arrays.sort(data, 0, size, c);
    }


    public static class Hole extends AABBList{
        private final AxisAlignedBB[] nonEmpty = new AxisAlignedBB[1];
        @Override
        public AxisAlignedBB[] newArrayAuto() {
            size = 0;
            return nonEmpty;
        }


        public Hole free(){
            size = 0;
            nonEmpty[0] = null;
            return this;
        }
    }
}