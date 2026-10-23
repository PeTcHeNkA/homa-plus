package plus.elch.chat.rule;

import org.bukkit.entity.Player;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import static plus.tson.ext.TsonNumUtils.*;


public interface IUserSelector {
    @NotNull
    Iterator<? extends Player> selectBy(Player player);

    static IUserSelector fromTson(TsonObj obj){
        if(obj.isCustom()){
            Object obj0 = obj.getField();
            if(obj0 instanceof IUserSelector)return (IUserSelector)obj0;
            throw new IllegalArgumentException("Illegal user selector: " + obj0.getClass()+" ("+obj0+")");
        }
        if(obj.isString()){
            String str = obj.getStr();
            switch (str) {
                case "all":
                    return SelectAll.getInstance();
                case "world":
                    return SelectWorld.getInstance();
                case "near":
                    return new SelectNear(128);
            }
            throw new IllegalArgumentException("Illegal user selector: " + str);
        }
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            String type = map.getStr("type");
            if(type == null) throw new IllegalArgumentException("Illegal user selector: " + obj);
            switch (type) {
                case "perm":
                    return new SelectPerm(IUserSelector.fromTson(map.get("parent")), map.getStr("permission"));
                case "world":
                    return SelectWorld.getInstance();
                case "all":
                    return SelectAll.getInstance();
                case "near":
                    return new SelectNear(calc(map, "radius", 128));
            }
        }
        throw new IllegalArgumentException("Illegal user selector: " + obj);
    }
}