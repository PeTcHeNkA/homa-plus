package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import plus.fserver.api.TextComponentAuto;
import plus.fserver.api.WorldConstructor;
import plus.fserver.utls.EmptyChunk;
import plus.fserver.utls.NbtUtils;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.LobbyTeleporter;
import plus.fserver.world.addon.MemoryChunkProviderServer;
import plus.fserver.world.addon.pvpworld.stats.WorldData;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;


public abstract class MinigameController {
    private static final Tuple<Chunk,NBTTagCompound> EMPTY = new Tuple<>(EmptyChunk.INSTANCE, new NBTTagCompound(2,1));
    private static final int MIN_PVPA_DIM_ID = 700000;

    private final Long2ObjectOpenHashMap<ExtendedBlockStorage[]> cache = new Long2ObjectOpenHashMap<>();
    private final LobbyTeleporter teleporter = new CleanLobbyTeleporter();
    private final Set<Integer> usedDimIds = new Te4HashSet<>();
    private final Set<Integer> freeDimIds = new Te4HashSet<>();
    private NBTTagCompound data;
    private WorldConstructor.Constructor constructor;
    private DimensionType type = DimensionType.OVERWORLD;
    private boolean mayReuseWorld = false;
    private int minPlayers = 2;
    private int maxPlayers = 10;

    public MinigameController(){}


    public void readFrom(NBTTagCompound data) {
        this.data = data;

        if(data.hasKey("teleporter")){
            teleporter.readFrom(data.getCompoundTag("teleporter"));
        }
        if(data.hasKey("dimType")){
            type = DimensionType.getById(data.getInteger("dimType"));
        }
    }
//
//
//    public boolean setOnAddPlayer(String id){
//        Consumer<EntityPlayerMP> task = TaskRegistry.REGISTRY.getTask(id);
//        onAddPlayer = task;
//        data.setString("onAddPL", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnRemPlayer(String id){
//        Consumer<Tuple<EntityPlayerMP,PvpArena>> task = TaskRegistry.REGISTRY.getTask(id);
//        onRemovePlayer = task;
//        data.setString("onRemPL", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnPreRemPlayer(String id){
//        Consumer<EntityPlayerMP> task = TaskRegistry.REGISTRY.getTask(id);
//        onPreRemovePlayer = task;
//        data.setString("onPreRemPL", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnDeath(String id){
//        Consumer<LivingDeathEvent> task = TaskRegistry.REGISTRY.getTask(id);
//        onDeath = task;
//        data.setString("onDeath", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnTick(String id){
//        Consumer<WorldServer> task = TaskRegistry.REGISTRY.getTask(id);
//        onTick = task;
//        data.setString("onTick", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnInit(String id){
//        Consumer<Tuple<WorldServer,EntityPlayerMP[]>> task = TaskRegistry.REGISTRY.getTask(id);
//        data.setString("onInit", id);
//        return TaskRegistry.isEmpty(task);
//    }
//
//
//    public boolean setOnComplete(String id){
//        Consumer<WorldServer> task = TaskRegistry.REGISTRY.getTask(id);
//        data.setString("onComplete", id);
//        return TaskRegistry.isEmpty(task);
//    }


    public abstract void onAddPlayer(EntityPlayerMP player);


    public abstract void onRemPlayer(MinigameWorld prev, EntityPlayerMP player);


    public abstract void onPreRemPlayer(EntityPlayerMP player);


    public abstract void onDeath(LivingDeathEvent event);


    public abstract void onTick(MinigameWorld world);


    public WorldData createData(MinigameWorld world){
        return new WorldData();
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
        Te4HashMap<UUID, Integer> prevs = world.prevDimMap();
        for (EntityPlayer player:world.playerEntities.toArray(new EntityPlayer[0])){
            if(player instanceof EntityPlayerMP){
                Integer prev = prevs.get(player.getUniqueID());
                if(prev != null){
                    player.changeDimension(prev);
                } else {
                    ((EntityPlayerMP)player).connection.disconnect(new TextComponentAuto("Dimension to respawn not founded"));
                }
            }
        }
    }


    private int getFreeId(){
        synchronized (freeDimIds){
            if(freeDimIds.isEmpty()){
                int id = getDimFreeId();
                WorldConstructor.register(id, constructor(), type);
                DimensionManager.registerDimension(id, type);
                usedDimIds.add(id);
                return id;
            } else {
                Integer id = freeDimIds.iterator().next();
                freeDimIds.remove(id);
                return id;
            }
        }
    }


    private static int getDimFreeId(){
        int min = MIN_PVPA_DIM_ID+DimensionManager.getNextFreeDimId();
        while (DimensionManager.isDimensionRegistered(min)){
            ++min;
        }
        return min;
    }


