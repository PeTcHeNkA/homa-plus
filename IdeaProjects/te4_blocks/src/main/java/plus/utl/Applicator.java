package plus.utl;

import java.util.HashMap;


public class Applicator<Obj, Val> extends HashMap<String, App<Obj, Val>>{
    public Applicator(App<Obj, Val>... apps){
        for(App<Obj, Val> applicator:apps){
            put(applicator.name, applicator);
        }
    }

    public void apply(String key, Obj obj, Val val){
        if(containsKey(key)){
            get(key).apply(obj, val);
        }
    }
}