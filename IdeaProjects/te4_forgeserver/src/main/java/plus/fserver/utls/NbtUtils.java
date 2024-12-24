package plus.fserver.utls;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import plus.fserver.server.SStrings;
import plus.fserver.world.light.LightingHooks;
import plus.tson.utl.Te4HashMap;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


public class NbtUtils {
    private static final NBTTagByte[]  TAG_BYTES;
    private static final NBTTagByte[]  TAG_BYTES_N;
    private static final NBTPrimitive[]TAG_INTS;
    public static final NBTTagByte     BYTE_TRUE;
    public static final NBTTagByte     BYTE_FALSE;
    public static final NBTTagInt      DATA_VERSION = new NBTTagInt(1343);
    public static final NBTTagInt      ITEM_DEFAULT_LIFESPAN = new NBTTagInt(6000);

    public static NBTTagByte create(byte b){
        if(b < 0)return TAG_BYTES_N[-b];
        return TAG_BYTES[b];
    }

    public static NBTPrimitive create(int i){
        if(i > 319 || i < 0)return new NBTTagInt(i);
        return TAG_INTS[i];
    }

    public static NBTPrimitive create(short s){
        if(s > 319 || s < 0)return new NBTTagShort(s);
        return TAG_INTS[s];
    }


    public static NBTTagByte create(boolean b){
        if(b)return BYTE_TRUE;
        return BYTE_FALSE;
    }


    static {
        NBTTagByte[] bytes = TAG_BYTES = new NBTTagByte[127];
        for (int i = 0; i < bytes.length; i++){
            bytes[i] = new NBTTagByte((byte) i);
        }
        BYTE_FALSE = bytes[0];
        BYTE_TRUE  = bytes[1];

        NBTTagByte[] bytesN = TAG_BYTES_N = new NBTTagByte[128];
        for (int i = 0; i < 128; i++){
            bytesN[i] = new NBTTagByte((byte) -i);
        }

        NBTPrimitive[] ints = TAG_INTS = new NBTPrimitive[320];
        for (int i = 0; i < ints.length; i++){
            if(i >= 127) ints[i] = new NBTTagShort((short) i);
            else ints[i] = bytes[i];
        }
    }


    public static Chunk readChunkFromNBT(World worldIn, NBTTagCompound nbt) {
        return readChunkFromNBT(worldIn, nbt, false);
    }


    public static Chunk readChunkFromNBT(World worldIn, NBTTagCompound nbt, boolean useBref) {
        int xPos = nbt.getInteger("xPos");
        int zPos = nbt.getInteger("zPos");
        Chunk chunk = new Chunk(worldIn, xPos, zPos);

        readCommonChunkData(chunk, nbt, useBref);
        readChunkBlockStorage(chunk, nbt);
        readChunkCapabilities(chunk, nbt);

        return chunk;
    }


    public static void readChunkCapabilities(Chunk chunk, NBTTagCompound nbt){
        if (chunk.getCapabilities() != null && nbt.hasKey("ForgeCaps")) {
            chunk.getCapabilities().deserializeNBT(nbt.getCompoundTag("ForgeCaps"));
        }
    }


    public static void readCommonChunkData(Chunk chunk, NBTTagCompound nbt) {
        readCommonChunkData(chunk, nbt, false);
    }


    public static void readCommonChunkData(Chunk chunk, NBTTagCompound nbt, boolean useBref) {
        NBTBase heightMap = nbt.getTag("HeightMap");
        if(heightMap instanceof NBTTagIntArrayUnsafe){
            chunk.setHeightMap(((NBTTagIntArrayUnsafe)heightMap).array());
        } else {
            chunk.setHeightMap(((NBTTagIntArray)heightMap).getIntArray());
            nbt.setTagU("HeightMap", new NBTTagIntArrayUnsafe(chunk.getHeightMapRaw()));
        }

        chunk.setTerrainPopulated(nbt.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(nbt.getBoolean("LightPopulated"));
        chunk.setInhabitedTime(nbt.getLong("InhabitedTime"));

        if (nbt.hasKey("Biomes", 7)) {
            if(useBref) {
                chunk.setBiomeArray(nbt.getByteArray("Biomes"));
            } else {
                chunk.setBiomeArrayRef(nbt.getByteArray("Biomes"));
            }
        }

        LightingHooks.readNeighborLightChecksFromNBT(chunk, nbt);
        chunk.setLightInitialized(nbt.getBoolean("LightPopulated"));
    }


    public static void readChunkBlockStorage(Chunk chunk, NBTTagCompound compound) {
        NBTTagList nbttaglist = compound.getTagList("Sections", 10);
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
        boolean flag = chunk.getWorld().provider.hasSkyLight();

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
    }


    public static void loadEntities(World worldIn, NBTTagCompound compound, Chunk chunk) {
        NBTTagList list = compound.getTagList("Entities", 10);

        for (int j1 = 0; j1 < list.tagCount(); ++j1) {
            NBTTagCompound tag = list.getCompoundTagAt(j1);
            AnvilChunkLoader.readChunkEntity(tag, worldIn, chunk);
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


    public static void writeChunkToNBT(@Nullable List<NextTickListEntry> pendings, Chunk chunkIn, World worldIn, NBTTagCompound compound, boolean saveEntities) {
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
                    tag.setTag("SkyLight", new NBTTagEmptyByteArray(2048, (byte) 0x00));
                }
                nbttaglist.appendTag(tag);
            }
        }
        compound.setTag("Sections", nbttaglist);
        compound.setByteArray("Biomes", chunkIn.getBiomeArray());
        chunkIn.setHasEntities(false);

        if(saveEntities) {
            NBTTagList nbttaglist1 = new NBTTagList();
            for (ClassInheritanceMultiMap<Entity> list : chunkIn.getEntityLists()) {
                if (list == null) continue;
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
        }

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


    public static void trimNbt(NBTTagCompound nbt){
        Te4HashMap<String, String>   keyCache  = new Te4HashMap<>();
        Te4HashMap<NBTBase,NBTBase> valueCache = new Te4HashMap<>();

        NBTTagCompound.tryTrim(nbt, keyCache, valueCache);
    }
}