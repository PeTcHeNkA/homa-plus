package net.minecraft.util;

import com.google.common.collect.Iterators;
import io.netty.util.collection.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import plus.fserver.utls.collections.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ClassInheritanceMultiMap<T> extends AbstractSet<T> {
    private static final EmptySelfIter EMPTY_BY_CLASS = new EmptySelfIter();
    private static final Set<Class<?>> ALL_KNOWN = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final IntObjectHashMap<ArrayRemSwapListE6<T>> map = new IntObjectHashMap<>();
    private final Set<Class<?>> knownKeys = new IdentityTe4HashSet<>();
    private final Class<T> baseClass;
    private final ArrayRemSwapListE6<T> values = new ArrayRemSwapListE6<>();

    public ClassInheritanceMultiMap(Class<T> baseClassIn) {
        this.baseClass = baseClassIn;
        this.knownKeys.add(baseClassIn);
        ALL_KNOWN.add(baseClassIn);
        this.map.put(baseClassIn.hashCode(), this.values);

        for (Class<?> oclass : ALL_KNOWN) {
            this.createLookupOnInit(oclass);
        }
    }


    protected void createLookupOnInit(Class<?> clazz) {
        for (T t : this.values) {
            if (clazz.isAssignableFrom(t.getClass())) {
                this.addForClass(t, clazz);
            }
        }
        this.knownKeys.add(clazz);
    }


    protected void createLookup(Class<?> clazz) {
        ALL_KNOWN.add(clazz);
        for (T obj : this.values) {
            if (clazz.isAssignableFrom(obj.getClass())) {
                this.addForClass((T) obj, clazz);
            }
        }
        this.knownKeys.add(clazz);
    }


    protected Class<?> initializeClassLookup(Class<?> clazz) {
        if (this.baseClass.isAssignableFrom(clazz)) {
            if (!this.knownKeys.contains(clazz)) {
                this.createLookup(clazz);
            }
            return clazz;
        } else {
            throw new IllegalArgumentException("Don't know how to search for " + clazz);
        }
    }


    @Override
    public boolean add(T obj) {
        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(obj.getClass())) {
                this.addForClass(obj, oclass);
            }
        }
        return true;
    }


    private void addForClass(T value, Class<?> parentClass) {
        int key = parentClass.hashCode();
        ArrayListE6<T> list = this.map.get(key);
        if (list == null) {
            this.map.put(key, new ArrayRemSwapListE1V<>(value));
        } else {
            list.addF(value);
        }
    }


    @Override
    public boolean remove(Object obj) {
        boolean flag = false;
        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(obj.getClass())) {
                ArrayRemSwapListE6<T> list = this.map.get(oclass.hashCode());

                if (list != null && list.remove(obj)) {
                    flag = true;
                }
            }
        }
        return flag;
    }


    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }


    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }


    public boolean isEmptyC(){
        return values.isEmptyC();
    }


    @Override
    public int size() {
        return values.size();
    }


    @Override
    public boolean contains(Object obj) {
        return Iterators.contains(this.getByClass(obj.getClass()).iterator(), obj);
    }


    public <S> Iterable<S> getByClass(final Class<S> clazz) {
        List<T> list = map.get(initializeClassLookup(clazz).hashCode());
        if(list == null || list.isEmpty())return (Iterable<S>) EMPTY_BY_CLASS;
        return new ByClass<>(clazz, list);
    }


    public class FastIter{
        private int cursor = 0;

        public boolean hasNext(){
            return cursor < values.sizeC();
        }

        public T readAndNext(){
            return values.get(cursor++);
        }
    }


    public FastIter fastIter(){
        return new FastIter();
    }
}


final class ByClass<S,T> implements Iterable<S>, Iterator<S> {
    private final Class<S> clazz;
    private T next;
    private int cursor = 0;
    private final List<T> src;

    ByClass(Class<S> clazz, List<T> src) {
        this.clazz = clazz;
        this.src = src;
        calcNext();
    }


    private void calcNext(){
        while (cursor<src.size()){
            T temp = src.get(cursor);
            ++cursor;
            if(clazz.isInstance(temp)){
                next = temp;
                return;
            }
        }
        next = null;
    }
    @NotNull
    @Override
    public Iterator<S> iterator() {
        return this;
    }
    @Override
    public boolean hasNext() {
        return next != null;
    }
    @Override
    public S next() {
        S prev = (S) next;
        calcNext();
        return prev;
    }
}