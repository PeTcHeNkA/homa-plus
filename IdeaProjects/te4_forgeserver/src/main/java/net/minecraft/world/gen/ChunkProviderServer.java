package net.minecraft.world.gen;

import it.unimi.dsi.fastutil.longs.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.fserver.server.chunk.io.TChunkTask;
import plus.fserver.server.chunk.io.WrappedChunkRunnable;
import plus.fserver.utls.collections.ChunkIteratorPro;
import plus.fserver.utls.collections.ChunkPopulationTask;
import plus.fserver.utls.collections.Long2ChunkMap;


public class ChunkProviderServer extends Long2ChunkMap implements IChunkProvider {
    @Keep
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    private final LongSet droppedChunks = new LongOpenHashSet();
    @Keep
    private LongSet loadingChunks;
    @Keep
    public final IChunkGenerator chunkGenerator;
    @Keep
    public final IChunkLoader chunkLoader;
//    @Keep
//    public final Long2ObjectMap<Chunk> loadedChunks = this;
    @Keep
    public final WorldServer world;
    private final ChunkPopulationTask task;
    private final boolean asyncSaveChunks;


    public ChunkProviderServer(WorldServer world, IChunkLoader chunkLoader, IChunkGenerator chunkGeneratorIn) {
        super(256);
        this.world = world;
        this.chunkLoader = chunkLoader;
        this.chunkGenerator = chunkGeneratorIn;
        task = chunkGeneratorIn.maybeAsyncPopulate()?new ChunkPopulationTask(this):null;
        asyncSaveChunks = (chunkLoader instanceof AnvilChunkLoader) && ElcaConfig.ASYNC_SAVE_CHUNKS;
    }


    public void forChunks(Consumer<Chunk> task) {
        Chunk[] raw = super.value;
        for (Chunk chunk : raw) {
            if (chunk != null) task.accept(chunk);
        }
    }


    public void forEntries(Consumer<PlayerChunkMapEntry> task) {
        Chunk[] raw = super.value;
        for (Chunk chunk : raw) {
            PlayerChunkMapEntry entry;
            if (chunk != null && (entry = chunk.chunkMapEntry) != null)
                task.accept(entry);
        }
    }


    public ChunkIteratorPro forChunks(int radius){
        return new ChunkIteratorPro(super.value, radius);
    }


    public Collection<Chunk> getLoadedChunks() {
        return super.values();
    }


    public synchronized void putChunk(long key, Chunk chunk){
        super.put(key, chunk);
    }


    public synchronized void removeChunk(long key){
        super.remove(key);
    }


    private synchronized void trimToSize(){
        super.trim();
    }


    public void queueUnload(Chunk chunkIn) {
        if (chunkIn.getWorld().provider.canDropChunk(chunkIn.x, chunkIn.z)) {
            this.droppedChunks.add(ChunkPos.asLong(chunkIn.x, chunkIn.z));
            chunkIn.setUnloadQueued();
        }
    }


    public void queueUnloadAll() {
        forChunks(this::queueUnload);
    }


    @Nullable
    public Chunk getLoadedChunk(int x, int z) {
        Chunk chunk = super.get(ChunkPos.asLong(x, z));
        if (chunk != null) chunk.setRequested(); //chunk.unloadQueued = false;
        return chunk;
    }


    @Nullable
    public Chunk loadChunk(int x, int z) {
        return loadChunk(x, z, (Consumer<Chunk>) null);
    }


    @Nullable
    public Chunk loadChunk(int x, int z, @Nullable Runnable runnable) {
        if(runnable != null) {
            return loadChunk(x, z, new WrappedChunkRunnable(runnable));
        } else {
            return loadChunk(x, z, (Consumer<Chunk>) null);
        }
    }


