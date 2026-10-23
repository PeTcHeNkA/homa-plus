package net.minecraft.util;

import java.util.*;


public class LowerStringMap<V> extends LinkedHashMap<String, V> {
    public LowerStringMap() {}

    @Override
    public boolean containsKey(Object obj) {
        return super.containsKey(obj.toString().toLowerCase(Locale.ROOT));
    }


    @Override
    public boolean containsValue(Object obj) {
        return super.containsKey(obj);
    }


    @Override
    public V get(Object key) {
        return super.get(key.toString().toLowerCase(Locale.ROOT));
    }


    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(Locale.ROOT), value);
    }


    @Override
    public V remove(Object key) {
        return super.remove(key.toString().toLowerCase(Locale.ROOT));
    }


    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
}