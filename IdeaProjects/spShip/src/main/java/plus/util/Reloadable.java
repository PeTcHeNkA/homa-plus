package plus.util;

import java.util.ArrayList;
import java.util.List;


public interface Reloadable {

    static void reloadAll(){
        ReloadCache.reloadAll();
    }


    static void add(Reloadable r){
        ReloadCache.add(r);
    }


    void reload();
}


class ReloadCache {

    private final static List<Reloadable> rs = new ArrayList<>();
    protected static void reloadAll(){
        List<Reloadable> old = new ArrayList<>(rs);
        rs.clear();
        for(Reloadable r:old){
            try {
                r.reload();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        rs.addAll(old);

        System.out.println("Reloaded!");
    }


    protected static void add(Reloadable r){
        rs.add(r);
    }
}