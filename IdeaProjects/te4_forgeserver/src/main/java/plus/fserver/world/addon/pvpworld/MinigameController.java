package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.api.TextComponentAuto;
import plus.fserver.api.WorldConstructor;
import plus.fserver.utls.NbtUtils;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.LobbyTeleporter;
import plus.fserver.world.addon.ScoreboardEX;
import plus.fserver.world.addon.pvpworld.stats.PlayerData;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.tson.TsonMap;
import plus.tson.utl.Te4HashSet;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;


public abstract class MinigameController {
    private static final int MIN_PVPA_DIM_ID = 700000;

    private final Long2ObjectOpenHashMap<ExtendedBlockStorage[]> cache = new Long2ObjectOpenHashMap<>(4);
    private final LobbyTeleporter teleporter = new CleanLobbyTeleporter();
    private final Set<Integer> usedDimIds = new Te4HashSet<>();
    private final Set<Integer> freeDimIds = new Te4HashSet<>();
    private NBTTagCompound data;
    private WorldConstructor.Constructor constructor;
    private DimensionType type = DimensionType.OVERWORLD;
    private boolean mayReuseWorld = false;
    private int minPlayers = 2, maxPlayers = 10;

    public MinigameController(){}


    public void init(MinigameProvider.Map.Info pro, TsonMap map){
        data = pro.getMap();
        type = getDimType(map, DimensionType.OVERWORLD);
        map.ifContainsMap("lobby", teleporter::readFrom);
    }


    public void loadConfigurations(TsonMap map){}


    protected static DimensionType getDimType(TsonMap map, DimensionType def){
        String dimType = map.getOrDefaultStr("dim-type", null);
        if(dimType == null)return def;
        try {
            return DimensionType.byName(dimType);
        } catch (Exception e){
            MinecraftServer.LOGGER.error("Invalid dimension: {}", dimType);
            return def;
        }
    }


    public void setMap(NBTTagCompound data){
        this.data = data;
    }


    public void onAddPlayer(EntityPlayerMP player){};


    public void onRemPlayer(MinigameWorld prev, EntityPlayerMP player){};


    public void onPreRemPlayer(EntityPlayerMP player){};


    public void onDeath(LivingDeathEvent event){};


    public void onTick(MinigameWorld world){};


    public WorldData createData(MinigameWorld world){
        return new WorldData();
    }


    public Scoreboard createScoreboard(MinigameWorld world){
        return new ScoreboardEX();
    }


    public int minPlayers(){
        return minPlayers;
    }


    public int maxPlayers(){
        return maxPlayers;
    }


    public void setPlayerLimits(int min, int max){
        this.minPlayers = min;
        this.maxPlayers = max;
    }


    public void onComplete(MinigameWorld world){
        world.finish();
        WorldData data = world.data();
        for (EntityPlayer player:world.playerEntities.toArray(new EntityPlayer[0])){
            if(player instanceof EntityPlayerMP){
                PlayerData stats = data.get(player.getUniqueID());
                if(stats != null){
                    player.changeDimension(stats.getDimToReturn());
                } else {
                    ((EntityPlayerMP)player).connection.disconnect(new TextComponentAuto("Dimension to respawn not founded"));
                }
            }
        }
    }


    private int getFreeId(DimensionType type){
        synchronized (freeDimIds){
            if(!freeDimIds.isEmpty()){
                Iterator<Integer> itr = freeDimIds.iterator();
                while (itr.hasNext()) {
                    Integer id = itr.next();
                    if (DimensionManager.isDimensionRegistered(id) && DimensionManager.getProviderType(id) == type) {
                        freeDimIds.remove(id);
                        return id;
                    }
                }
            }
            int id = getDimFreeId();
            WorldConstructor.register(id, constructor(), type);
            DimensionManager.registerDimension(id, type);
            usedDimIds.add(id);
            return id;
        }
    }


    private static int getDimFreeId(){
        int min = MIN_PVPA_DIM_ID+DimensionManager.getNextFreeDimId();
        while (DimensionManager.isDimensionRegistered(min)){
            ++min;
        }
        return min;
    }


    public void startNewGame(MinecraftServer server, EntityPlayerMP[][] teams, TsonMap settings){
        DimensionType type = this.type;
        boolean shuffleTeams = true, shufflePlayers = true;
        if(settings != null){
            if(settings.containsKey("dim-type"))
                type = getDimType(settings, type);
            shuffleTeams   = settings.getOrDefaultBool("shuffle.team"   , shuffleTeams  );
            shufflePlayers = settings.getOrDefaultBool("shuffle.players", shufflePlayers);
        }

        int dimId = getFreeId(type);
        MinigameWorld world = (MinigameWorld) server.getWorld(dimId);

        if(teams.length > 1 || shuffleTeams)
            FastRandom.shuffle(world.rand, teams, teams.length);

        world.onNewGame(teams);
        WorldData data = world.data();
        for (EntityPlayerMP[] team:teams){
            if(shufflePlayers)
                FastRandom.shuffle(world.rand, team, team.length);

            for (EntityPlayerMP player:team){
                data.stats(player).setDimToReturn(player.dimension);
            }
        }
        onNewGameInitialized(world, teams, settings);

        for (EntityPlayerMP[] team:teams) {
            for (EntityPlayerMP player : team) {
                player.changeDimension(dimId);
            }
        }
    }


    public void onNewGameInitialized(MinigameWorld world, EntityPlayerMP[][] teams, TsonMap settings){}


    public void onNewGameStarted(MinigameWorld world, EntityPlayerMP[] players) {}


    public boolean isImmutable(){return false;}


    public void setDimensionType(DimensionType type){
        this.type = type;
        data.setIntegerU("dimType", type.getId());
    }


    public DimensionType getDimensionType(){
        return type;
    }


    public void setTeleporter(Vec3d lobby, int distance, GameType gameType){
        teleporter.init(lobby, distance, gameType);
        data.setTag("teleporter", teleporter.toNbt());
    }


    public LobbyTeleporter getTeleporter(){
        return teleporter;
    }


    public WorldConstructor.Constructor constructor(){
        if(constructor == null) return constructor = (server, saveHandler, dimId, delegate, profiler) -> new MinigameWorld(server, saveHandler, dimId, delegate, profiler) {
            @Override
            public MinigameController controller() {
                return MinigameController.this;
            }
            @Override
            public void onUnload() {
                super.onUnload();
                synchronized (freeDimIds){
                    freeDimIds.add(provider.getDimension());
                }
            }
        };
        return constructor;
    }



    public NBTTagCompound getMapNbt(){
        return data;
    }


    public Long2ObjectOpenHashMap<ExtendedBlockStorage[]> getMapCache(){
        if(isImmutable())return null;
        return cache;
    }


    //-------------- Functions ---------------------

    public static void autoRespawn(LivingDeathEvent event, int delay, Consumer<EntityPlayerMP> future) {
        Entity entity = event.getEntity();
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) entity;
            event.setCanceled(true);

            player.onPlayerDeath(event.getSource(), false, false);
            GameType prev = player.interactionManager.getGameType();
            player.setGameType(GameType.SPECTATOR);
            MinigameWorld world = (MinigameWorld) player.world;

            UUID uuid = player.getUniqueID();

            world.addDelayedTask(worldServer -> {
                Entity entity0 = worldServer.getEntityFromUuid(uuid);
                if(entity0 instanceof EntityPlayerMP){
                    EntityPlayerMP pl0 = (EntityPlayerMP) entity0;
                    pl0.setGameType(prev);
                    future.accept(pl0);
                }
            }, delay);
        }
    }

}