package plus.fmsg.cmd.proc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import plus.fmsg.cmd.CmdMessage;
import plus.fmsg.cmd.McVarProvider;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.Objects;
import java.util.function.Predicate;


public class CmdConduction  {
    public static Predicate<CmdMessage> ofTson(TsonObj obj) {
        if(obj != null) {
            if (obj.isMap()) return ofMap(obj.getMap());
            if (obj.isString()) return ofStr(obj.getStr());
        }
        return msg -> true;
    }


    private static Predicate<CmdMessage>[] buildAll(TsonList list) {
        final Predicate<CmdMessage>[] parents = new Predicate[list.size()];
        for(int i = 0; i < list.size(); i++) {
            parents[i] = ofMap(list.getMap(i));
        }
        return parents;
    }


    private static Predicate<CmdMessage> ofStr(String type) {
        switch (type) {
            case "has-args":
                return msg -> msg.availableArgs() > 0;
            case "no-args":
                return msg -> msg.availableArgs() == 0;
            case "from-player":
                return CmdMessage::fromPlayer;
        }
        if (type.startsWith("args-length:")) {
            int length = Integer.parseInt(type.substring(12));
            return msg -> msg.availableArgs() >= length;
        }
        if(type.startsWith("player:")){
            String[] sub = type.split(":");
            switch (sub[1]){
                case "has-nbt":{
                    return new PlayerPredicate() {
                        final McVarProvider.NbtDeepAccess access = new McVarProvider.NbtDeepAccess(sub[2]);
                        @Override
                        public boolean testMsg(CmdMessage msg) {
                            return access.hasNbt(msg.getPlayer());
                        }
                    };
                }
            }
        }
        return msg -> true;
    }


    private static Predicate<CmdMessage> ofMap(TsonMap map) {
        String type = map.getStr("type");
        if(type == null){
            if(map.containsKey("or")){
                type = "or";
            }else if(map.containsKey("and")){
                type = "and";
            }
            if(type == null) return msg -> true;
        }

        switch (type){
            case "or":{
                final Predicate<CmdMessage>[] parents = buildAll(map.getList("or"));
                return msg -> {
                    for(Predicate<CmdMessage> parent : parents) {
                        if(parent.test(msg)) return true;
                    }
                    return false;
                };
            }
            case "and":{
                final Predicate<CmdMessage>[] parents = buildAll(map.getList("and"));
                return msg -> {
                    for(Predicate<CmdMessage> parent : parents) {
                        if(!parent.test(msg)) return false;
                    }
                    return true;
                };
            }
        }

        if(type.startsWith("player:")){
            String[] sub = type.split(":");

            switch (sub[1]){
                case "eq-nbt":{
                    return new PlayerPredicate() {
                        final McVarProvider.NbtDeepAccess access = new McVarProvider.NbtDeepAccess(sub[2]);
                        final NBTBase nbt = McVarProvider.fromTson(map.get("nbt"));
                        @Override
                        public boolean testMsg(CmdMessage msg) {
                            NBTTagCompound nbt = access.getNbt(msg.getPlayer());
                            if(nbt == null) return false;
                            NBTBase realNbt = nbt.getTag(access.lastKey());
                            return Objects.equals(realNbt, this.nbt);
                        }
                    };
                }
            }
        }

        return msg -> true;
    }


    private static abstract class PlayerPredicate implements Predicate<CmdMessage>{
        @Override
        public final boolean test(CmdMessage msg) {
            if(!msg.fromPlayer()) return false;
            return testMsg(msg);
        }
        public abstract boolean testMsg(CmdMessage msg);
    }
}