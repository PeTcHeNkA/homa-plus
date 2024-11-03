package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.server.SStrings;
import plus.fserver.utls.NbtUtils;
import plus.fserver.world.light.LightingHooks;


public class AnvilChunkLoader implements IChunkLoader, IThreadedFileIO, Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ChunkPos, NBTTagCompound> chunksToSave = Maps.newConcurrentMap();
    private final Set<ChunkPos> chunksBeingSaved = Collections.newSetFromMap(Maps.newConcurrentMap());
    public final File chunkSaveLocation;
    private final DataFixer fixer;
    private boolean flushing;

    public AnvilChunkLoader(File chunkSaveLocationIn, DataFixer dataFixerIn) {
        this.chunkSaveLocation = chunkSaveLocationIn;
        this.fixer = dataFixerIn;
    }

    @Deprecated // TODO: remove (1.13)
    public boolean chunkExists(World world, int x, int z) {
        return isChunkGeneratedAt(x, z);
    }


    @Nullable
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        Object[] data = this.loadChunk__Async(worldIn, x, z);

        if (data != null) {
            Chunk chunk = (Chunk) data[0];
            NBTTagCompound nbttagcompound = (NBTTagCompound) data[1];
            this.loadEntities(worldIn, nbttagcompound.getCompoundTag(SStrings.CL_LEVEL), chunk);
            return chunk;
        }

        return null;
    }


    @Nullable
    public Object[] loadChunk__Async(World world, int x, int z) throws IOException {
        ChunkPos chunkpos = new ChunkPos(x, z);
        NBTTagCompound nbttagcompound = this.chunksToSave.get(chunkpos);

        if (nbttagcompound == null) {
            DataInputStream datainputstream = RegionFileCache.getChunkInputStream(this.chunkSaveLocation, x, z);

            if (datainputstream == null) {
                return null;
            }

            nbttagcompound = this.fixer.process(FixTypes.CHUNK, CompressedStreamTools.read(datainputstream));

            datainputstream.close(); // Forge: close stream after use

            return this.checkedReadChunkFromNBT__Async(world, x, z, nbttagcompound);
        } else {
            synchronized (nbttagcompound){
                return this.checkedReadChunkFromNBT__Async(world, x, z, nbttagcompound);
            }
        }
    }


    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        ChunkPos chunkpos = new ChunkPos(x, z);
        NBTTagCompound nbttagcompound = this.chunksToSave.get(chunkpos);
        return nbttagcompound != null || RegionFileCache.chunkExists(this.chunkSaveLocation, x, z);
    }


    @Nullable
    protected Chunk checkedReadChunkFromNBT(World worldIn, int x, int z, NBTTagCompound compound) {
        Object[] data = this.checkedReadChunkFromNBT__Async(worldIn, x, z, compound);
        return data != null ? (Chunk)data[0] : null;
    }


    @Nullable
    protected Object[] checkedReadChunkFromNBT__Async(World worldIn, int x, int z, NBTTagCompound compound) {
        if (!compound.hasKey(SStrings.CL_LEVEL, 10)) {
            LOGGER.error("Chunk file at {},{} is missing level data, skipping", x, z);
            return null;
        } else {
            NBTTagCompound nbttagcompound = compound.getCompoundTag(SStrings.CL_LEVEL);

            if (!nbttagcompound.hasKey(SStrings.CL_SECTIONS, 9)) {
                LOGGER.error("Chunk file at {},{} is missing block data, skipping", x, z);
                return null;
            } else {
                Chunk chunk = this.readChunkFromNBT(worldIn, nbttagcompound);

                if (!chunk.isAtLocation(x, z)) {
                    LOGGER.error("Chunk file at {},{} is in the wrong location; relocating. (Expected {}, {}, got {}, {})", x, z, x, z, chunk.x, chunk.z);
                    nbttagcompound.setInteger("xPos", x);
                    nbttagcompound.setInteger("zPos", z);

                    // Have to move tile entities since we don't load them at this stage
                    NBTTagList _tileEntities = nbttagcompound.getTagList("TileEntities", 10);

                    if (_tileEntities != null) {
                        for (int te = 0; te < _tileEntities.tagCount(); te++) {
                            NBTTagCompound _nbt = _tileEntities.getCompoundTagAt(te);
                            _nbt.setInteger("x", x * 16 + (_nbt.getInteger("x") - chunk.x * 16));
                            _nbt.setInteger("z", z * 16 + (_nbt.getInteger("z") - chunk.z * 16));
                        }
                    }
                    chunk = this.readChunkFromNBT(worldIn, nbttagcompound);
                }

                Object[] data = new Object[2];
                data[0] = chunk;
                data[1] = compound;
                // event is fired in ChunkIOProvider.callStage2 since it must be fired after TE's load.
                // MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, par4NBTTagCompound));
                return data;
            }
        }
    }


    @Override
    public void saveChunk(@Nullable List<NextTickListEntry> pendings, World worldIn, Chunk chunkIn, boolean async) throws MinecraftException {
        worldIn.checkSessionLock();
        try {
            NBTTagCompound compound = new NBTTagCompound(8);
            NBTTagCompound compound1 = new NBTTagCompound();
            compound.setTag(SStrings.CL_LEVEL, compound1);
            compound.setTag("DataVersion", NbtUtils.DATA_VERSION);
            FMLCommonHandler.instance().getDataFixer().writeVersionData(compound);
            this.writeChunkToNBT(pendings, chunkIn, worldIn, compound1);
            ForgeChunkManager.storeChunkNBT(chunkIn, compound1);
            MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunkIn, compound));
            this.addChunkToPending(chunkIn.getWorld(), chunkIn.getPos(), compound, async);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }
    }


    @Override
    public void saveChunk(World worldIn, Chunk chunk) throws MinecraftException {
        chunk.getLightingEngine().awaitUnlock();

        List<NextTickListEntry> pendings = chunk.getWorld().getPendingBlockUpdates(chunk, false);
        saveChunk(pendings, worldIn, chunk, true);
    }


    protected void addChunkToPending(World world, ChunkPos pos, NBTTagCompound compound, boolean async) {
        if (!this.chunksBeingSaved.contains(pos)) {
            this.chunksToSave.put(pos, compound);
        }
        if(!flushing) {
            if(async) world.getMinecraftServer().ticker().runAsync(this);
            else run();
        }
    }


    @Override
    public boolean writeNextIO() {
        Map<ChunkPos, NBTTagCompound> chunksToSave = this.chunksToSave;
        if (chunksToSave.isEmpty()) {
            return false;
        } else {
            Iterator<ChunkPos> iter = chunksToSave.keySet().iterator();
            if(!iter.hasNext())return false;
            ChunkPos chunkpos = iter.next();
            boolean success;

            try {
                this.chunksBeingSaved.add(chunkpos);
                NBTTagCompound nbttagcompound = chunksToSave.get(chunkpos);

                if (nbttagcompound != null) {
                    try {
                        synchronized (nbttagcompound) {
                            this.writeChunkData(chunkpos, nbttagcompound);
                        }
                    } catch (Exception exception) {
                        LOGGER.error("Failed to save chunk", exception);
                    }
                }
                success = true;
            } finally {
                this.chunksBeingSaved.remove(chunkpos);
                chunksToSave.remove(chunkpos);
            }
            return success;
        }
    }


    private void writeChunkData(ChunkPos pos, NBTTagCompound compound) throws IOException {
        DataOutputStream dataoutputstream = RegionFileCache.getChunkOutputStream(
                this.chunkSaveLocation, pos.x, pos.z
        );
        CompressedStreamTools.write(compound, dataoutputstream);
        dataoutputstream.close();
    }


    public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException {}


    @Override
    public void chunkTick() {}


    @Override
    public synchronized void flush() {
        try {
            this.flushing = true;
            Iterator<ChunkPos> iter = chunksToSave.keySet().iterator();
            while (iter.hasNext()) {
                ChunkPos chunkpos = iter.next();
                try {
                    this.chunksBeingSaved.add(chunkpos);
                    NBTTagCompound nbttagcompound = chunksToSave.get(chunkpos);

                    if (nbttagcompound != null) {
                        try {
                            synchronized (nbttagcompound) {
                                this.writeChunkData(chunkpos, nbttagcompound);
                            }
                        } catch (Exception exception) {
                            LOGGER.error("Failed to save chunk", exception);
                        }
                    }
                } finally {
                    this.chunksBeingSaved.remove(chunkpos);
                    chunksToSave.remove(chunkpos);
                }
            }
        } finally {
            this.flushing = false;
        }
    }


    @Override
    public void run() {
        try {
            if(flushing)return;
            this.flush();
        } catch (Exception e){
            LOGGER.error("Saving error: ", e);
        }
    }


    public static void registerFixes(DataFixer fixer) {
        fixer.registerWalker(FixTypes.CHUNK, new IDataWalker() {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn) {
                if (compound.hasKey(SStrings.CL_LEVEL, 10)) {
                    NBTTagCompound nbttagcompound = compound.getCompoundTag(SStrings.CL_LEVEL);

                    if (nbttagcompound.hasKey("Entities", 9)) {
                        NBTTagList nbttaglist = nbttagcompound.getTagList("Entities", 10);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                            nbttaglist.set(i, fixer.process(FixTypes.ENTITY, (NBTTagCompound)nbttaglist.get(i), versionIn));
                        }
                    }

                    if (nbttagcompound.hasKey("TileEntities", 9)) {
                        NBTTagList nbttaglist1 = nbttagcompound.getTagList("TileEntities", 10);

                        for (int j = 0; j < nbttaglist1.tagCount(); ++j) {
                            nbttaglist1.set(j, fixer.process(FixTypes.BLOCK_ENTITY, (NBTTagCompound)nbttaglist1.get(j), versionIn));
                        }
                    }
                }
                return compound;
            }
        });
    }


    private void writeChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound compound){
        writeChunkToNBT(worldIn.getPendingBlockUpdates(chunkIn, false), chunkIn, worldIn, compound);
    }


    private void writeChunkToNBT(@Nullable List<NextTickListEntry> pendings, Chunk chunkIn, World worldIn, NBTTagCompound compound) {
        compound.setIntegerU("xPos", chunkIn.x);
        compound.setIntegerU("zPos", chunkIn.z);
        compound.setLong("LastUpdate", worldIn.getTotalWorldTime());
        compound.setTagU("HeightMap", new NBTTagIntArrayUnsafe(chunkIn.getHeightMapRaw()));
        compound.setBoolean("TerrainPopulated", chunkIn.isTerrainPopulated());
        compound.setBoolean("LightPopulated", chunkIn.isLightPopulated());
        compound.setLong("InhabitedTime", chunkIn.getInhabitedTime());
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        NBTTagList nbttaglist = new NBTTagList();
        boolean flag = worldIn.provider.hasSkyLight();
        byte[] empty = null;

        for (ExtendedBlockStorage storage : aextendedblockstorage) {
            if (storage != Chunk.NULL_BLOCK_STORAGE) {
                NBTTagCompound tag = new NBTTagCompound(8, 1);
                tag.setByteU("Y", (byte)(storage.getYLocation() >> 4 & 255));
                byte[] abyte = new byte[4096];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = storage.getDataForNBT(abyte, nibblearray);
                tag.setByteArray("Blocks", abyte);
                tag.setByteArray("Data", nibblearray.getData());
                if (nibblearray1 != null)
                    tag.setByteArray("Add", nibblearray1.getData());

                tag.setTag("BlockLight", storage.toBlockLightNbt());

                if (flag)
                    tag.setTag("SkyLight", storage.toSkyLightNbt());
                else{
//                    if(empty==null)
//                        empty = new byte[storage.getBlockLight().getData().length];
                    tag.setTag("SkyLight", new NBTTagEmptyByteArray(2048, (byte) 0x00));
                }
                nbttaglist.appendTag(tag);
            }
        }
        compound.setTag("Sections", nbttaglist);
        compound.setByteArray("Biomes", chunkIn.getBiomeArray());
        chunkIn.setHasEntities(false);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (ClassInheritanceMultiMap<Entity> list : chunkIn.getEntityLists()) {
            if(list == null) continue;

            NBTTagCompound tagCompound;

            for (Entity entity : list) {
                tagCompound = new NBTTagCompound();
                try {
                    if (entity.writeToNBTOptional(tagCompound)) {
                        chunkIn.setHasEntities(true);
                        nbttaglist1.appendTag(tagCompound);
                    }
                } catch (Exception e) {
                    net.minecraftforge.fml.common.FMLLog.log.error("An Entity type {} has thrown an exception trying to write state. It will not persist. Report this to the mod author",
                            entity.getClass().getName(), e);
                }
            }
        }
        compound.setTag("Entities", nbttaglist1);
        NBTTagList nbttaglist2 = new NBTTagList();

        for (TileEntity tileentity : chunkIn.getTileEntityMap().values()) {
            try {
                NBTTagCompound nbttagcompound3 = tileentity.writeToNBT(new NBTTagCompound());
                nbttaglist2.appendTag(nbttagcompound3);
            } catch (Exception e) {
                net.minecraftforge.fml.common.FMLLog.log.error("A TileEntity type {} has throw an exception trying to write state. It will not persist. Report this to the mod author",
                        tileentity.getClass().getName(), e);
            }
        }

        compound.setTag("TileEntities", nbttaglist2);

        if (pendings != null) {
            long j = worldIn.getTotalWorldTime();
            NBTTagList updates = new NBTTagList(pendings.size());
            for (NextTickListEntry entry : pendings) {
                NBTTagCompound update = new NBTTagCompound(8, 1);
                ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(entry.getBlock());
                update.setString("i", resourcelocation == null ? "" : resourcelocation.toString());
                update.setInteger("x", entry.position.getX());
                update.setIntegerU("y", entry.position.getY());
                update.setInteger("z", entry.position.getZ());
                update.setIntegerU("t", (int)(entry.scheduledTime - j));
                update.setIntegerU("p", entry.priority);
                updates.appendTag(update);
            }
            compound.setTag(SStrings.CL_TILE_TICKS, updates);
        }

        if (chunkIn.getCapabilities() != null) {
            try {
                compound.setTag("ForgeCaps", chunkIn.getCapabilities().serializeNBT());
            } catch (Exception exception) {
                net.minecraftforge.fml.common.FMLLog.log.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", exception);
            }
        }
        compound.setBoolean("LightPopulated", chunkIn.isLightInitialized());
    }



    private Chunk readChunkFromNBT(World worldIn, NBTTagCompound compound) {
        int i = compound.getInteger("xPos");
        int j = compound.getInteger("zPos");
        Chunk chunk = new Chunk(worldIn, i, j);

        NBTBase heightMap = compound.getTag("HeightMap");
        if(heightMap instanceof NBTTagIntArrayUnsafe){
            chunk.setHeightMap(((NBTTagIntArrayUnsafe)heightMap).array());
        } else {
            chunk.setHeightMap(((NBTTagIntArray)heightMap).getIntArray());
            compound.setTagU("HeightMap", new NBTTagIntArrayUnsafe(chunk.getHeightMapRaw()));
        }

        chunk.setTerrainPopulated(compound.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(compound.getBoolean("LightPopulated"));
        chunk.setInhabitedTime(compound.getLong("InhabitedTime"));
        NBTTagList nbttaglist = compound.getTagList("Sections", 10);
        ExtendedBlockStorage[] storages = new ExtendedBlockStorage[16];
        boolean flag = worldIn.provider.hasSkyLight();

        for (int l = 0; l < nbttaglist.tagCount(); ++l) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(l);
            int i1 = nbttagcompound.getByte("Y");
            ExtendedBlockStorage storage = new ExtendedBlockStorage(i1 << 4, flag);
            byte[] abyte = nbttagcompound.getByteArray("Blocks");
            NibbleArray nibblearray = new NibbleArray(nbttagcompound.getByteArray("Data"));
            NibbleArray nibblearray1 = nbttagcompound.hasKey("Add", 7) ? new NibbleArray(nbttagcompound.getByteArray("Add")) : null;
            storage.setDataFromNBT(abyte, nibblearray, nibblearray1);
            storage.setBlockLight(nbttagcompound.getByteArray("BlockLight"));

            if (flag)
                storage.setSkyLight(nbttagcompound.getByteArray("SkyLight"));

            storage.recalculateRefCounts();
            storages[i1] = storage;
        }

        chunk.setStorageArrays(storages);

        if (compound.hasKey("Biomes", 7))
            chunk.setBiomeArray(compound.getByteArray("Biomes"));

        if (chunk.getCapabilities() != null && compound.hasKey("ForgeCaps")) {
            chunk.getCapabilities().deserializeNBT(compound.getCompoundTag("ForgeCaps"));
        }
        LightingHooks.readNeighborLightChecksFromNBT(chunk, compound);
        chunk.setLightInitialized(compound.getBoolean("LightPopulated"));

        return chunk;
    }


    public void loadEntities(World worldIn, NBTTagCompound compound, Chunk chunk) {
        NBTTagList list = compound.getTagList("Entities", 10);

        for (int j1 = 0; j1 < list.tagCount(); ++j1) {
            NBTTagCompound tag = list.getCompoundTagAt(j1);
            readChunkEntity(tag, worldIn, chunk);
            chunk.setHasEntities(true);
        }

        NBTTagList nbttaglist2 = compound.getTagList("TileEntities", 10);

        for (int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
            NBTTagCompound nbttagcompound2 = nbttaglist2.getCompoundTagAt(k1);
            TileEntity tileentity = TileEntity.create(worldIn, nbttagcompound2);

            if (tileentity != null) {
                chunk.addTileEntity(tileentity);
            }
        }


        if (compound.hasKey(SStrings.CL_TILE_TICKS, 9)) {
            NBTTagList updates = compound.getTagList(SStrings.CL_TILE_TICKS, 10);

            for (int i = 0, count = updates.tagCount(); i < count; ++i) {
                NBTTagCompound update = updates.getCompoundTagAt(i);
                Block block;

                if (update.hasKey("i", 8)) {
                    block = Block.getBlockFromName(update.getString("i"));
                } else {
                    block = Block.getBlockById(update.getInteger("i"));
                }

                worldIn.scheduleBlockUpdate(new BlockPos(update.getInteger("x"), update.getInteger("y"), update.getInteger("z")), block, update.getInteger("t"), update.getInteger("p"));
            }
        }
    }


    @Nullable
    public static Entity readChunkEntity(NBTTagCompound compound, World worldIn, Chunk chunkIn) {
        Entity entity = createEntityFromNBT(compound, worldIn);
        if (entity == null) {
            return null;
        } else {
            chunkIn.addEntity(entity);
            if (compound.hasKey(SStrings.CL_PASSENGERS, 9)) {
                NBTTagList nbttaglist = compound.getTagList(SStrings.CL_PASSENGERS, 10);

                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    Entity entity1 = readChunkEntity(nbttaglist.getCompoundTagAt(i), worldIn, chunkIn);

                    if (entity1 != null) {
                        entity1.startRiding(entity, true);
                    }
                }
            }
            return entity;
        }
    }


    @Nullable
    public static Entity readWorldEntityPos(NBTTagCompound compound, World worldIn, double x, double y, double z, boolean attemptSpawn) {
        Entity entity = createEntityFromNBT(compound, worldIn);

        if (entity == null) {
            return null;
        } else {
            entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

            if (attemptSpawn && !worldIn.spawnEntity(entity)) {
                return null;
            } else {
                if (compound.hasKey(SStrings.CL_PASSENGERS, 9)) {
                    NBTTagList nbttaglist = compound.getTagList(SStrings.CL_PASSENGERS, 10);

                    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                        Entity entity1 = readWorldEntityPos(nbttaglist.getCompoundTagAt(i), worldIn, x, y, z, attemptSpawn);

                        if (entity1 != null) {
                            entity1.startRiding(entity, true);
                        }
                    }
                }
                return entity;
            }
        }
    }


    @Nullable
    protected static Entity createEntityFromNBT(NBTTagCompound compound, World worldIn) {
        try {
            return EntityList.createEntityFromNBT(compound, worldIn);
        } catch (RuntimeException var3) {
            return null;
        }
    }


    public static void spawnEntity(Entity entityIn, World worldIn) {
        if (worldIn.spawnEntity(entityIn) && entityIn.isBeingRidden()) {
            for (Entity entity : entityIn.getPassengers()) {
                spawnEntity(entity, worldIn);
            }
        }
    }


    @Nullable
    public static Entity readWorldEntity(NBTTagCompound compound, World worldIn, boolean p_186051_2_) {
        Entity entity = createEntityFromNBT(compound, worldIn);

        if (entity == null) {
            return null;
        } else if (p_186051_2_ && !worldIn.spawnEntity(entity)) {
            return null;
        } else {
            if (compound.hasKey(SStrings.CL_PASSENGERS, 9)) {
                NBTTagList nbttaglist = compound.getTagList(SStrings.CL_PASSENGERS, 10);

                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    Entity entity1 = readWorldEntity(nbttaglist.getCompoundTagAt(i), worldIn, p_186051_2_);

                    if (entity1 != null) {
                        entity1.startRiding(entity, true);
                    }
                }
            }
            return entity;
        }
    }


    public int getPendingSaveCount() {
        return this.chunksToSave.size();
    }
}
