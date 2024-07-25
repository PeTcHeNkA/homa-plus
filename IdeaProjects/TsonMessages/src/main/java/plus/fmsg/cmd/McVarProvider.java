package plus.fmsg.cmd;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.math.MathHelper;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;


public class McVarProvider implements IVarProvider<CmdMessage> {
    public static final McVarProvider INSTANCE = new McVarProvider();

    @Override
    public IVarGetter<CmdMessage> getFmGetter(String name) {
        if (name.startsWith("user:")) {
            name = name.substring(5);
            switch (name) {
                case "name":
                    return CmdMessage::getName;
                case "uuid":
                    return msg -> {
                        if (msg.fromPlayer()) {
                            return msg.getPlayer().getUniqueID().toString();
                        }
                        return "<null>";
                    };
                case "pos":
                    return msg -> {
                        EntityPlayerMP player = msg.getPlayer();
                        return String.format("[%.2f,%.2f,%.2f]", player.posX, player.posY, player.posZ);
                    };
                case "bpos":
                    return msg -> {
                        EntityPlayerMP player = msg.getPlayer();
                        return "["+ MathHelper.floor(player.posX) +","+ MathHelper.floor(player.posY) +","+ MathHelper.floor(player.posZ) +"]";
                    };
                case "args-pos":
                    return msg -> {
                        EntityPlayerMP player = msg.getPlayer();
                        return MathHelper.floor(player.posX) +" "+ MathHelper.floor(player.posY) +" "+ MathHelper.floor(player.posZ);
                    };
            }
            if(name.startsWith("nbt:")) {
                return new NbtDeepGetter(name.substring(4));
            }
        }
        if(name.startsWith("server:")) {
            name = name.substring(7);
            switch (name) {
                case "name":
                    return msg -> msg.getServer().getName();
                case "pack-url":
                    return msg -> msg.getServer().getResourcePackUrl();
                case "pack-hash":
                    return msg -> msg.getServer().getResourcePackHash();
            }
        }
        if(name.startsWith("arg:")) {
            final int index = Integer.parseInt(name.substring(4));
            return msg -> msg.getArg(index);
        }
        return null;
    }


    public static class NbtDeepAccess {
        protected final String[] keys;

        public NbtDeepAccess(String key) {
            this.keys = key.split("\\.");
        }


        public final NBTTagCompound getNbt(Entity entity){
            NBTTagCompound nbt = entity.getEntityData();
            for (int i = 0, s = keys.length-1; i < s; i++) {
                nbt = nbt.getCompoundTag(keys[i]);
                if(nbt.isEmpty())return null;
            }
            return nbt;
        }


        public final boolean hasNbt(Entity entity){
            NBTTagCompound nbt = getNbt(entity);
            if(nbt == null)return false;
            return nbt.hasKey(lastKey());
        }


        public final String lastKey(){
            return keys[keys.length - 1];
        }
    }


    public static class NbtDeepGetter extends NbtDeepAccess implements IVarGetter<CmdMessage>{
        public NbtDeepGetter(String key) {
            super(key);
        }


        @Override
        public String get(CmdMessage msg) {
            if(!msg.fromPlayer())return "";
            EntityPlayerMP player = msg.getPlayer();

            NBTTagCompound nbt = getNbt(player);
            if(nbt == null)return "";
            return nbt.getString(lastKey());
        }
    }


    public static NBTBase fromTson(TsonObj src){
        if(src == null)return null;
        switch (src.type()){
            case INT:
                return new NBTTagInt(src.getInt());
            case LONG:
                return new NBTTagLong(src.getLong());
            case FLOAT:
                return new NBTTagFloat(src.getFloat());
            case DOUBLE:
                return new NBTTagDouble(src.getDouble());
            case STRING:
                return new NBTTagString(src.getStr());
            case BOOL:
                return new NBTTagByte(src.getBool() ? (byte)1 : (byte)0);
            case LIST:{
                NBTTagList nbt = new NBTTagList();
                for(TsonObj item : src.getList()){
                    nbt.appendTag(fromTson(item));
                }
                return nbt;
            }
            case MAP:{
                NBTTagCompound nbt = new NBTTagCompound();
                src.getMap().forEach((k, v) -> nbt.setTag(k, fromTson(v)));
                return nbt;
            }
            default:
                throw new RuntimeException("Unsupported type: " + src.type());
        }
    }
}




