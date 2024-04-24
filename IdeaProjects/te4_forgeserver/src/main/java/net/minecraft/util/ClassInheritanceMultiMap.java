package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import io.netty.util.collection.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.IdentityTe4HashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ClassInheritanceMultiMap<T> extends AbstractSet<T> {
    private static final EmptyByClass EMPTY_BY_CLASS = new EmptyByClass();
    private static final Set<Class<?>> ALL_KNOWN = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final IntObjectHashMap<ArrayListEA<T>> map = new IntObjectHashMap<>();
    private final Set<Class<?>> knownKeys = new IdentityTe4HashSet<>();
    private final Class<T> baseClass;
    private final ArrayListEA<T> values = new ArrayListEA<>();

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
            this.map.put(key, new ArrayListE1V<>(value));
        } else {
            list.addF(value);
        }
    }


    @Override
    public boolean remove(Object obj) {
        //T toRemove = (T)obj;
        boolean flag = false;
        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(obj.getClass())) {
                ArrayListEA<T> list = this.map.get(oclass.hashCode());

                if (list != null && list.removeF(obj)) {
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
            return cursor < values.size();
        }
        public T readAndNext(){
            return values.get(cursor++);
        }
    }


    public FastIter fastIter(){
        return new FastIter();
    }
}


final class ArrayListE1V<T> extends ArrayListEA<T>{
    public ArrayListE1V(T v){
        this.data = new Object[]{v};
        this.size = 1;
    }
    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data.length == 1?5:data.length+7);
    }
}


class ArrayListEA<T> extends ArrayListE6<T>{
    boolean removeF(Object o) {
        for (int index = 0; index < size; index++)
            if (o.equals(data[index])) {
                int numMoved = size - index - 1;
                if (numMoved > 0) System.arraycopy(data, index+1, data, index, numMoved);
                data[--size] = null;
                return true;
            }
        return false;
    }
}


final class EmptyByClass<S> implements Iterable<S>, Iterator<S>{
    @NotNull
    @Override
    public Iterator<S> iterator() {
        return this;
    }
    @Override
    public boolean hasNext() {
        return false;
    }
    @Override
    public S next() {
        return null;
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