package plus.fserver.world.addon.pvpworld;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import plus.fserver.api.WorldConstructor;
import plus.fserver.utls.EmptyChunk;
import plus.fserver.utls.NbtUtils;
import plus.fserver.world.addon.CleanLobbyTeleporter;
import plus.fserver.world.addon.LobbyTeleporter;
import plus.fserver.world.addon.MemoryChunkProviderServer;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class PvpArena {
    private static Tuple<Chunk,NBTTagCompound> EMPTY = new Tuple<>(EmptyChunk.INSTANCE, new NBTTagCompound(2,1));
    private final Long2ObjectOpenHashMap<ExtendedBlockStorage[]> cache = new Long2ObjectOpenHashMap<>();
    private final NBTTagCompound data;
    private boolean immutable = true;
    private List<List<BlockPos>> spawnPositions;
    private LobbyTeleporter teleporter;
    private WorldConstructor.Constructor constructor;
    private DimensionType type = DimensionType.OVERWORLD;
    private Consumer<EntityPlayerMP> onAddPlayer                       = TaskRegistry.empty();
    private Consumer<Tuple<EntityPlayerMP,PvpArena>> onRemovePlayer    = TaskRegistry.empty();
    private Consumer<EntityPlayerMP> onPreRemovePlayer                 = TaskRegistry.empty();
    private Consumer<EntityPlayerMP> onDeath                           = TaskRegistry.empty();
    private Consumer<WorldServer>    onTick                            = TaskRegistry.empty();

    public PvpArena(NBTTagCompound data) {
        this.data = data;
        this.immutable = data.getBoolean("isImmutable");
        spawnPositions = readSpawnPositions(data);
        if(data.hasKey("teleporter")){
            teleporter = new CleanLobbyTeleporter();
            teleporter.readFrom(data.getCompoundTag("teleporter"));
        } else {
            teleporter = new CleanLobbyTeleporter();
        }
        if(data.hasKey("dimType")){
            type = DimensionType.getById(data.getInteger("dimType"));
        }
        onAddPlayer       = TaskRegistry.REGISTRY.getTask(data.getString("onAddPL")   );
        onRemovePlayer    = TaskRegistry.REGISTRY.getTask(data.getString("onRemPL")   );
        onPreRemovePlayer = TaskRegistry.REGISTRY.getTask(data.getString("onPreRemPL"));
        onDeath           = TaskRegistry.REGISTRY.getTask(data.getString("onDeath")   );
        onTick            = TaskRegistry.REGISTRY.getTask(data.getString("onTick")    );
    }


    public boolean setOnAddPlayer(String id){
        Consumer<EntityPlayerMP> task = TaskRegistry.REGISTRY.getTask(id);
        onAddPlayer = task;
        data.setString("onAddPL", id);
        return TaskRegistry.isEmpty(task);
    }


    public boolean setOnRemPlayer(String id){
        Consumer<Tuple<EntityPlayerMP,PvpArena>> task = TaskRegistry.REGISTRY.getTask(id);
        onRemovePlayer = task;
        data.setString("onRemPL", id);
        return TaskRegistry.isEmpty(task);
    }


    public boolean setOnPreRemPlayer(String id){
        Consumer<EntityPlayerMP> task = TaskRegistry.REGISTRY.getTask(id);
        onPreRemovePlayer = task;
        data.setString("onPreRemPL", id);
        return TaskRegistry.isEmpty(task);
    }


    public boolean setOnDeath(String id){
        Consumer<EntityPlayerMP> task = TaskRegistry.REGISTRY.getTask(id);
        onDeath = task;
        data.setString("onDeath", id);
        return TaskRegistry.isEmpty(task);
    }


    public boolean setOnTick(String id){
        Consumer<WorldServer> task = TaskRegistry.REGISTRY.getTask(id);
        onTick = task;
        data.setString("onTick", id);
        return TaskRegistry.isEmpty(task);
    }


    public boolean setOnInit(String id){
        Consumer<WorldServer> task = TaskRegistry.REGISTRY.getTask(id);
        onTick = task;
        data.setString("onInit", id);
        return TaskRegistry.isEmpty(task);
    }


    public void onAddPlayer(EntityPlayerMP player){
        onAddPlayer.accept(player);
    }


    public void onRemPlayer(EntityPlayerMP player){
        onRemovePlayer.accept(new Tuple<>(player, this));
    }


    public void onPreRemPlayer(EntityPlayerMP player){
        onPreRemovePlayer.accept(player);
    }


    public void onDeath(EntityPlayerMP player){
        onDeath.accept(player);
    }


    public void onTick(WorldServer world){
        onTick.accept(world);
    }


    public void setImmutable(boolean b){
        this.immutable = b;
        data.setBoolean("isImmutable", b);
    }


    public boolean isImmutable(){
        return immutable;
    }


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


    public List<List<BlockPos>> spawnPositions(){
        return spawnPositions;
    }


    public WorldConstructor.Constructor constructor(){
        if(constructor == null) return constructor = (server, saveHandler, dimId, delegate, profiler) -> new PvpWorld(server, saveHandler, dimId, delegate, profiler) {
            @Override
            public World init() {
                super.init();
                TaskRegistry.REGISTRY.getTask(data.getString("onInit")).accept(this);
                return this;
            }

            @Override
            PvpArena getArena() {
                return PvpArena.this;
            }
        };
        return constructor;
    }


    public void addPos(int group, BlockPos pos){
        while (spawnPositions.size() <= group){
            spawnPositions.add(new ArrayList<>());
        }
        spawnPositions.get(group).add(pos);
    }


    public Tuple<Chunk,NBTTagCompound> createChunk(World world, int x, int z){
        String key = MemoryChunkProviderServer.chunkKey(x, z);
        if(data.hasKey(key)){
            NBTTagCompound nbt = data.getCompoundTag(key);
            return new Tuple<>(createFromNbt(nbt, world, x, z), nbt);
        }
        if(immutable){
            return EMPTY;
        }
        return new Tuple<>(new Chunk(world, x, z), EMPTY.B);
    }


    private Chunk createFromNbt(NBTTagCompound nbt, World world, int x, int z){
        if(immutable) {
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


    public static PvpArena createFromWorld(WorldServer world, int minX, int minZ, int maxX, int maxZ){
        NBTTagCompound nbt = MemoryChunkProviderServer.dumpToNbt(
                world.getChunkProvider(), minX, minZ, maxX, maxZ
        );
        return new PvpArena(nbt);
    }


    public static void saveTo(PvpArena arena, File file){
        try {
            writeSpawnPositionsTo(arena.getNbt(), arena.spawnPositions());
            CompressedStreamTools.write(arena.getNbt(), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static PvpArena readFrom(File file){
        try {
            return new PvpArena(CompressedStreamTools.read(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}