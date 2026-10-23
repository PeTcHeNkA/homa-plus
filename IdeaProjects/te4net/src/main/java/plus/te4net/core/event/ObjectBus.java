package plus.te4net.core.event;

import io.netty.util.collection.IntObjectHashMap;
import java.util.HashMap;


public class ObjectBus{
    private final IntObjectHashMap<HashMap> bus = new IntObjectHashMap<>();
    public <C, V extends C> void put(Class<C> clazz, Object key, V value){
        int key0 = clazz.hashCode();
        put(key0, key, value);
    }


    public <V> void put(Object key, V value){
        int key0 = value.getClass().hashCode();
        put(key0, key, value);
    }


    public String remStr(Object key){
        return remove(String.class, key);
    }


    public <T> T remove(Class<T> clazz, Object key){
        HashMap<Object,T> map = bus.get(clazz.hashCode());
        if(map==null)return null;
        return map.get(key);
    }


    private void put(int key0, Object key, Object value){
        HashMap map = bus.get(key0);
        if(map==null){
            map = new HashMap<>();
            bus.put(key0, map);
        }
        map.put(key, value);
    }


    public String getStr(Object key){
        return get(String.class, key);
    }


    public <T> T get(Class<T> clazz, Object key){
        HashMap<Object,T> map = bus.get(clazz.hashCode());
        if(map==null)return null;
        return map.get(key);
    }
}
