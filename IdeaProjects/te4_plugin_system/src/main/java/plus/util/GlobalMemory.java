package plus.util;

import java.util.HashMap;


public final class GlobalMemory {
    private static final HashMap<String,Object> linker = new HashMap<>();

    public static Object get(String s){
        return linker.get(s);
    }


    public static Object remove(String s){
        return linker.remove(s);
    }


    public static void addNew(String s, Object o) throws IllegalArgumentException{
        if(linker.containsKey(s))throw new IllegalArgumentException();
        linker.put(s, o);
    }


    public static void add(String s, Object o){
        linker.put(s, o);
    }


    public static boolean has(String s){
        return linker.containsKey(s);
    }


    public static<T> T get(String s, Class<T> clazz){
        Object result = linker.get(s);
        if(result==null)return null;
        if(clazz.isAssignableFrom(result.getClass())){
            return (T) result;
        }
        return null;
    }


    public static boolean has(String s, Class<?> clazz){
        Object result = linker.get(s);
        if(result==null)return false;
        return clazz.isAssignableFrom(result.getClass());
    }
}