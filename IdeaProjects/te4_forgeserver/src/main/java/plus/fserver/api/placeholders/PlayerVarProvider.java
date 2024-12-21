package plus.fserver.api.placeholders;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.format.AdaptVarProvider;
import plus.format.FormatBuilder;
import plus.format.IVarProvider;
import plus.fserver.api.IPlayerDataModel;
import plus.format.IVarGetter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


public class PlayerVarProvider<T> extends TreeVarProvider<T,PlayerVarProvider<T>>{
    public  static final PlayerVarProvider<Entity>           ENTITY_PRO      = new PlayerVarProvider<>(Entity.class);
    public  static final PlayerVarProvider<EntityPlayer>     PLAYER_PRO      = new PlayerVarProvider<>(EntityPlayer.class);
    public  static final PlayerVarProvider<World>            WORLD_PRO       = new PlayerVarProvider<>(World.class);
    public  static final PlayerVarProvider<MinecraftServer>  SERVER_PRO      = new PlayerVarProvider<>(MinecraftServer.class);
    private static final PlayerVarProvider<IPlayerDataModel> MODEL_PRO       = new PlayerVarProvider<>(IPlayerDataModel.class);
    private static final PlayerVarProvider<NBTBase>          NBT_ADAPT       = new PlayerVarProvider<>(NBTBase.class);
    private static final IVarProvider<IPlayerDataModel>      MODEL_2_PLAYER  = new AdaptVarProvider<IPlayerDataModel, EntityPlayer>(PLAYER_PRO) {
        public IVarGetter<IPlayerDataModel> adapt(IVarGetter<EntityPlayer> src) {
            return new IVarGetter<IPlayerDataModel>() {
                public Object get(IPlayerDataModel model) {return src.get(model.getSelfPlayer());}
                public Class<?> getType() {return src.getType();}
            };
        }
    };


    public static final IVarProvider[] ALL_PROVIDERS = new IVarProvider[]{
            ENTITY_PRO, PLAYER_PRO, WORLD_PRO, SERVER_PRO, MODEL_PRO, NBT_ADAPT, MODEL_2_PLAYER
    };


    public PlayerVarProvider(Class<T> type) {
        super(type);
    }


    public static IVarGetter<IPlayerDataModel> buildFormat(String format){
        return builder().setFormat(format).build().simplify();
    }


    public static FormatBuilder<IPlayerDataModel> builder(){
        return new FormatBuilder<>(IPlayerDataModel.class)
                .setProvider(ALL_PROVIDERS);
    }


    @Override
    protected PlayerVarProvider<T> create() {
        return new PlayerVarProvider<>(getInputType());
    }


    static {
        ENTITY_PRO.add("entity")
            .addStr("name", Entity::getName)
            .add("uuid", new IVarGetter<Entity>() {
                public Object get(Entity entity) {return entity.getUniqueID();}
                public Class<?> getType() {return UUID.class;}
            })
            .add("pos", new IVarGetter<Entity>() {
                public Object get(Entity entity) {return entity.getPosition();}
                public Class<?> getType() {return BlockPos.class;}
            })
            .addDouble("posX", entity -> entity.posX)
            .addDouble("posY", entity -> entity.posY)
            .addDouble("posZ", entity -> entity.posZ)
            .add("tag", args -> new IVarGetter<Entity>() {
                final String arg = args.allLast();
                public NBTBase get(Entity entity) {
                    NBTTagCompound nbt;
                    if((nbt = entity.getEntityDataRaw()) == null)return null;
                    return nbt.getTag(arg);
                }
                public int getInt(Entity entity) {
                    NBTBase result;
                    if((result = get(entity)) == null)return 0;
                    int id;
                    if ((id = result.getId()) > 0 && id < 7)return ((NBTPrimitive)result).getInt();
                    return 0;
                }
                public double getDouble(Entity entity) {
                    NBTBase result;
                    if((result = get(entity)) == null)return 0;
                    int id;
                    if ((id = result.getId()) > 0 && id < 7)return ((NBTPrimitive)result).getDouble();
                    return 0;
                }
                public boolean getBool(Entity entity) {
                    return getInt(entity) != 0;
                }
                public Class<?> getType() {return NBTBase.class;}
            })
            .add("world", new IVarGetter<Entity>() {
                public Object get(Entity entity) {return entity.world;}
                public Class<?> getType() {return World.class;}
            })
        ;

        MODEL_PRO.add("arg", args -> new IVarGetter.Str<IPlayerDataModel>() {
            private final String arg = args.cur();
            public Object get(IPlayerDataModel model) {
                return model.model().apply(arg);
            }
        });

        NBT_ADAPT.add("str", args -> new IVarGetter.Str<NBTBase>() {
            final String arg = args.allLast("null");
            public Object get(NBTBase tag) {
                if(tag == null)return arg;
                if(tag.getId() == 8)return ((NBTTagString)tag).getString();
                return tag.toString();
            }
        });

        WORLD_PRO
            .addStr("name"    , PlayerVarProvider::worldName)
            .addStr("seed"    , PlayerVarProvider::seed     )
            .addStr("time"    , PlayerVarProvider::time     )
            .addStr("tick"    , PlayerVarProvider::meanTT   )
            .addStr("tps"     , PlayerVarProvider::tps      )
            .addInt("chunks"  , PlayerVarProvider::chunks   )
            .addInt("entities", PlayerVarProvider::entities )
            .addInt("players" , PlayerVarProvider::players  )
            .add("server", new IVarGetter<World>() {
                public Object get(World world) {return world.getMinecraftServer();}
                public Class<?> getType() {return MinecraftServer.class;}
            })
        ;

        SERVER_PRO
            .addStr("name", PlayerVarProvider::serverName)
            .addStr("host", PlayerVarProvider::serverHost)
        ;
    }