    public void startNewGame(MinecraftServer server, EntityPlayerMP[][] players){
        int dimId = getDimFreeId();
        MinigameWorld world = (MinigameWorld) server.getWorld(dimId);

        world.onNewGame(players);
        Te4HashMap<UUID, Integer> prevs = world.prevDimMap();
        for (EntityPlayerMP[] team:players){
            for (EntityPlayerMP player:team){
                prevs.fput(player.getUniqueID(), player.dimension);
            }
        }

        TaskRegistry.REGISTRY.getTask(data.getString("onInit")).accept(new Tuple<>(world, players));

        for (EntityPlayerMP[] team:players) {
            for (EntityPlayerMP player : team) {
                player.changeDimension(dimId);
            }
        }

        //onNewGame(world, players);
    }


    public void onNewGameStarted(MinigameWorld world, EntityPlayerMP[] players){
        //((MinigameWorld)world).onNewGame(players);
        //TaskRegistry.REGISTRY.getTask(data.getString("onStart")).accept(new Tuple<>(world, players));
    }


    public abstract boolean isImmutable();


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
//
//
//    public List<List<BlockPos>> spawnPositions(){
//        return spawnPositions;
//    }


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
//
//
//    public void addPos(int group, BlockPos pos){
//        while (spawnPositions.size() <= group){
//            spawnPositions.add(new ArrayList<>());
//        }
//        spawnPositions.get(group).add(pos);
//    }


    public Tuple<Chunk,NBTTagCompound> createChunk(World world, int x, int z){
        String key = MemoryChunkProviderServer.chunkKey(x, z);
        if(data.hasKey(key)){
            NBTTagCompound nbt = data.getCompoundTag(key);
            return new Tuple<>(createFromNbt(nbt, world, x, z), nbt);
        }
        if(isImmutable()){
            return EMPTY;
        }
        return new Tuple<>(new Chunk(world, x, z), EMPTY.B);
    }


    private Chunk createFromNbt(NBTTagCompound nbt, World world, int x, int z){
        if(isImmutable()) {
            ExtendedBlockStorage[] cached;
            synchronized (cache){
                cached = cache.get(ChunkPos.asLong(x, z));
            }
            Chunk chunk;
            if (cached != null) {
                chunk = new Chunk(world, x, z);

                NbtUtils.readCommonChunkData(chunk, nbt);

                ExtendedBlockStorage[] arr = chunk.getBlockStorageArray();
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = cached[i];
                }

                NbtUtils.readChunkCapabilities(chunk, nbt);
            } else {
                chunk = NbtUtils.readChunkFromNBT(world, nbt);
                synchronized (cache) {
                    cache.put(ChunkPos.asLong(x, z), chunk.getBlockStorageArray());
                }
            }
            return chunk;
        } else {
            return NbtUtils.readChunkFromNBT(world, nbt);
        }
    }


    public NBTTagCompound getNbt(){
        return data;
    }


    public static List<List<BlockPos>> readSpawnPositions(NBTTagCompound data){
        List<List<BlockPos>> bGroups = new ArrayList<>();
        if(data.hasKey("spawn_positions")){
            NBTTagList list = data.getTagList("spawn_positions", 9);

            for (int i = 0; i < list.tagCount(); i++){
                NBTTagList group = (NBTTagList) list.get(i);
                if(group.tagCount() == 0)continue;
                List<BlockPos> bList = new ArrayList<>(group.tagCount());
                bGroups.add(bList);

                for (int j = 0; j < group.tagCount(); j+=3){
                    bList.add(new BlockPos(
                            group.getIntAt(j), group.getIntAt(j+1), group.getIntAt(j+2)
                    ));
                }
            }
        }
        return bGroups;
    }


    public static void writeSpawnPositionsTo(NBTTagCompound data, List<List<BlockPos>> bGroups){
        NBTTagList list = new NBTTagList();
        data.setTag("spawn_positions", list);
        for (List<BlockPos> bList:bGroups){
            NBTTagList group = new NBTTagList();
            for (BlockPos pos:bList){
                group.appendTag(NbtUtils.create(pos.getX()));
                group.appendTag(NbtUtils.create(pos.getY()));
                group.appendTag(NbtUtils.create(pos.getZ()));
            }
            list.appendTag(group);
        }
    }


//    public static PvpArena createFromWorld(WorldServer world, int minX, int minZ, int maxX, int maxZ){
//        NBTTagCompound nbt = MemoryChunkProviderServer.dumpToNbt(
//                world.getChunkProvider(), minX, minZ, maxX, maxZ
//        );
//        return new PvpArena(nbt);
//    }


    public static void saveTo(MinigameController arena, File file){
        try {
            //writeSpawnPositionsTo(arena.getNbt(), arena.spawnPositions());
            CompressedStreamTools.write(arena.getNbt(), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//    public static PvpArena readFrom(File file){
//        try {
//            return new PvpArena(CompressedStreamTools.read(file));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    //-------------- Functions ---------------------

    public static void autoRespawn(LivingDeathEvent event, int delay, Consumer<EntityPlayerMP> future) {
        Entity entity = event.getEntity();
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) entity;
            event.setCanceled(true);

            player.onPlayerDeath(event.getSource(), false);
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