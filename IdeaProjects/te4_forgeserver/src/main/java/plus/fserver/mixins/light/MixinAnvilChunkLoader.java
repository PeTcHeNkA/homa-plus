package plus.fserver.mixins.light;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.nbt.*;
import plus.fserver.world.light.LightingHooks;
import plus.fserver.world.light.api.IChunkLightingData;
import plus.fserver.world.light.api.ILightingEngineProvider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
    @Shadow protected abstract void addChunkToPending(ChunkPos pos, NBTTagCompound compound);
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public File chunkSaveLocation;

    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    public void saveChunk(World worldIn, Chunk chunkIn) throws MinecraftException {
        ((ILightingEngineProvider)worldIn).getLightingEngine().processLightUpdates();
        worldIn.checkSessionLock();
        try {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound compound1 = new NBTTagCompound();
            compound.setTag("Level", compound1);
            compound.setInteger("DataVersion", 1343);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().getDataFixer().writeVersionData(compound);
            this.writeChunkToNBT(chunkIn, worldIn, compound1);
            ForgeChunkManager.storeChunkNBT(chunkIn, compound1);
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkDataEvent.Save(chunkIn, compound));
            this.addChunkToPending(chunkIn.getPos(), compound);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }
    }


    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    private Chunk readChunkFromNBT(World worldIn, NBTTagCompound compound) {
        int i = compound.getInteger("xPos");
        int j = compound.getInteger("zPos");
        Chunk chunk = new Chunk(worldIn, i, j);
        chunk.setHeightMap(compound.getIntArray("HeightMap"));
        chunk.setTerrainPopulated(compound.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(compound.getBoolean("LightPopulated"));
        chunk.setInhabitedTime(compound.getLong("InhabitedTime"));
        NBTTagList nbttaglist = compound.getTagList("Sections", 10);
        ExtendedBlockStorage[] storages = new ExtendedBlockStorage[16];
        boolean flag = worldIn.provider.hasSkyLight();

        for (int l = 0; l < nbttaglist.tagCount(); ++l) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(l);
            int i1 = nbttagcompound.getByte("Y");
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(i1 << 4, flag);
            byte[] abyte = nbttagcompound.getByteArray("Blocks");
            NibbleArray nibblearray = new NibbleArray(nbttagcompound.getByteArray("Data"));
            NibbleArray nibblearray1 = nbttagcompound.hasKey("Add", 7) ? new NibbleArray(nbttagcompound.getByteArray("Add")) : null;
            extendedblockstorage.getData().setDataFromNBT(abyte, nibblearray, nibblearray1);
            extendedblockstorage.setBlockLight(new NibbleArray(nbttagcompound.getByteArray("BlockLight")));

            if (flag)
                extendedblockstorage.setSkyLight(new NibbleArray(nbttagcompound.getByteArray("SkyLight")));

            extendedblockstorage.recalculateRefCounts();
            storages[i1] = extendedblockstorage;
        }

        chunk.setStorageArrays(storages);

        if (compound.hasKey("Biomes", 7))
            chunk.setBiomeArray(compound.getByteArray("Biomes"));

        if (chunk.getCapabilities() != null && compound.hasKey("ForgeCaps")) {
            chunk.getCapabilities().deserializeNBT(compound.getCompoundTag("ForgeCaps"));
        }
        LightingHooks.readNeighborLightChecksFromNBT(chunk, compound);
        ((IChunkLightingData)chunk).setLightInitialized(compound.getBoolean("LightPopulated"));
        return chunk;
    }


    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    private void writeChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound compound) {
        compound.setInteger("xPos", chunkIn.x);
        compound.setInteger("zPos", chunkIn.z);
        compound.setLong("LastUpdate", worldIn.getTotalWorldTime());
        compound.setIntArray("HeightMap", chunkIn.getHeightMap());
        compound.setBoolean("TerrainPopulated", chunkIn.isTerrainPopulated());
        compound.setBoolean("LightPopulated", chunkIn.isLightPopulated());
        compound.setLong("InhabitedTime", chunkIn.getInhabitedTime());
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        NBTTagList nbttaglist = new NBTTagList();
        boolean flag = worldIn.provider.hasSkyLight();
        byte[] empty = null;

        for (ExtendedBlockStorage extendedblockstorage : aextendedblockstorage) {
            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Y", (byte)(extendedblockstorage.getYLocation() >> 4 & 255));
                byte[] abyte = new byte[4096];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = extendedblockstorage.getData().getDataForNBT(abyte, nibblearray);
                nbttagcompound.setByteArray("Blocks", abyte);
                nbttagcompound.setByteArray("Data", nibblearray.getData());
                if (nibblearray1 != null)
                    nbttagcompound.setByteArray("Add", nibblearray1.getData());

                nbttagcompound.setByteArray("BlockLight", extendedblockstorage.getBlockLight().getData());

                if (flag)
                    nbttagcompound.setByteArray("SkyLight", extendedblockstorage.getSkyLight().getData());
                else{
                    if(empty==null)
                        empty = new byte[extendedblockstorage.getBlockLight().getData().length];
                    nbttagcompound.setByteArray("SkyLight", empty);
                }
                nbttaglist.appendTag(nbttagcompound);
            }
        }
        compound.setTag("Sections", nbttaglist);
        compound.setByteArray("Biomes", chunkIn.getBiomeArray());
        chunkIn.setHasEntities(false);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int i = 0; i < chunkIn.getEntityLists().length; ++i) {
            NBTTagCompound tagCompound;
            for (Entity entity : chunkIn.getEntityLists()[i]) {
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
        List<NextTickListEntry> list = worldIn.getPendingBlockUpdates(chunkIn, false);

        if (list != null) {
            long j = worldIn.getTotalWorldTime();
            NBTTagList nbttaglist3 = new NBTTagList();
            for (NextTickListEntry nextticklistentry : list) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(nextticklistentry.getBlock());
                nbttagcompound1.setString("i", resourcelocation == null ? "" : resourcelocation.toString());
                nbttagcompound1.setInteger("x", nextticklistentry.position.getX());
                nbttagcompound1.setInteger("y", nextticklistentry.position.getY());
                nbttagcompound1.setInteger("z", nextticklistentry.position.getZ());
                nbttagcompound1.setInteger("t", (int)(nextticklistentry.scheduledTime - j));
                nbttagcompound1.setInteger("p", nextticklistentry.priority);
                nbttaglist3.appendTag(nbttagcompound1);
            }
            compound.setTag("TileTicks", nbttaglist3);
        }
        if (chunkIn.getCapabilities() != null) {
            try {
                compound.setTag("ForgeCaps", chunkIn.getCapabilities().serializeNBT());
            } catch (Exception exception) {
                net.minecraftforge.fml.common.FMLLog.log.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", exception);
            }
        }
        compound.setBoolean("LightPopulated", ((IChunkLightingData)chunkIn).isLightInitialized());
    }


    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Overwrite
    private void writeChunkData(ChunkPos pos, NBTTagCompound compound) throws IOException {
        DataOutputStream dataoutputstream = RegionFileCache.getChunkOutputStream(
                this.chunkSaveLocation, pos.x, pos.z
        );
        CompressedStreamTools.write(compound, dataoutputstream);
        dataoutputstream.close();
    }
}