    @Nullable
    public Chunk loadChunk(int x, int z, @Nullable Consumer<Chunk> runnable) {
        Chunk chunk = this.getLoadedChunk(x, z);
        if (chunk == null) {
            long chunkHash = ChunkPos.asLong(x, z);
            WorldServer world = world(x,z);
            chunk = ForgeChunkManager.fetchDormantChunk(chunkHash, world);

            if (chunk != null || !(this.chunkLoader instanceof AnvilChunkLoader)) {
                LongSet loadingChunks;
                if((loadingChunks = this.loadingChunks) == null){
                    this.loadingChunks = loadingChunks = new LongOpenHashSet();
                }
                if (!loadingChunks.add(chunkHash)) FMLLog.bigWarning("There is an attempt to load a chunk ({},{}) in dimension {} that is already being loaded. This will cause weird chunk breakages.", x, z, this.world.provider.getDimension());
                if (chunk == null) chunk = this.loadChunkFromFile(x, z);

                if (chunk != null) {
                    //todo check async?
                    putChunk(chunkHash, chunk);
                    chunk.onLoad();
                    chunk.populate(this, this.chunkGenerator);
                }

                loadingChunks.remove(chunkHash);
            } else {
                AnvilChunkLoader loader = (AnvilChunkLoader) this.chunkLoader;

                if(runnable == null){
                    if(loader.isChunkGeneratedAt(x, z)){
                        chunk = TChunkExecutor.syncChunkLoad(world, loader, this, x, z);
                    } else {
                        chunk = TChunkExecutor.syncChunkGen(world, loader, this, x, z);
                    }
                } else {
                    if(loader.isChunkGeneratedAt(x, z)){
                        TChunkExecutor.queueChunkLoad(world, loader, this, x, z, runnable);
                        return null;
                    } else {
                        if(chunkGenerator.maybeAsync()){
                            TChunkExecutor.queueChunkGenerate(world, loader, this, x, z, runnable);
                            return null;
                        }
                    }
                }
            }
            if(chunk != null) chunk.setRequested();
        }
        if (runnable != null) runnable.accept(chunk);
        return chunk;
    }


