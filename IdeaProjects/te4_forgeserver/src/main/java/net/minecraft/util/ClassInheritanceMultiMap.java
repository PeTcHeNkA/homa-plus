package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.util.collection.IntObjectHashMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ClassInheritanceMultiMap<T> extends AbstractSet<T> {
    // Forge: Use concurrent collection to allow creating chunks from multiple threads safely
    private static final Set<Class<?>> ALL_KNOWN = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final IntObjectHashMap<List<T>> map = new IntObjectHashMap<>();
    private final Set<Class<?>> knownKeys = Sets.newIdentityHashSet();
    private final Class<T> baseClass;
    private final List<T> values = Lists.newArrayList();

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
                this.addForClass(obj, clazz);
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
        List<T> list = this.map.get(key);
        if (list == null) {
            this.map.put(key, Lists.newArrayList(value));
        } else {
            list.add(value);
        }
    }


    @Override
    public boolean remove(Object obj) {
        //T toRemove = (T)obj;
        boolean flag = false;
        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(obj.getClass())) {
                List<T> list = this.map.get(oclass.hashCode());

                if (list != null && list.remove(obj)) {
                    flag = true;
                }
            }
        }
        return flag;
    }


    @Override
    public boolean contains(Object obj) {
        return Iterators.contains(this.getByClass(obj.getClass()).iterator(), obj);
    }


    public <S> Iterable<S> getByClass(final Class<S> clazz) {
        return () -> {
            List<T> list = ClassInheritanceMultiMap.this.map.get(
                    ClassInheritanceMultiMap.this.initializeClassLookup(clazz).hashCode()
            );

            if (list == null) {
                return Collections.emptyIterator();
            } else {
                Iterator<T> iterator = list.iterator();
                return Iterators.filter(iterator, clazz);
            }
        };
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


    @Override
    public Iterator<T> iterator() {
        return this.values.isEmpty() ? Collections.emptyIterator() : this.values.iterator();
    }


    @Override
    public int size() {
        return this.values.size();
    }
}