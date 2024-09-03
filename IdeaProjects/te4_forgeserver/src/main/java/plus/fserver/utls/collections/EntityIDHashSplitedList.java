package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.function.Consumer;


public class EntityIDHashSplitedList implements List<Entity> {
    private static final int SIZE = 32;
    private static final int MASK = 31;
    private final ArrayRemSwapListE15<Entity>[] lists;

    public EntityIDHashSplitedList(){
        ArrayRemSwapListE15<Entity>[] lists = this.lists = new ArrayRemSwapListE15[SIZE];
        for (int i = 0; i < SIZE; i++) lists[i] = new ArrayRemSwapListE15<>();
    }


    @Override
    public boolean add(Entity entity) {
        return lists[entity.getEntityId() & MASK].add(entity);
    }

    @Override
    public boolean remove(Object o) {
        return lists[((Entity) o).getEntityId() & MASK].remove(o);
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) if(!contains(o))return false;
        return true;
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends Entity> c) {
        boolean res = false;
        for (Entity entity : c) res |= add(entity);
        return res;
    }


    @Override
    @Deprecated
    public boolean addAll(int index, @NotNull Collection<? extends Entity> c) {
        return addAll(c);
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean res = false;
        for (Object o : c) res |= remove(o);
        return res;
    }


    public void removeRefSwapAll(Collection<Entity> c){
        for (Entity o : c) removeRefSwap(o);
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int i = 0; i < SIZE;i++) lists[i].clear();
    }


    @Override
    @Deprecated
    public Entity get(int index) {
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int i = 0; i < SIZE;i++) {
            ArrayRemSwapListE15<Entity> list = lists[i];
            if (list.size >= index) index -= list.size;
            else return list.get(index);
        }
        return null;
    }


    @Override
    @Deprecated
    public Entity set(int index, Entity element) {
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int i = 0; i < SIZE;i++) {
            ArrayRemSwapListE15<Entity> list = lists[i];
            if (list.size >= index) index -= list.size;
            else return list.set(index, element);
        }
        throw new IndexOutOfBoundsException();
    }


    @Override
    @Deprecated
    public void add(int index, Entity element) {
        addF(element);
    }


    @Override
    public Entity remove(int index) {
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int i = 0; i < SIZE;i++) {
            ArrayRemSwapListE15<Entity> list = lists[i];
            if (list.size >= index) index -= list.size;
            else return list.remove(index);
        }
        throw new IndexOutOfBoundsException();
    }


    @Override
    public int indexOf(Object o) {
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        int pre = 0;
        for (int i = 0; i < SIZE; i++) {
            ArrayRemSwapListE15<Entity> list = lists[i];
            int res = list.indexOf(o);
            if(res != -1)return pre + res;
            pre += list.size;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }


    @NotNull
    @Override
    public ListIterator<Entity> listIterator() {
        return listIterator(0);
    }


    @NotNull
    @Override
    public ListIterator<Entity> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public List<Entity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }


    public void addF(Entity entity){
        lists[entity.getEntityId() & MASK].addF(entity);
    }


    public void removeRefSwap(Entity entity){
        lists[entity.getEntityId() & MASK].removeRefSwap(entity);
    }


    public ArrayRemSwapListE15<Entity> getRaw(int index){
        return lists[index];
    }


    public ArrayRemSwapListE15<Entity>[] getRaw(){
        return lists;
    }


    public int totalSize(){
        return SIZE;
    }


    @Override
    public int size() {
        int size = 0;
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int i = 0; i < SIZE; i++)size += lists[i].size;
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    
    @Override
    public boolean contains(Object o) {
        return containsRef((Entity) o);
    }


    public boolean containsRef(Entity entity){
        return lists[entity.getEntityId() & MASK].containsRef(entity);
    }


    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return new Itr(this.lists);
    }


    @Override
    public void forEach(Consumer<? super Entity> action) {
        for (ArrayRemSwapListE15<Entity> list:lists){
            Object[] data = list.data;
            for (int i = 0, s = list.size; i < s; i++)
                action.accept((Entity)data[i]);
        }
    }


    @NotNull
    @Override
    public Object[] toArray() {
        int size = 0;
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int cursor = 0; cursor < SIZE; cursor++)size += lists[cursor].size;
        Object[] arr = new Object[size];

        int i = 0;
        for (int cursor = 0; cursor < SIZE; cursor++){
            ArrayRemSwapListE15<Entity> list = lists[cursor];
            Object[] lData = list.data;
            for (int e = 0, s = list.size; e < s; e++) arr[i++] = lData[e];
        }
        return arr;
    }


    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        int size = 0;
        ArrayRemSwapListE15<Entity>[] lists = this.lists;
        for (int cursor = 0; cursor < SIZE; cursor++)size += lists[cursor].size;

        T[] arr = a.length >= size ? a :
                (T[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size);

        int i = 0;
        for (int cursor = 0; cursor < SIZE; cursor++){
            ArrayRemSwapListE15<Entity> list = lists[cursor];
            Object[] lData = list.data;
            for (int e = 0, s = list.size; e < s; e++) arr[i++] = (T) lData[e];
        }
        return arr;
    }


    private static final class Itr implements Iterator<Entity>, Iterable<Entity> {
        private final ArrayRemSwapListE15<Entity>[] lists;
        private ArrayListE6<Entity> list;
        private int cursor1 = 1, cursor2 = 0;
        private Entity next;

        public Itr(ArrayRemSwapListE15<Entity>[] lists) {
            list = (this.lists = lists)[0];
            calcNext();
        }

        private void calcNext(){
            if(cursor2 < list.size){
                next = list.get(cursor2++);
                return;
            }
            while (cursor1 < SIZE) {
                ArrayRemSwapListE6<Entity> res = lists[cursor1++];
                if(!res.isEmptyC()){
                    next = res.get(0);
                    cursor2 = 1;
                    list = res;
                    return;
                }
            }
            next = null;
        }


        @Override
        public Iterator<Entity> iterator() {
            return this;
        }


        @Override
        public void forEach(Consumer<? super Entity> action) {
            ArrayRemSwapListE15<Entity>[] lists = this.lists;
            for (int i = 0; i < SIZE; ++i) lists[i].forEach(action);
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public Entity next() {
            Entity prev = next;
            calcNext();
            return prev;
        }
    }
}