    @Override
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.loadChunk(x, z);
        if (chunk == null) {
            long i = ChunkPos.asLong(x, z);
            try {
                chunk = TChunkExecutor.syncChunkGen(world(x,z), (AnvilChunkLoader)this.chunkLoader, this, x, z);
            } catch (Throwable throwable) {
                CrashReport report = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                CrashReportCategory cat = report.makeCategory("Chunk to be generated");
                cat.addCrashSection("Location", String.format("%d,%d", x, z));
                cat.addCrashSection("Position hash", i);
                cat.addCrashSection("Generator", this.chunkGenerator);
                throw new ReportedException(report);
            }
        }
        return chunk;
    }


    protected WorldServer world(int x, int z){
        return world;
    }


    @Nullable
    private Chunk loadChunkFromFile(int x, int z) {
        try {
            World world = world(x, z);
            Chunk chunk = this.chunkLoader.loadChunk(world, x, z);
            if (chunk != null) {
                chunk.setLastSaveTime(world.getTotalWorldTime());
                this.chunkGenerator.recreateStructures(chunk, x, z);
            }
            return chunk;
        } catch (Exception exception) {
            LOGGER.error("Couldn't load chunk", exception);
            return null;
        }
    }


    @Keep
    public void saveChunkExtraData(Chunk chunkIn) {
        try {
            this.chunkLoader.saveExtraChunkData(chunkIn.getWorld(), chunkIn);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save entities", exception);
        }
    }


    @Keep
    public void saveChunkData(Chunk chunk) {
        try {
            WorldServer world = (WorldServer)chunk.getWorld();
            chunk.setLastSaveTime(world.getTotalWorldTime());
            this.chunkLoader.saveChunk(world, chunk);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", ioexception);
        }
        catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
        }
    }


    @Keep
    public void saveChunkData(List<NextTickListEntry> pendings, Chunk chunk) {
        try {
            World world = chunk.getWorld();
            chunk.setLastSaveTime(world.getTotalWorldTime());
            this.chunkLoader.saveChunk(pendings, world, chunk);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", ioexception);
        }
        catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
        }
    }


    @Keep
    public boolean saveChunks(boolean all) {
        boolean needProcess = true;
        int i = 0;
        Collection<Chunk> vs = getLoadedChunks();
        if(vs.isEmpty())return true;
        List<Chunk> list = new ArrayList<>(vs);

        for (int j = 0; j < list.size(); ++j) {
            Chunk chunk = list.get(j);
            if (all) {
                this.saveChunkExtraData(chunk);
            }
            if (chunk.needsSaving(all)) {
                if(needProcess){
                    chunk.getLightingEngine().processLightUpdates();
                    needProcess = false;
                }
                this.saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !all) {
                    return false;
                }
            }
        }
        return true;
    }


    public void onComplete(TChunkTask task){
        Chunk chunk = task.getChunk();
        putChunk(ChunkPos.asLong(chunk.x, chunk.z), chunk);
        chunk.onLoad();
        if(this.task != null) {
            this.task.add(chunk);
        } else {
            chunk.populate(ChunkProviderServer.this, chunkGenerator);
        }
    }


    public void flushToDisk() {
        this.chunkLoader.flush();
    }


    private int ticks = 0;
    @Override
    public boolean tick() {
        if(task != null) task.reRunIfNeed();
        if (!this.world.disableLevelSaving) {
            LongSet droppedChunks = this.droppedChunks;
            int size = droppedChunks.size();

            if(size != 0){
                if (size >= 100) {
                    int i = 0;
                    for (LongIterator iterator = droppedChunks.iterator();
                         i < 100 && iterator.hasNext();) {
                        long olong = iterator.next();
                        Chunk chunk = super.get(olong);

                        l1: if (chunk != null) {
                            if(chunk.maybeUnload()) {
                                if(asyncSaveChunks){
                                    boolean res = TChunkExecutor.queueChunkUnload((WorldServer)chunk.getWorld(), (AnvilChunkLoader)chunkLoader,this, chunk);
                                    if(!res)break l1;
                                } else {
                                    chunk.onUnload();
                                    ForgeChunkManager.putDormantChunk(olong, chunk);
                                    this.saveChunkData(chunk);
                                    this.saveChunkExtraData(chunk);
                                }
                                removeChunk(olong);
                                ++i;
                                iterator.remove();
                            } else {
                                chunk.setRequested(false);
                            }
                        }
                    }
                    trimToSize();
                } else {
                    int i = 0;
                    for (long olong : droppedChunks) {
                        Chunk chunk = super.get(olong);

                        l1: if (chunk != null) {
                            if (chunk.maybeUnload()) {
                                if (asyncSaveChunks) {
                                    boolean res = TChunkExecutor.queueChunkUnload((WorldServer) chunk.getWorld(), (AnvilChunkLoader) chunkLoader, this, chunk);
                                    if(!res)break l1;
                                } else {
                                    chunk.onUnload();
                                    ForgeChunkManager.putDormantChunk(olong, chunk);
                                    this.saveChunkData(chunk);
                                    this.saveChunkExtraData(chunk);
                                }
                                removeChunk(olong);
                                ++i;
                            } else {
                                chunk.setRequested(false);
                            }
                        }
                    }
                    droppedChunks.clear();
                    if(i > 32) trimToSize();
                }
            }

            if (!super.isEmpty()) {
                if(++ticks>1200){
                    LongSet lc = loadingChunks;
                    if(lc != null && lc.isEmpty())loadingChunks = null;

                    int limit = 0;
                    Iterator<Chunk> iter = getLoadedChunks().iterator();
                    Chunk chunk;
                    while (iter.hasNext() && limit<100){
                        try {
                            chunk = iter.next();
                        } catch (NullPointerException e){
                            world.getMinecraftServer().ticker().onError(world, e);
                            break;
                        }
                        l1: if(chunk != null) {
                            if (chunk.maybeUnload()) {
                                long olong = ChunkPos.asLong(chunk.x, chunk.z);

                                if(asyncSaveChunks){
                                    boolean res = TChunkExecutor.queueChunkUnload((WorldServer)chunk.getWorld(), (AnvilChunkLoader)chunkLoader,this, chunk);
                                    if(!res)break l1;
                                } else {
                                    chunk.onUnload();
                                    ForgeChunkManager.putDormantChunk(olong, chunk);
                                    this.saveChunkData(chunk);
                                    this.saveChunkExtraData(chunk);
                                }

                                removeChunk(olong);
                                iter.remove();
                                ++limit;
                            } else {
                                chunk.setRequested(false);
                            }
                        }
                    }
                    if(limit > 32) trimToSize();
                    if(limit >= 100){
                        ticks = 1190;
                    } else {
                        ticks = 0;
                    }
                }
            }
            this.chunkLoader.chunkTick();
        }
        return false;
    }


    public boolean canSave() {
        return !this.world.disableLevelSaving;
    }


    @Override
    public String makeString() {
        return "ServerChunkCache: " + super.size() + " Drop: " + this.droppedChunks.size();
    }


    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.chunkGenerator.getPossibleCreatures(creatureType, pos);
    }


    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return this.chunkGenerator.getNearestStructurePos(worldIn, structureName, position, findUnexplored);
    }


    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return this.chunkGenerator.isInsideStructure(worldIn, structureName, pos);
    }


    public int getLoadedChunkCount() {
        return super.size();
    }


    public boolean chunkExists(int x, int z) {
        return super.containsKey(ChunkPos.asLong(x, z));
    }


//    public boolean isChunkLoadedSafe(int x, int z) {
//        Chunk chunk = loadedChunks.get(ChunkPos.asLong(x, z));
//        return chunk == null || chunk.isNotUnloadQueued();
//    }


    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return super.containsKey(ChunkPos.asLong(x, z)) || this.chunkLoader.isChunkGeneratedAt(x, z);
    }
}