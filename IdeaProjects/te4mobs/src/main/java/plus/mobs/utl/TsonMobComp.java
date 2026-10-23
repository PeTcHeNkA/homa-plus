package plus.mobs.utl;

import plus.tson.TsonMap;


public interface TsonMobComp {
    static double getOrDef(TsonMap map, String key, double def){
        if(map.containsKey(key)){
            return map.get(key).getDouble();
        } else {
            map.put(key, def);
            return def;
        }
    }
}