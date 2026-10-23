package plus.fmsg.cmd.proc;

import net.minecraft.entity.Entity;
import plus.fmsg.msg.TsonCompUtls;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.function.Predicate;


public class TsonEntityFilters {
    public static Predicate<Entity> ofTson(TsonObj obj){
        if(obj.isMap())return ofMap(obj.getMap());
        return entity -> true;
    }


    private static Predicate<Entity> ofMap(TsonMap obj) {
        return null;
    }
}
