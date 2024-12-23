package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import plus.fserver.api.ChatPacketBuilder;
import plus.fserver.api.ITextSender;
import plus.fserver.server.Reflector;
import plus.tson.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;
import static plus.tson.ext.TsonAccessUtils.*;


public class MinigameProvider {
    private static MinigameProvider INSTANCE;
    private final HashMap<String, Supplier<MinigameController>> cache = new HashMap<>();
    private final ObjectOpenHashSet<String> allIds = new ObjectOpenHashSet<>(4);
    private final HashMap<String, Map> maps = new HashMap<>(4);
    private HashMap<String, ITextSender> msgCache;
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
//
//
//    public void register(String id, Supplier<MinigameController> func){
//        registry.put(id, func);
//    }
//
//
//    public MinigameController create(String id){
//        return registry.get(id).get();
//    }


    public TsonMap getCfg(){
        if(cachedCfg == null)
            cachedCfg = new TsonMap(TsonFile.read(pathToRootCfg));
        return cachedCfg;
    }


    protected ITextSender tryGetMsg(String id){
        ITextSender builder;
        HashMap<String, ITextSender> msgCache;

        if((msgCache = this.msgCache) != null){
            builder = msgCache.get(id);
            if(builder != null)return builder;
        }

        TsonObj msg = getR(getCfg(), id);
        if(msg == null)return null;
        builder = ITextSender.of(msg);
        if(msgCache == null){
            msgCache = this.msgCache = new HashMap<>();
        }
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
        private HashMap<String, ITextSender> msgCache;
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


        public Info info(){
            return new Info(getCfg());
        }


        protected ITextSender tryGetMsg(String id){
            ITextSender builder;
            HashMap<String, ITextSender> msgCache;

            if((msgCache = this.msgCache) != null){
                builder = msgCache.get(id);
                if(builder != null)return builder;
            }

            TsonObj msg = getR(getCfg(), id);
            if(msg == null)return null;
            builder = ITextSender.of(msg);
            if(msgCache == null){
                msgCache = this.msgCache = new HashMap<>();
            }
            msgCache.put(id, builder);
            return builder;
        }


        public class Info{
            private final TsonMap config;
            private Supplier<MinigameController> gFactory;
            private final int teams, playersPerTeam;
            private MinigameController controller;

            Info(TsonMap config) {
                this.config = config;
                this.teams = config.getInt("teams");
                this.playersPerTeam = config.getOrDefaultInt("player-per-team", 1);
            }


            public NBTTagCompound getMap(){
                return MinigameProvider.Map.this.getMap();
            }


            public int teams(){
                return teams;
            }


            public int playersPerTeam(){
                return playersPerTeam;
            }


            public TsonMap getCfg(){
                return config;
            }


            public ITextSender getMsg(String id){
                ITextSender msg = Map.this.tryGetMsg(id);
                if(msg == null)
                    msg = MinigameProvider.this.tryGetMsg(id);
                if(msg == null)
                    msg = ChatPacketBuilder.of("Default message: ["+id+"]");
                return msg;
            }
//
//
//            public String gameId(){
//                return config.getStr("game-id");
//            }


            public Supplier<MinigameController> getFactory(){
                if(gFactory != null)return gFactory;


                TsonObj func = getCfg().get("factory");
                if(func.isCustom()){
                    return (Supplier<MinigameController>) func.getField();
                }
                if(func.isString()){
                    gFactory = cache.get(func.getStr());
                    if(gFactory != null)return gFactory;
                    try {
                        gFactory = (Supplier<MinigameController>)Reflector.compileCtor(Class.forName(func.getStr()));
                        cache.put(func.getStr(), gFactory);
                        return gFactory;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException("Unsupported type: "+func);
            }


            public MinigameController controller(){
                MinigameController controller = this.controller;
                if(controller != null)return controller;
                controller = this.controller = getFactory().get();
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
