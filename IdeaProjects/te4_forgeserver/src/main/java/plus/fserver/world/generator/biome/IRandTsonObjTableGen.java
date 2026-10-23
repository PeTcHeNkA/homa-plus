package plus.fserver.world.generator.biome;

import plus.tson.TsonInt;
import plus.tson.TsonList;
import plus.tson.TsonObj;


public interface IRandTsonObjTableGen {
    default TsonList getRandObjects(){
        return new TsonList(getRandBiomes0());
    }


    TsonObj[] getRandBiomes0();
    static IRandTsonObjTableGen build(TsonObj obj){
        if(obj.isMap()){
            return new RTsonObjLib(obj.getMap());
        } else if (obj.isList()) {
            TsonList lst = obj.getList();
            if(lst.size()==0)throw new IllegalArgumentException();
            if(lst.size()==1){
                return new RTsonObjLib(lst.getMap(0));
            } else return new GTsonObjLib(lst);
        } else if (obj.isNumber()) {
            return () -> new TsonObj[]{new TsonInt(obj.getInt())};
        }
        throw new IllegalArgumentException();
    }
}