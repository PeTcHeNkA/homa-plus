package plus.fserver.world.addon.pvpworld;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ChatPacketBuilder;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.ext.TsonAccessUtils;
import java.util.HashMap;
import java.util.function.Supplier;


public class MinigameControllerProvider {
    private static HashMap<String,ChatPacketBuilder> globalLang = new HashMap<>();
    private static TsonMap globalLangMap;
    private HashMap<String,ChatPacketBuilder> localLang;
    private final Supplier<MinigameController> factory;
    private final String id;
    private NBTTagCompound map;
    private String pathToMap, pathToLang;
    private TsonMap localLangMap;


    public MinigameControllerProvider(String id, Supplier<MinigameController> factory) {
        this.id = id;
        this.factory = factory;
    }

    private static class Settings{
        private String pathToMap;
        private NBTTagCompound map;
        private String pathToSettings;
        //private
    }


    public ChatPacketBuilder getMsg(String id) {
        HashMap<String, ChatPacketBuilder> lc = localLang;
        TsonMap langMap = localLangMap;
        if (lc == null) {
            if (pathToLang != null) {
                if (localLangMap == null) {
                    lc = localLang = new HashMap<>();
                    langMap = localLangMap = new TsonMap(TsonFile.read(pathToLang));
                }
            }
        }

        ChatPacketBuilder msg = tryGetMsgFrom(id, lc, langMap);
        if (msg != null) return msg;
        return tryGetMsgFrom(id, localLang, globalLangMap);
    }


    @Nullable
    private static ChatPacketBuilder tryGetMsgFrom(String id, HashMap<String, ChatPacketBuilder> lc, TsonMap langMap) {
        if(lc != null) {
            ChatPacketBuilder msg = lc.get(id);
            if(msg != null)return msg;
            TsonObj obj = TsonAccessUtils.getR(langMap, id);
            if(obj != null){
                if ((msg = tryBuild(obj, id)) != null) {
                    lc.put(id, msg);
                    return msg;
                }
            }
        }
        return null;
    }


    public NBTTagCompound getMap(){
         return map;
    }


    public final String getId(){
        return id;
    }


    private static ChatPacketBuilder tryBuild(TsonObj obj, String key){
        try {
            return ChatPacketBuilder.of(obj);
        } catch (Exception e){
            MinecraftServer.LOGGER.error("Error build message: ["+key+"] ->\n"+obj);
            e.printStackTrace();
            return null;
        }
    }


//    protected static ChatPacketBuilder getMsg(TsonMap map, String key){
//        TsonObj obj = TsonAccessUtils.getR(map, key);
//        if(obj == null)return ChatPacketBuilder.of("Default message: "+key);
//        try {
//            return ChatPacketBuilder.of(obj);
//        } catch (Exception e){
//            MinecraftServer.LOGGER.error("Error build message: ["+key+"] ->\n"+map.toString());
//            e.printStackTrace();
//            return ChatPacketBuilder.of("Default message: "+key);
//        }
//    }
}
