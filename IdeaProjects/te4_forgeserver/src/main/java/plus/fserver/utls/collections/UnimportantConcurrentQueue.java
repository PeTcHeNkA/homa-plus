package plus.fserver.utls.collections;

import org.jetbrains.annotations.NotNull;
import plus.tson.utl.alloc.ConcurrentCasAllocator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;


public class UnimportantConcurrentQueue<T> extends ConcurrentCasAllocator implements Queue<T> {
    private int emptyAllocs = 0;
    public UnimportantConcurrentQueue(int size, int maxLoop) {
        super(size, maxLoop);
    }


    @Override
    public boolean contains(Object o) {
        return super.containsObj(o);
    }


    @NotNull
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean add(T t) {
        super.freeObj(t);
        return true;
    }


    @Override
    public boolean remove(Object o) {
        return false;
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o:c) if(!contains(o))return false;
        return true;
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        for (Object o:c) super.freeObj(o);
        return true;
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
    public boolean offer(T t) {
        super.freeObj(t);
        return true;
    }


    @Override
    public T remove() {
        return null;
    }


    @Override
    public T poll() {
        Object last = super.tryAllocRaw();
        if(last == null){
            if(emptyAllocs++ > 1200){
                super.optimize();
                emptyAllocs = 0;
            }
        }
        return (T) last;
    }


    @Override
    public T element() {
        throw new UnsupportedOperationException();
    }


    @Override
    public T peek() {
        throw new UnsupportedOperationException();
    }
}
