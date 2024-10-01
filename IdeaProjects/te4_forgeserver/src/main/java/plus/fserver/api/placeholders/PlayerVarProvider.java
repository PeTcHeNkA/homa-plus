package plus.fserver.api.placeholders;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.tson.format.IVarGetter;
import java.util.function.Function;


public class PlayerVarProvider extends TreeVarProvider<EntityPlayerMP,PlayerVarProvider>{
    public static PlayerVarProvider PROVIDER = new PlayerVarProvider();

    public PlayerVarProvider wAdd(String name, IVarGetter<World> func){
        return super.add(name, (IVarGetter<EntityPlayerMP>) playerMP -> func.get(playerMP.world));
    }

    public PlayerVarProvider sAdd(String name, IVarGetter<MinecraftServer> func){
        return super.add(name, (IVarGetter<EntityPlayerMP>) playerMP -> func.get(playerMP.server));
    }

    @Override
    protected PlayerVarProvider create() {
        return new PlayerVarProvider();
    }

    static {
        PROVIDER.add("player")
                .add("name", PlayerVarProvider::userName)
                .add("ip"  , PlayerVarProvider::ip      )
                .add("uuid", PlayerVarProvider::uuid    )
                .add("ping", PlayerVarProvider::ping    )
                .add("pos" , PlayerVarProvider::pos     )
                .add("posX", PlayerVarProvider::posX    )
                .add("posY", PlayerVarProvider::posY    )
                .add("posZ", PlayerVarProvider::posZ    )
                .add("hp"  , PlayerVarProvider::health  )
                .add("exp" , PlayerVarProvider::expLvl  )
                .add("tag", (Function<Args, IVarGetter<EntityPlayerMP>>) args -> {
                    String arg = args.allLast();
                    return playerMP -> {
                        NBTBase tag = playerMP.getEntityData().getTag(arg);
                        if(tag.getId() == 8)return ((NBTTagString)tag).getString();
                        return tag.toString();
                    };
                })
        ;

        PROVIDER.add("world")
                .wAdd("name"    , PlayerVarProvider::worldName)
                .wAdd("seed"    , PlayerVarProvider::seed     )
                .wAdd("time"    , PlayerVarProvider::time     )
                .wAdd("tick"    , PlayerVarProvider::meanTT   )
                .wAdd("tps"     , PlayerVarProvider::tps      )
                .wAdd("chunks"  , PlayerVarProvider::chunks   )
                .wAdd("entities", PlayerVarProvider::entities )
                .wAdd("players" , PlayerVarProvider::players  )
        ;

        PROVIDER.add("server")
                .sAdd("name", PlayerVarProvider::serverName)
                .sAdd("host", PlayerVarProvider::serverHost)
        ;
    }


    //player funcs
    private static String userName(EntityPlayerMP player){
        return player.getName();
    }
    private static String ip(EntityPlayerMP player){
        return player.getPlayerIP();
    }
    private static String uuid(EntityPlayerMP player){
        return player.getUniqueID().toString();
    }
    private static String ping(EntityPlayerMP player){
        return String.valueOf(player.ping);
    }
    private static String pos(EntityPlayerMP player){
        return player.getPosition().toString();
    }
    private static String posX(EntityPlayerMP player){
        return String.valueOf(MathHelper.floor(player.posX));
    }
    private static String posY(EntityPlayerMP player){
        return String.valueOf(MathHelper.floor(player.posY));
    }
    private static String posZ(EntityPlayerMP player){
        return String.valueOf(MathHelper.floor(player.posZ));
    }
    private static String health(EntityPlayerMP player){
        return String.format("%.2f", player.getHealth());
    }
    private static String expLvl(EntityPlayerMP player){
        return String.valueOf(player.experienceLevel);
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
    private static String chunks(World world){
        return String.valueOf(world.chunkProviderServer.size());
    }
    private static String entities(World world){
        return String.valueOf(world.loadedEntityList.size());
    }
    private static String players(World world){
        return String.valueOf(world.playerEntities.size());
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
}
