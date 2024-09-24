package net.minecraft.world.gen;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
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
import plus.fserver.utls.collections.ChunkPopulationTask;


public class ChunkProviderServer extends Long2ObjectOpenHashMap<Chunk> implements IChunkProvider {
    @Keep
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    private final LongSet droppedChunks = new LongOpenHashSet();
    @Keep
    private final LongSet loadingChunks = new LongOpenHashSet();
    @Keep
    public final IChunkGenerator chunkGenerator;
    @Keep
    public final IChunkLoader chunkLoader;
    @Keep
    public final Long2ObjectMap<Chunk> loadedChunks = this;
    @Keep
    public final WorldServer world;
    private final ChunkPopulationTask task;
    private final boolean asyncSaveChunks;


    public ChunkProviderServer(WorldServer world, IChunkLoader chunkLoader, IChunkGenerator chunkGeneratorIn) {
        super(1024);
        this.world = world;
        this.chunkLoader = chunkLoader;
        this.chunkGenerator = chunkGeneratorIn;
        task = chunkGeneratorIn.maybeAsyncPopulate()?new ChunkPopulationTask(this):null;
        asyncSaveChunks = (chunkLoader instanceof AnvilChunkLoader) && ElcaConfig.ASYNC_SAVE_CHUNKS;
    }


    public Collection<Chunk> getLoadedChunks() {
        return super.values();
    }


    public void queueUnload(Chunk chunkIn) {
        if (this.world.provider.canDropChunk(chunkIn.x, chunkIn.z)) {
            this.droppedChunks.add(ChunkPos.asLong(chunkIn.x, chunkIn.z));
            chunkIn.setUnloadQueued();
            //chunkIn.unloadQueued = true;
        }
    }


    public void queueUnloadAll() {
        ObjectIterator<Chunk> iter = super.values().iterator();
        while (iter.hasNext()) {
            Chunk chunk = iter.next();
            this.queueUnload(chunk);
        }
    }


    @Nullable
    public Chunk getLoadedChunk(int x, int z) {
        Chunk chunk = super.get(ChunkPos.asLong(x, z));
        if (chunk != null) chunk.setRequested(); //chunk.unloadQueued = false;
        return chunk;
    }


    @Nullable
    public Chunk loadChunk(int x, int z) {
        return loadChunk(x, z, null);
    }


    @Nullable
    public Chunk loadChunk(int x, int z, @Nullable Runnable runnable) {
        Chunk chunk = this.getLoadedChunk(x, z);
        if (chunk == null) {
            long chunkHash = ChunkPos.asLong(x, z);
            chunk = ForgeChunkManager.fetchDormantChunk(chunkHash, this.world);
            if (chunk != null || !(this.chunkLoader instanceof AnvilChunkLoader)) {
                if (!loadingChunks.add(chunkHash)) FMLLog.bigWarning("There is an attempt to load a chunk ({},{}) in dimension {} that is already being loaded. This will cause weird chunk breakages.", x, z, this.world.provider.getDimension());
                if (chunk == null) chunk = this.loadChunkFromFile(x, z);

                if (chunk != null) {
                    super.put(chunkHash, chunk);
                    chunk.onLoad();
                    chunk.populate(this, this.chunkGenerator);
                }

                loadingChunks.remove(chunkHash);
            } else {
                AnvilChunkLoader loader = (AnvilChunkLoader) this.chunkLoader;

                if(runnable == null){
                    if(loader.isChunkGeneratedAt(x, z)){
                        chunk = TChunkExecutor.syncChunkLoad(this.world, loader, this, x, z);
                    } else {
                        chunk = TChunkExecutor.syncChunkGen(this.world, loader, this, x, z);
                    }
                } else {
                    if(loader.isChunkGeneratedAt(x, z)){
                        TChunkExecutor.queueChunkLoad(this.world, loader, this, x, z, runnable);
                        return null;
                    }else {
                        if(chunkGenerator.maybeAsync()){
                            TChunkExecutor.queueChunkGenerate(this.world, loader, this, x, z, runnable);
                            return null;
                        }
                    }
                }
            }
            if(chunk != null) chunk.setRequested();
        }
        if (runnable != null) runnable.run();
        return chunk;
    }


