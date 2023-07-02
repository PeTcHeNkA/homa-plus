package plus.te4net.utl;

import io.netty.util.collection.IntObjectHashMap;


public final class NCHashMap<T> extends IntObjectHashMap<T> {
    @Override
    public T get(Object key) {
        return super.get(key.hashCode());
    }


    public void putSafe(Object key, T val){
        int ch = key.hashCode();
        if(containsKey(ch))throw new IllegalArgumentException("Key ["+key+"] ready exists!");
        put(ch, val);
    }
}
