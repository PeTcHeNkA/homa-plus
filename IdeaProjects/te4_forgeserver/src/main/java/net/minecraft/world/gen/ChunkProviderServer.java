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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.fserver.server.chunk.io.TChunkGenProvider;


public class ChunkProviderServer implements IChunkProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LongSet droppedChunks = new LongOpenHashSet();
    private final LongSet loadingChunks = new LongOpenHashSet();
    public final IChunkGenerator chunkGenerator;
    public final IChunkLoader chunkLoader;
    public final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<>(8192);
    public final WorldServer world;
    private final Long2ObjectOpenHashMap realLC = (Long2ObjectOpenHashMap) loadedChunks;


    public ChunkProviderServer(WorldServer worldObjIn, IChunkLoader chunkLoaderIn, IChunkGenerator chunkGeneratorIn) {
        this.world = worldObjIn;
        this.chunkLoader = chunkLoaderIn;
        this.chunkGenerator = chunkGeneratorIn;
    }


    public Collection<Chunk> getLoadedChunks() {
        return this.loadedChunks.values();
    }


    public void queueUnload(Chunk chunkIn) {
        if (this.world.provider.canDropChunk(chunkIn.x, chunkIn.z)) {
            this.droppedChunks.add(ChunkPos.asLong(chunkIn.x, chunkIn.z));
            chunkIn.setUnloadQueued();
            //chunkIn.unloadQueued = true;
        }
    }


    public void queueUnloadAll() {
        ObjectIterator<Chunk> iter = this.loadedChunks.values().iterator();
        while (iter.hasNext()) {
            Chunk chunk = iter.next();
            this.queueUnload(chunk);
        }
    }


    @Nullable
    public Chunk getLoadedChunk(int x, int z) {
        Chunk chunk = this.loadedChunks.get(ChunkPos.asLong(x, z));
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
                    this.loadedChunks.put(chunkHash, chunk);
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


    private void saveChunkExtraData(Chunk chunkIn) {
        try {
            this.chunkLoader.saveExtraChunkData(this.world, chunkIn);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't save entities", exception);
        }
    }


    private void saveChunkData(Chunk chunkIn) {
        try {
            chunkIn.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkLoader.saveChunk(this.world, chunkIn);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", ioexception);
        }
        catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
        }
    }


    public boolean saveChunks(boolean all) {
        int i = 0;
        ObjectCollection<Chunk> vs = this.loadedChunks.values();
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


    public void onComplete(TChunkGenProvider provider){
        Chunk chunk = provider.getChunk();
        loadedChunks.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);
        chunk.onLoad();
        if(chunkGenerator.maybeAsyncPopulate()) {
            world.addAsyncTask(
                    () -> provider.getChunk().populate(ChunkProviderServer.this, chunkGenerator)
            );
        } else {
            provider.getChunk().populate(ChunkProviderServer.this, chunkGenerator);
        }
    }


    public void flushToDisk() {
        this.chunkLoader.flush();
    }


    private int ticks = 0;
    @Override
    public boolean tick() {
        if (!this.world.disableLevelSaving) {
            if (!this.droppedChunks.isEmpty()) {
                for (ChunkPos forced : this.world.getPersistentChunks().keySet()) {
                    this.droppedChunks.remove(ChunkPos.asLong(forced.x, forced.z));
                }
                int i = 0;
                for (LongIterator iterator = this.droppedChunks.iterator();
                     i < 100 && iterator.hasNext();) {
                    long olong = iterator.next();
                    Chunk chunk = this.loadedChunks.get(olong);

                    if (chunk != null) {
                        if(chunk.maybeUnload()) {
                            chunk.onUnload();
                            ForgeChunkManager.putDormantChunk(olong, chunk);
                            this.saveChunkData(chunk);
                            this.saveChunkExtraData(chunk);
                            this.loadedChunks.remove(olong);
                            ++i;
                            iterator.remove();
                        } else {
                            chunk.setRequested(false);
                        }
                    }
                }
                if(i > 10) realLC.trim();
            }
            if (this.loadedChunks.isEmpty()) {
                DimensionManager.unloadWorld(this.world.provider.getDimension());
            } else {
                if(++ticks>1200){
                    int limit = 0;
                    Iterator<Chunk> iter = loadedChunks.values().iterator();
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
                                ForgeChunkManager.putDormantChunk(olong, chunk);
                                this.saveChunkData(chunk);
                                this.saveChunkExtraData(chunk);
                                iter.remove();
                                this.loadedChunks.remove(olong);
                                ++limit;
                            } else {
                                chunk.setRequested(false);
                            }
                        }
                    }
                    if(limit>10) realLC.trim();
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
        return "ServerChunkCache: " + this.loadedChunks.size() + " Drop: " + this.droppedChunks.size();
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
        return this.loadedChunks.size();
    }


    public boolean chunkExists(int x, int z) {
        return this.loadedChunks.containsKey(ChunkPos.asLong(x, z));
    }


    public boolean isChunkLoadedSafe(int x, int z) {
        Chunk chunk = loadedChunks.get(ChunkPos.asLong(x, z));
        return chunk == null || chunk.isNotUnloadQueued();
    }


    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return this.loadedChunks.containsKey(ChunkPos.asLong(x, z)) || this.chunkLoader.isChunkGeneratedAt(x, z);
    }
}