    @Override
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.loadChunk(x, z);
        if (chunk == null) {
            long i = ChunkPos.asLong(x, z);
            try {
                chunk = TChunkExecutor.syncChunkGen(world, (AnvilChunkLoader)this.chunkLoader, this, x, z);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                crashreportcategory.addCrashSection("Location", String.format("%d,%d", x, z));
                crashreportcategory.addCrashSection("Position hash", i);
                crashreportcategory.addCrashSection("Generator", this.chunkGenerator);
                throw new ReportedException(crashreport);
            }
        }
        return chunk;
    }


    @Nullable
    private Chunk loadChunkFromFile(int x, int z) {
        try {
            Chunk chunk = this.chunkLoader.loadChunk(this.world, x, z);
            if (chunk != null) {
                chunk.setLastSaveTime(this.world.getTotalWorldTime());
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
            this.chunkLoader.saveExtraChunkData(this.world, chunkIn);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't save entities", exception);
        }
    }


    @Keep
    public void saveChunkData(Chunk chunk) {
        try {
            chunk.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkLoader.saveChunk(this.world, chunk);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", ioexception);
        }
        catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
        }
    }


    @Keep
    public void saveChunkData(List<NextTickListEntry> pendings, Chunk chunkIn) {
        try {
            chunkIn.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkLoader.saveChunk(pendings, this.world, chunkIn);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", ioexception);
        }
        catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
        }
    }


    @Keep
    public boolean saveChunks(boolean all) {
        int i = 0;
        ObjectCollection<Chunk> vs = super.values();
        if(vs.isEmpty())return true;
        List<Chunk> list = new ArrayList<>(vs);

        for (int j = 0; j < list.size(); ++j) {
            Chunk chunk = list.get(j);

            if (all) {
                this.saveChunkExtraData(chunk);
            }
            if (chunk.needsSaving(all)) {
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
        super.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);
        chunk.onLoad();
        if(this.task != null) {
            this.task.add(task.getChunk());
        } else {
            task.getChunk().populate(ChunkProviderServer.this, chunkGenerator);
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
            if (!this.droppedChunks.isEmpty()) {
//                for (ChunkPos forced : this.world.getPersistentChunks().keySet()) {
//                    this.droppedChunks.remove(ChunkPos.asLong(forced.x, forced.z));
//                }
                int i = 0;
                for (LongIterator iterator = this.droppedChunks.iterator();
                     i < 100 && iterator.hasNext();) {
                    long olong = iterator.next();
                    Chunk chunk = super.get(olong);

                    if (chunk != null) {
                        if(chunk.maybeUnload()) {
                            chunk.onUnload();
                            if(asyncSaveChunks){
                                TChunkExecutor.queueChunkUnload(world, (AnvilChunkLoader)chunkLoader,this, chunk);
                            } else {
                                ForgeChunkManager.putDormantChunk(olong, chunk);
                                this.saveChunkData(chunk);
                                this.saveChunkExtraData(chunk);
                            }
                            super.remove(olong);
                            ++i;
                            iterator.remove();
                        } else {
                            chunk.setRequested(false);
                        }
                    }
                }
                if(i > 10) super.trim();
            }
            if (super.isEmpty()) {
                //DimensionManager.unloadWorld(this.world.provider.getDimension());
            } else {
                if(++ticks>1200){
                    int limit = 0;
                    Iterator<Chunk> iter = super.values().iterator();
                    Chunk chunk;
                    while (iter.hasNext() && limit<100){
                        try {
                            chunk = iter.next();
                        } catch (NullPointerException e){
                            world.getMinecraftServer().ticker().onError(world, e);
                            break;
                        }
                        if(chunk != null) {
                            if (chunk.maybeUnload()) {
                                long olong = ChunkPos.asLong(chunk.x, chunk.z);
                                chunk.onUnload();

                                if(asyncSaveChunks){
                                    TChunkExecutor.queueChunkUnload(world, (AnvilChunkLoader)chunkLoader,this, chunk);
                                } else {
                                    ForgeChunkManager.putDormantChunk(olong, chunk);
                                    this.saveChunkData(chunk);
                                    this.saveChunkExtraData(chunk);
                                }

                                super.remove(olong);
                                iter.remove();
                                ++limit;
                            } else {
                                chunk.setRequested(false);
                            }
                        }
                    }
                    if(limit>10) super.trim();
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