package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import plus.fserver.api.ChatPacketBuilder;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;
import static plus.tson.ext.TsonAccessUtils.*;


public class MinigameProvider {
    private static MinigameProvider INSTANCE;
    private final HashMap<String, Supplier<MinigameController>> registry = new HashMap<>();
    private final ObjectOpenHashSet<String> allIds = new ObjectOpenHashSet<>(4);
    private final HashMap<String, Map> maps = new HashMap<>(4);
    private HashMap<String,ChatPacketBuilder> msgCache;
    private final String pathToRootCfg;
    private TsonMap cachedCfg;

    public static MinigameProvider instance(){
        MinigameProvider inst = INSTANCE;
        if(inst == null)return INSTANCE = new MinigameProvider("minigames.tson");
        return inst;
    }


    private MinigameProvider(String pathToRootCfg) {
        this.pathToRootCfg = pathToRootCfg;
    }


    public void register(String id, Supplier<MinigameController> func){
        registry.put(id, func);
    }


    public MinigameController create(String id){
        return registry.get(id).get();
    }


    public TsonMap getCfg(){
        if(cachedCfg == null)
            cachedCfg = new TsonMap(TsonFile.read(pathToRootCfg));
        return cachedCfg;
    }


    protected ChatPacketBuilder tryGetMsg(String id){
        TsonObj msg = getR(getCfg(), id);
        if(msg == null)return null;
        ChatPacketBuilder builder = ChatPacketBuilder.of(msg);
        if(msgCache == null)msgCache = new HashMap<>();
        msgCache.put(id, builder);
        return builder;
    }


    public Map map(String id){
        Map map = maps.get(id);
        if(map == null){
            TsonMap cfg = getCfg();

            TsonObj obj = getR(cfg, id);
            if(obj != null){
                TsonMap mapInf = obj.getMap();
                String path    = mapInf.getStr("map-path");
                if(!allIds.add(path))
                    throw new IllegalArgumentException("Map by path ["+path+"] ready defined!");
                String cfgPath = mapInf.getOrDefaultStr("cfg-path", null);

                map = new Map(path, cfgPath);
                if(cfgPath == null){
                    map.cachedCfg = mapInf.getMap("config");
                }
                maps.put(id, map);
            } else {
                throw new IllegalArgumentException("Map by ID [" + id + "] not founded!");
            }
        }
        return map;
    }



    public class Map{
        private HashMap<String,ChatPacketBuilder> msgCache;
        private final String pathToMap;
        private NBTTagCompound cachedMapNbt;
        private final String pathToConfig;
        private TsonMap cachedCfg;

        Map(String pathToMap, String pathToConfig) {
            this.pathToMap = pathToMap;
            this.pathToConfig = pathToConfig;
        }

        public NBTTagCompound getMap(){
            if(cachedMapNbt == null){
                cachedMapNbt = readMap(pathToMap);
            }
            return cachedMapNbt;
        }

        public TsonMap getCfg(){
            if(cachedCfg == null)
                cachedCfg = new TsonMap(TsonFile.read(pathToConfig));
            return cachedCfg;
        }


        public Info info(String path){
            return new Info(getCfg());
        }


        protected ChatPacketBuilder tryGetMsg(String id){
            TsonObj msg = getR(getCfg(), id);
            if(msg == null)return null;
            ChatPacketBuilder builder = ChatPacketBuilder.of(msg);
            if(msgCache == null)msgCache = new HashMap<>();
            msgCache.put(id, builder);
            return builder;
        }


        public class Info{
            private final TsonMap config;
            private Supplier<MinigameController> gFactory;

            Info(TsonMap config) {
                this.config = config;
            }


            public NBTTagCompound getMap(){
                return MinigameProvider.Map.this.getMap();
            }


            public TsonMap getCfg(){
                return config;
            }


            public ChatPacketBuilder getMsg(String id){
                ChatPacketBuilder msg = Map.this.tryGetMsg(id);
                if(msg == null)
                    msg = MinigameProvider.this.tryGetMsg(id);
                if(msg == null)
                    msg = ChatPacketBuilder.of("Default message: ["+id+"]");
                return msg;
            }


            public String gameId(){
                return config.getStr("game-id");
            }


            public Supplier<MinigameController> getFactory(){
                return registry.get(gameId());
            }


            public MinigameController create(){
                MinigameController controller = getFactory().get();
                controller.init(this, getCfg());
                controller.loadConfigurations(getCfg());
                return controller;
            }
        }
    }


    public static NBTTagCompound readMap(String path){
        try {
            return CompressedStreamTools.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