    //world funcs
    private static String worldName(World world){
        return world.getWorldInfo().getWorldName();
    }
    private static String seed(World world){
        return String.valueOf(world.getSeed());
    }
    private static String time(World world){
        return String.valueOf(world.getTotalWorldTime());
    }
    private static int chunks(World world){
        return world.chunkProviderServer.size();
    }
    private static int entities(World world){
        return world.loadedEntityList.size();
    }
    private static int players(World world){
        return world.playerEntities.size();
    }

    //tps
    private static String meanTT(World world){
        long[] tt = world.getMinecraftServer().worldTickTimes.get(world.provider.getDimension());
        double worldTickTime = mean(tt) * 1.0E-6D;
        return String.format("%.2f", worldTickTime);
    }
    private static String tps(World world){
        double worldTickTime = mean(world.getMinecraftServer().worldTickTimes.get(world.provider.getDimension())) * 1.0E-6D;
        double worldTPS = Math.min(1000.0/worldTickTime, 20);
        return String.format("%.2f", worldTPS);
    }


    //server funcs
    private static String serverName(MinecraftServer server){
        return server.getName();
    }
    private static String serverHost(MinecraftServer server){
        return server.getServerHostname();
    }


    private static double mean(long[] values) {
        long sum = 0L;
        for (long v : values) {
            sum += v;
        }
        return ((double) sum / values.length) * 1.0E-6D;
    }


    private static class SingletonModel implements IPlayerDataModel, Function<String, String>{
        final EntityPlayerMP player;
        final String key, value;

        private SingletonModel(EntityPlayerMP player, String key, String value) {
            this.player = player;
            this.key = key;
            this.value = value;
        }


        @Override
        public EntityPlayerMP getSelfPlayer() {
            return player;
        }


        @Override
        public Function<String, String> model() {
            return this;
        }


        @Override
        public String apply(String name) {
            if(name.equals(key))return value;
            return null;
        }
    }


    private static class Pair2Model extends SingletonModel{
        final String key2, value2;
        private Pair2Model(EntityPlayerMP player, String key, String value, String key2, String value2) {
            super(player, key, value);
            this.key2 = key2;
            this.value2 = value2;
        }
        @Override
        public String apply(String name) {
            if(name.equals(key))return value;
            if(name.equals(key2))return value2;
            return null;
        }
    }


    private static class WrappedMapModel implements IPlayerDataModel, Function<String, String>{
        final EntityPlayerMP player;
        final Map<String,String> model;

        private WrappedMapModel(EntityPlayerMP player, Map<String, String> model) {
            this.player = player;
            this.model = model;
        }


        @Override
        public String apply(String name) {
            return model.get(name);
        }


        @Override
        public EntityPlayerMP getSelfPlayer() {
            return player;
        }
    }


    public static IPlayerDataModel model(EntityPlayerMP player, String key, String arg){
        return new SingletonModel(player, key, arg);
    }


    public static IPlayerDataModel model(EntityPlayerMP player, String key1, String arg1, String key2, String arg2){
        return new Pair2Model(player, key1, arg1, key2, arg2);
    }


    public static IPlayerDataModel model(EntityPlayerMP player, Map<String,String> model){
        return new WrappedMapModel(player, model);
    }
}