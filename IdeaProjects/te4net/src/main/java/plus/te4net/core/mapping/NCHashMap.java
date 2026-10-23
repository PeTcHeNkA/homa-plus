package plus.te4net.core.mapping;

import io.netty.util.collection.IntObjectHashMap;


public final class NCHashMap<T> extends IntObjectHashMap<T> {
    @Override
    public T get(Object key) {
        return super.get(key.hashCode());
    }


    @Override
    public T getOrDefault(Object key, T def){
        return super.getOrDefault(key.hashCode(), def);
    }


    public void putSafe(Object key, T val){
        int ch = key.hashCode();
        if(containsKey(ch))throw new IllegalArgumentException("Key ["+key+"] ready exists!");
        put(ch, val);
    }
}