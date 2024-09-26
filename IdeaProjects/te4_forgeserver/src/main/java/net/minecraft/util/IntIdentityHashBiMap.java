package net.minecraft.util;

import javax.annotation.*;
import net.minecraft.util.math.*;
import plus.fserver.utls.collections.NotNullArrayIter;
import java.util.*;


public class IntIdentityHashBiMap<K> implements IObjectIntIterable {
    private static final Object EMPTY;
    private K[] values;
    private int[] intKeys;
    private K[] byId;
    private int nextFreeIndex;
    private int mapSize;

    public IntIdentityHashBiMap(int initialCapacity) {
        int realSize = 1;
        while (initialCapacity > realSize) realSize <<= 2;

        //initialCapacity /= 0.8f;
        this.values = (K[])new Object[realSize];
        this.intKeys = new int[realSize];
        this.byId = (K[])new Object[realSize];
    }


    public int getId(@Nullable final K obj) {
        return this.getValue(this.getIndex(obj, this.hashObject(obj)));
    }


    @Nullable
    public K get(final int idIn) {
        return (idIn >= 0 && idIn < this.byId.length) ? this.byId[idIn] : null;
    }


    private int getValue(final int id) {
        return (id == -1) ? -1 : this.intKeys[id];
    }


    public int add(final K objectIn) {
        final int i = this.nextId();
        this.put(objectIn, i);
        return i;
    }


    private int nextId() {
        while (this.nextFreeIndex < this.byId.length && this.byId[this.nextFreeIndex] != null) {
            ++this.nextFreeIndex;
        }
        return this.nextFreeIndex;
    }


    private void grow(final int capacity) {
        final K[] ak = this.values;
        final int[] aint = this.intKeys;
        this.values = (K[])new Object[capacity];
        this.intKeys = new int[capacity];
        this.byId = (K[])new Object[capacity];
        this.nextFreeIndex = 0;
        this.mapSize = 0;
        for (int i = 0; i < ak.length; ++i) {
            if (ak[i] != null) {
                this.put(ak[i], aint[i]);
            }
        }
    }


    public void put(final K objectIn, final int intKey) {
        final int i = Math.max(intKey, this.mapSize + 1);
        if (i >= this.values.length * 0.8f) {
            int j;
            for (j = this.values.length << 1; j < intKey; j <<= 1) {}
            this.grow(j);
        }
        final int k = this.findEmpty(this.hashObject(objectIn));
        this.values[k] = objectIn;
        this.intKeys[k] = intKey;
        this.byId[intKey] = objectIn;
        ++this.mapSize;
        if (intKey == this.nextFreeIndex) {
            ++this.nextFreeIndex;
        }
    }


    private int hashObject(@Nullable final K obectIn) {
        return (MathHelper.hash(System.identityHashCode(obectIn)) & Integer.MAX_VALUE) % this.values.length;
    }


    private int getIndex(@Nullable final K objectIn, final int cursor) {
        K[] values = this.values;
        for (int i = cursor; i < values.length; ++i) {
            if (values[i] == objectIn) return i;
            if (values[i] == IntIdentityHashBiMap.EMPTY) return -1;
        }
        for (int j = 0; j < cursor; ++j) {
            if (values[j] == objectIn) return j;
            if (values[j] == IntIdentityHashBiMap.EMPTY) return -1;
        }
        return -1;
    }


    private int findEmpty(final int cursor) {
        K[] values = this.values;
        for (int i = cursor; i < values.length; ++i) {
            if (values[i] == IntIdentityHashBiMap.EMPTY) return i;
        }
        for (int j = 0; j < cursor; ++j) {
            if (values[j] == IntIdentityHashBiMap.EMPTY) return j;
        }
        throw new RuntimeException("Overflowed :(");
    }


    @Override
    public Iterator<K> iterator() {
        return new NotNullArrayIter<>(this.byId);
    }


    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.byId, null);
        this.nextFreeIndex = 0;
        this.mapSize = 0;
    }


    public int size() {
        return this.mapSize;
    }


    static {
        EMPTY = null;
    }
}