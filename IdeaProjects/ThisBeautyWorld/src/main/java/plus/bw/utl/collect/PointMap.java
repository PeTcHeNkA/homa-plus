package plus.bw.utl.collect;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


public class PointMap<T> extends Int2ObjectOpenHashMap<T> {
    public T get(int x, int y){
        return super.get(x<<16 | y);
    }


    public T put(int x, int y, T val){
        return super.put(x<<16 | y, val);
    }
}