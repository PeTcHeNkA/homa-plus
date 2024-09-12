package net.minecraft.util;

import com.google.common.collect.Iterators;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import plus.fserver.utls.collections.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class ClassInheritanceMultiMap<T extends Entity> extends EntityRemSwapList<T> {
    private static final EmptySelfIter EMPTY_BY_CLASS = new EmptySelfIter();
    private static final Set<Class<?>> ALL_KNOWN = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final IntObjectHashMap<EntityRemSwapList<T>> map = new IntObjectHashMap<>(4);
    private final IdentityTe4HashSet<Class<?>> knownKeys = new IdentityTe4HashSet<>(4);
    private final Class<T> baseClass;

    public ClassInheritanceMultiMap(Class<T> baseClassIn) {
        this.baseClass = baseClassIn;
        this.knownKeys.add(baseClassIn);
        ALL_KNOWN.add(baseClassIn);
        this.map.put(baseClassIn.hashCode(), this);

        ALL_KNOWN.forEach(this::createLookupOnInit);
    }


    protected void createLookupOnInit(Class<?> clazz) {
        final int key = clazz.hashCode();
        Entity[] values = super.data;
        for (int i = 0, s = size; i < s; i++) {
            Entity entity = values[i];
            if (clazz.isAssignableFrom(entity.getClass()))
                this.addForClassHash((T) entity, key);
        }
        this.knownKeys.add(clazz);
    }


    protected void createLookup(Class<?> clazz) {
        ALL_KNOWN.add(clazz);
        final int key = clazz.hashCode();
        Entity[] values = super.data;
        for (int i = 0, s = size; i < s; i++) {
            Entity entity = values[i];
            if (clazz.isAssignableFrom(entity.getClass())) {
                this.addForClassHash((T) entity, key);
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
    public boolean add(final T obj) {
        EntityRemSwapList<T> list; int key;
        final Class<?> clazz = obj.getClass();
        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(clazz)) {
                if ((list = map.get(key = oclass.hashCode())) == null) {
                    map.put(key, new EntityRemSwapList.E1V<>(obj));
                } else {
                    list.addF(obj);
                }
            }
        }
        return true;
    }


    private void addForClassHash(final T value, final int key) {
        final EntityRemSwapList<T> list;
        if ((list = map.get(key)) == null) {
            map.put(key, new EntityRemSwapList.E1V<>(value));
        } else {
            list.addF(value);
        }
    }


    @Override
    public boolean remove(final Object obj) {
        boolean flag = false;
        final Class<?> clazz = obj.getClass();

        for (Class<?> oclass : this.knownKeys) {
            if (oclass.isAssignableFrom(clazz)) {
                EntityRemSwapList<T> list;

                if ((list = map.get(oclass.hashCode())) != null && list.remove(obj))
                    flag = true;
            }
        }
        return flag;
    }


    @Override
    public boolean contains(Object obj) {
        return Iterators.contains(this.getByClass(obj.getClass()).iterator(), obj);
    }


    public <S> Iterable<S> getByClass(final Class<S> clazz) {
        EntityRemSwapList<T> list = map.get(initializeClassLookup(clazz).hashCode());
        if(list == null || list.isEmpty())return (Iterable<S>) EMPTY_BY_CLASS;
        return new ByClass<>(clazz, list);
    }
}


final class ByClass<S,T extends Entity> implements Iterable<S>, Iterator<S> {
    private final Class<S> clazz;
    private T next;
    private int cursor = 0;
    private final EntityRemSwapList<T> src;

    ByClass(Class<S> clazz, EntityRemSwapList<T> src) {
        this.clazz = clazz;
        this.src = src;
    }


    private void calcNext(){
        while (cursor<src.sizeC()){
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
        cursor = 0;
        calcNext();
        return this;
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public void forEach(Consumer<? super S> action) {
        int cursor = 0, size = src.sizeC();
        Object[] data = src.raw();
        Class<S> clazz = this.clazz;
        Object temp;
        while (cursor < size) {
            if (clazz.isInstance(temp = data[cursor])) action.accept((S) temp);
            ++cursor;
        }
    }


    @Override
    public S next() {
        S prev = (S) next;
        calcNext();
        return prev;
    }
}