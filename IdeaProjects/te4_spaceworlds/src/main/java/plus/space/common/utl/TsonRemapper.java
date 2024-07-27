package plus.space.common.utl;

import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.Te4HashMap;


public class TsonRemapper {
    private final Te4HashMap<String,String> mappings;

    public TsonRemapper(){
        this.mappings = new Te4HashMap<>();
    }


    public TsonRemapper(Te4HashMap<String,String> mappings){
        this.mappings = mappings;
    }


    public TsonRemapper addAll(String[][] mappings){
        Te4HashMap<String,String> map = this.mappings;
        for (String[] pair:mappings){
            map.fput(pair[0], pair[1]);
        }
        return this;
    }

    private String reKey(String key){
        return mappings.getOrDefault(key, key);
    }


    public TsonObj copyMapped(TsonObj obj){
        switch (obj.type()){
            case MAP : return copyMapped(obj.getMap());
            case LIST: return copyMapped(obj.getList());
            default  : return obj.clone();
        }
    }


    public TsonList copyMapped(TsonList list){
        TsonList result = new TsonList(list.size());
        for (TsonObj obj:list) result.add(copyMapped(obj));
        return result;
    }


    public TsonMap copyMapped(TsonMap src){
        TsonMap map = new TsonMap(src.size());
        src.forEach((key, obj) -> map.fput(reKey(key), copyMapped(obj)));
        return map;
    }
}
