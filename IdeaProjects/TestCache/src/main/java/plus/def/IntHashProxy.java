package plus.def;

import plus.pro.EPooledIntVHashMap;


public class IntHashProxy<T> extends EPooledIntVHashMap<T> {
    public IntHashProxy() {}


    public T get(Object key){
        return lookup(key.hashCode());
    }


    public void put(Object key, T value){
        super.addKey(key.hashCode(), value);
    }


    public boolean containsKey(Object key){
        return super.containsItem(key.hashCode());
    }
}
