package plus.fserver.utls.collections;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class HoleList<T> implements List<T> {
    private static final Object[] EMPTY = new Object[]{};
    private static final ListIterator EMPTY_ITER = new ListIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }


        @Override
        public Object next() {
            return null;
        }


        @Override
        public boolean hasPrevious() {
            return false;
        }


        @Override
        public Object previous() {
            return null;
        }


        @Override
        public int nextIndex() {
            return 0;
        }


        @Override
        public int previousIndex() {
            return 0;
        }


        @Override
        public void remove() {}


        @Override
        public void set(Object o) {}


        @Override
        public void add(Object o) {}
    };

    private boolean isEmpty = true;
    @Override
    public int size() {
        return isEmpty?0:1;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return EMPTY_ITER;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return EMPTY;
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        isEmpty = false;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }


    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return false;
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }


    @Override
    public void clear() {
        isEmpty = true;
    }


    @Override
    public T get(int index) {
        return null;
    }


    @Override
    public T set(int index, T element) {
        return null;
    }


    @Override
    public void add(int index, T element) {
        isEmpty = false;
    }


    @Override
    public T remove(int index) {
        return null;
    }


    @Override
    public int indexOf(Object o) {
        return 0;
    }


    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }


    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return EMPTY_ITER;
    }


    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return EMPTY_ITER;
    }


    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this;
    }
}