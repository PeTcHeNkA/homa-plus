package plus.invlib.utl;

import java.util.HashMap;


public class AMap<T> extends HashMap<Integer, HashMap<Integer, T>> {
    public void add(int int1, int int2, T obj) {
        get(int1).put(int2, obj);
    }


    public T get(int int1, int int2) {
        if (containsKey(int1)) {
            return get(int1).get(int2);
        } else return null;
    }


    public HashMap<Integer, T> get(int int1) {
        if (containsKey(int1)) {
            return super.get(int1);
        } else {
            HashMap<Integer, T> m = new HashMap<>();
            put(int1, m);
            return m;
        }
    }
}