package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.asyutl.buf.FastBuffer;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.api.Keep;
import plus.fserver.ElcaCV;
import plus.fserver.utls.collections.EntityList;
import plus.fserver.utls.math.MathUtls;
import plus.fserver.world.light.LightingEngine;
import plus.fserver.world.light.LightingHooks;
import plus.fserver.world.light.WorldChunkSlice;
import plus.fserver.world.light.api.IChunkLightingData;
import plus.fserver.world.light.api.ILightingEngine;
import plus.fserver.world.light.api.ILightingEngineProvider;
import plus.tson.utl.Te4HashMap;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class Chunk implements net.minecraftforge.common.capabilities.ICapabilityProvider, ILightingEngineProvider{
    @Keep
    public static final int STORAGES_COUNT = 16;
    @Keep
    public static final int MAX_HEIGHT     = 255;
    @Keep
    public static final EnumFacing[] HORIZONTAL = EnumFacing.Plane.HORIZONTAL.facings();
    @Keep
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
    @Keep
    private final ExtendedBlockStorage[] storageArrays;
    @Keep
    private final byte[] blockBiomeArray;
    @Keep
    private final short[] precipitationHeightMap;
    @Keep
    private final short[] updateSkylights;
    @Keep
    private boolean loaded;
    @Keep
    private final World world;
    @Keep
    private final byte[] heightMap;
    public final int x;
    public final int z;
    @Keep
    private boolean isGapLightingUpdated;
    @Keep
    private final Map<BlockPos, TileEntity> tileEntities;
    @Keep
    private final ClassInheritanceMultiMap<Entity>[] entityLists;
    @Keep
    private boolean isTerrainPopulated;
    @Keep
    private boolean isLightPopulated;
    @Keep
    private boolean ticked;
    @Keep
    private boolean dirty;
    @Keep
    private boolean hasEntities;
    @Keep
    private long lastSaveTime;
    @Keep
    private int heightMapMinimum;
    @Keep
    private long inhabitedTime;
    @Keep
    private int queuedLightChecks;
    @Keep
    private final FastBuffer<BlockPos> tileEntityPosQueue;
    @Keep
    public byte state = 0;
    //00   (0) = default
    //01   (1) = tracked
    //10   (2) = requested
    //100  (4) = should tick
    //1000 (8) = now sawing

    public boolean isNotUnloadQueued(){
        return (state & 1) == 1;
    }
    public boolean isRequested(){
        return (state & 2) == 2;
    }
    public void setRequested(boolean b){
        if(b) state |= 2;
        else state &= ~2;
    }
    public void setRequested(){
        state |= 2;
    }
    public void setUnloadQueued(boolean b){
        if(b) state &= ~1;
        else state |= 1;
    }
    public void setUnloadQueued(){
        state &= ~1;
    }
    public boolean maybeUnload(){
        return (state & 7) == 0;
    }
    public void setPersistent(boolean b){
        if(b) state &= ~4;
        else state |= 4;
    }
    public boolean isPersistent(){
        return (state & 4) == 4;
    }
    public boolean isNowSaving(){
        return (state & 8) == 8;
    }
    public void setSaveStatus(boolean b){
        if(b) state &= ~8;
        else state |= 8;
    }

    public Chunk(World worldIn, int x, int z) {
        this.storageArrays = new ExtendedBlockStorage[STORAGES_COUNT];
        byte[] biomeArr = this.blockBiomeArray = new byte[256];
        short[] phMap = this.precipitationHeightMap = new short[256];
        this.updateSkylights = new short[16];

        this.tileEntities = new Te4HashMap<>(8);
        this.queuedLightChecks = 4096;
        this.tileEntityPosQueue = new FastBuffer<>();
        this.world = worldIn;
        this.x = x;
        this.z = z;
        this.heightMap = new byte[256];

        Arrays.fill(phMap, (short) -999);
        Arrays.fill(biomeArr, (byte) - 1);

        ClassInheritanceMultiMap[] lists = this.entityLists = new ClassInheritanceMultiMap[16];

        for (int i = 0; i < lists.length; ++i) {
            lists[i] = new ClassInheritanceMultiMap<>(Entity.class);
        }
        //capabilities = ForgeEventFactory.gatherCapabilities(this);
    }


    @Override
    public ILightingEngine getLightingEngine() {
        return world.lightingEngine;
    }


    public Chunk(World worldIn, ChunkPrimer primer, int x, int z) {
        this(worldIn, x, z);
        boolean flag = worldIn.provider.hasSkyLight(), flag2 = false;
        IBlockState state = null;
        ExtendedBlockStorage[] storageArrays = this.storageArrays;
        for (int yPos = 0, xPos, sae, zPOs, ids, pids=-10; yPos < 256; ++yPos) {
            ExtendedBlockStorage arr = storageArrays[sae = yPos >> 4];
            for (xPos = 0; xPos < 16; ++xPos) {
                for (zPOs = 0; zPOs < 16; ++zPOs) {
                    if ((ids = primer.getBlockStateId(xPos, yPos, zPOs)) != pids)
                        flag2 = (state = BlockIDIndexes.BLOCK_INDEX.get(pids = ids)) != null
                                && state.getMaterial() != Material.AIR;

                    if (flag2) {
                        if (arr == null) {
                            storageArrays[sae] = arr = new ExtendedBlockStorage(sae << 4, flag);
                        }
                        arr.set(xPos, yPos & 15, zPOs, state);
                    }
                }
            }
        }
    }


    public boolean isAtLocation(int x, int z) {
        return x == this.x && z == this.z;
    }


    public int getHeight(BlockPos pos) {
        return this.getHeightValue(pos.getX() & 15, pos.getZ() & 15);
    }


    public int getHeightValue(int x, int z) {
        return this.heightMap[z << 4 | x] & 255;
    }


    @Nullable
    private ExtendedBlockStorage getLastExtendedBlockStorage() {
        ExtendedBlockStorage[] storageArrays;
        for (int i = (storageArrays = this.storageArrays).length - 1; i >= 0; --i)
            if (storageArrays[i] != null)
                return storageArrays[i];
        return null;
    }


    public int getTopFilledSegment() {
        ExtendedBlockStorage extendedblockstorage = this.getLastExtendedBlockStorage();
        return extendedblockstorage == null ? 0 : extendedblockstorage.getYLocation();
    }


    public ExtendedBlockStorage[] getBlockStorageArray() {
        return this.storageArrays;
    }


    @SideOnly(Side.CLIENT)
    protected void generateHeightMap() {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        IBlockState iblockstate;
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;
                for (int l = i + 16; l > 0; --l) {
                    iblockstate = this.getBlockState(j, l - 1, k);
                    if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                        this.heightMap[k << 4 | j] = (byte) l;
                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }
                        break;
                    }
                }
            }
        }
        this.dirty = true;
    }


    public void generateSkylightMap() {
        int i = this.getTopFilledSegment();
        int min = this.heightMapMinimum = Integer.MAX_VALUE;

        short[] precipitationHeightMap = this.precipitationHeightMap;
        byte[] heightMap = this.heightMap;
        World world = this.world;
        boolean hasSkyLight = world.provider.hasSkyLight();
        ExtendedBlockStorage[] storageArrays = this.storageArrays;

        for (int xPos = 0; xPos < 16; ++xPos) {
            for (int zPos = 0; zPos < 16; ++zPos) {
                precipitationHeightMap[xPos + (zPos << 4)] = -999;
                for (int l = i + 16; l > 0; --l) {
                    if (this.getBlockLightOpacity(xPos, l - 1, zPos) != 0) {
                        heightMap[zPos << 4 | xPos] = (byte) l;
                        if (l < min) {
                            this.heightMapMinimum = min = l;
                        }
                        break;
                    }
                }

                if (hasSkyLight) {
                    int k1 = 15, i1 = i + 16 - 1;
                    while (true) {
                        int j1 = this.getBlockLightOpacity(xPos, i1, zPos);

                        if (j1 == 0 && k1 != 15) {
                            j1 = 1;
                        }
                        k1 -= j1;
                        if (k1 > 0) {
                            ExtendedBlockStorage storage = storageArrays[i1 >> 4];

                            if (storage != null) {
                                storage.setSkyLight(xPos, i1 & 15, zPos, k1);
                                world.notifyLightSet(new BlockPos((this.x << 4) + xPos, i1, (this.z << 4) + zPos));
                            }
                        }
                        --i1;
                        if (i1 <= 0 || k1 <= 0) break;
                    }
                }
            }
        }
        this.dirty = true;
    }


    private void propagateSkylightOcclusion(int x, int z) {
        updateSkylights[x] |= 1<<z;
        this.isGapLightingUpdated = true;
    }


    private void recheckGapsServer() {
        WorldChunkSlice slice = world.poledSlice;
        if(slice == null){
            slice = world.poledSlice = new WorldChunkSlice();
        }
        slice.tryLoad3x3(world.chunkProviderServer, x, z);
        if (slice.isLoaded3x3()) {
            slice.tryLoad5x5No3x3(world.chunkProviderServer, x, z);
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    this.recheckGapsForColumn(slice, x, z);
                }
            }
            this.isGapLightingUpdated = false;
        }
    }


    private void recheckGaps(final boolean onlyOne) {
        if (this.world.isAreaLoaded(new BlockPos(this.x * 16 + 8, 0, this.z * 16 + 8), 16)) {
            final WorldChunkSlice slice = world.slice(this.x, this.z);
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (this.recheckGapsForColumn(slice, x, z) && onlyOne) {
                        return;
                    }
                }
            }
            this.isGapLightingUpdated = false;
        }
    }


    private boolean recheckGapsForColumn(final WorldChunkSlice slice, final int x, final int z) {
        final int mask = 1<<z;
        if ((updateSkylights[x] & mask) == mask) {
            updateSkylights[x] &= ~mask;
            final int x2 = (this.x << 4) + x;
            final int z2 = (this.z << 4) + z;
            this.recheckGapsSkylightNeighborHeight(
                    slice, x2, z2,
                    getHeightValue(x, z),
                    recheckGapsGetLowestHeight(slice, x2, z2)
            );
            return true;
        }
        return false;
    }


    private int recheckGapsGetLowestHeight(final WorldChunkSlice slice, final int x, final int z) {
        int max = Integer.MAX_VALUE;
        for (final EnumFacing facing : HORIZONTAL) {
            final Chunk chunk = slice.getChunkFromWorldCoords(
                    x + facing.getXOffset(),
                    z + facing.getZOffset()
            );
            if (chunk != null) {
                max = Math.min(max, chunk.getLowestHeight());
            }
        }
        return max;
    }


    private void recheckGapsSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int height, final int max) {
        this.checkSkylightNeighborHeight(slice, x, z, max);
        this.checkSkylightNeighborHeight(slice, x+1, z, max);
        this.checkSkylightNeighborHeight(slice, x-1, z, max);
        this.checkSkylightNeighborHeight(slice, x, z+1, max);
        this.checkSkylightNeighborHeight(slice, x, z-1, max);
    }


    private void checkSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int maxValue) {
        final Chunk chunk;
        if ((chunk = slice.getChunkFromWorldCoords(x, z)) == null) return;
        final int i = chunk.getHeightValue(x & 0xF, z & 0xF);
        if (i > maxValue)
            this.updateSkylightNeighborHeight(slice, x, z, maxValue, i + 1);
        else if (i < maxValue)
            this.updateSkylightNeighborHeight(slice, x, z, i, maxValue + 1);
    }


    private void updateSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int startY, final int endY) {
        if (endY > startY) {
            if (!slice.isLoaded(x, z, 16)) return;
            final LightingEngine eng = world.lightingEngine;
            for (int i = startY; i < endY; ++i) {
                eng.scheduleLightUpdate(EnumSkyBlock.SKY, x, i, z);
            }
            this.dirty = true;
        }
    }


//    private void recheckGaps(boolean onlyOne) {
//        int x = (this.x << 4) + 8, z = (this.z << 4) + 8;
//        if (this.world.isAreaLoaded(x-16, 16, z-16, x+16, 16, z+16, true)) {
//            int mask;
//            for (x = 0; x < 16; ++x) {
//                for (z = 0; z < 16; ++z) {
//                    mask = 1<<z;
//                    if ((updateSkylights[x] & mask) == mask) {
//                        updateSkylights[x] &= ~mask;
//                        //this.updateSkylightColumns[x + z * 16] = false;
//                        int l = this.x * 16 + x;
//                        int i1 = this.z * 16 + z;
//                        int j1 = Integer.MAX_VALUE;
//
//                        for (EnumFacing enumfacing : HORIZONTAL) {
//                            j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumfacing.getXOffset(), i1 + enumfacing.getZOffset()));
//                        }
//                        this.checkSkylightNeighborHeight(l, i1, j1);
//
//                        for (EnumFacing facing : HORIZONTAL) {
//                            this.checkSkylightNeighborHeight(l + facing.getXOffset(), i1 + facing.getZOffset(), getHeightValue(x, z));
//                        }
//                        if (onlyOne) return;
//                    }
//                }
//            }
//            this.isGapLightingUpdated = false;
//        }
//    }


    private void checkSkylightNeighborHeight(int x, int z, int maxValue) {
        int i = this.world.getHeight(new BlockPos(x, 0, z)).getY();
        if (i > maxValue) {
            this.updateSkylightNeighborHeight(x, z, maxValue, i + 1);
        } else if (i < maxValue) {
            this.updateSkylightNeighborHeight(x, z, i, maxValue + 1);
        }
    }


    private void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {
        if (endY > startY && this.world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
            for (int i = startY; i < endY; ++i) {
                this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, i, z));
            }
            this.dirty = true;
        }
    }


    public void relight(BlockPos pos){
        relightBlock(pos.getX()&15, pos.getY(), pos.getZ()&15);
    }


    //todo overwrited
    private void relightBlock(final int x, final int y, final int z) {
        int i = this.heightMap[z << 4 | x] & 255;
        int j = i;
        if (y > i) {
            j = y;
        }

        while (j > 0 && this.getBlockLightOpacity(x, j - 1, z) == 0) {
            --j;
        }

        if (j != i) {
            this.world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = (byte) j;
            int k = this.x * 16 + x;
            int l = this.z * 16 + z;

            if (this.world.provider.hasSkyLight()) {
                if (j < i) {
                    for (int j1 = j; j1 < i; ++j1) {
                        ExtendedBlockStorage storage = this.storageArrays[j1 >> 4];

                        if (storage != NULL_BLOCK_STORAGE) {
                            storage.setSkyLight(x, j1 & 15, z, 15);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                        }
                    }
                } else {
                    for (int i1 = i; i1 < j; ++i1) {
                        ExtendedBlockStorage storage = this.storageArrays[i1 >> 4];

                        if (storage != null) {
                            storage.setSkyLight(x, i1 & 15, z, 0);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
                        }
                    }
                }

                int k1 = 15;

                while (j > 0 && k1 > 0) {
                    --j;
                    int i2 = this.getBlockLightOpacity(x, j, z);
                    if (i2 == 0) {
                        i2 = 1;
                    }
                    k1 -= i2;
                    if (k1 < 0) {
                        k1 = 0;
                    }
                    ExtendedBlockStorage storage = this.storageArrays[j >> 4];
                    if (storage != null)
                        storage.setSkyLight(x, j & 15, z, k1);
                }
            }

            int l1 = this.heightMap[z << 4 | x] & 255;
            int j2 = i;
            int k2 = l1;

            if (l1 < i) {
                j2 = l1;
                k2 = i;
            }

            if (l1 < this.heightMapMinimum) {
                this.heightMapMinimum = l1;
            }
            if (this.world.provider.hasSkyLight()) {
                for (EnumFacing enumfacing : HORIZONTAL) {
                    this.updateSkylightNeighborHeight(k + enumfacing.getXOffset(), l + enumfacing.getZOffset(), j2, k2);
                }
                this.updateSkylightNeighborHeight(k, l, j2, k2);
            }
            this.dirty = true;
        }
    }


    public int getBlockLightOpacity(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ()).getLightOpacity(this.world, pos);
    }


    private int getBlockLightOpacity(int x, int y, int z) {
        IBlockState state = this.getBlockState(x, y, z); //Forge: Can sometimes be called before we are added to the global world list. So use the less accurate one during that. It'll be recalculated later
        return !loaded ? state.getLightOpacity() : state.getLightOpacity(world, new BlockPos(this.x << 4 | x & 15, y, this.z << 4 | z & 15));
    }


    public IBlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }


    public IBlockState getBlockState(final int x, final int y, final int z) {
        if (y >= 0 && y < MAX_HEIGHT) {
            ExtendedBlockStorage storage;
            if ((storage = this.storageArrays[y >> 4]) != null) return storage.get(x & 15, y & 15, z & 15);
        }
        return BlockIDIndexes.AIR_STATE;
    }


    public IBlockState getBlockStateUnchecked(final int x, final int y, final int z){
        ExtendedBlockStorage storage;
        if((storage = this.storageArrays[y >> 4]) == null) return BlockIDIndexes.AIR_STATE;
        return storage.get(x & 15, y & 15, z & 15);
    }


    @Nullable
    public IBlockState setBlockState(final BlockPos pos, final IBlockState state) {
        int px = pos.getX() & 15, py = pos.getY(), pz = pos.getZ() & 15, xzIndex = pz << 4 | px;

        if (py >= this.precipitationHeightMap[xzIndex] - 1) {
            this.precipitationHeightMap[xzIndex] = -999;
        }
        IBlockState prev = this.getBlockState(pos);

        if (prev == state) {
            return null;
        } else {
            int i1 = this.heightMap[xzIndex] & 255;
            Block block = state.getBlock(), prevBlock = prev.getBlock();
            int k1 = prev.getLightOpacity(this.world, pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage storage = this.storageArrays[py >> 4];
            boolean flag = false;

            if (storage == null) {
                if (block == Blocks.AIR) {
                    return null;
                }
                storage = new ExtendedBlockStorage(py >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[py >> 4] = storage;
                flag = py >= i1;
            }

            storage.set(px, py & 15, pz, state);

            if (!this.world.isRemote) {
                if (prevBlock != block) prevBlock.breakBlock(this.world, pos, prev);

                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, prev, state))
                    this.world.removeTileEntity(pos);

            } else if (prev.hasTile()) {
                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, prev, state))
                    this.world.removeTileEntity(pos);
            }

            if (storage.get(px, py & 15, pz).getBlock() != block) {
                return null;
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int j1 = state.getLightOpacity(this.world, pos);

                    if (j1 > 0) {
                        if (py >= i1) {
                            this.relightBlock(px, py + 1, pz);
                        }
                    } else if (py == i1 - 1) {
                        this.relightBlock(px, py, pz);
                    }

                    if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(px, pz);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                if (!this.world.isRemote && prevBlock != block && (!this.world.captureBlockSnapshots || state.hasTile())) {
                    block.onBlockAdded(this.world, pos, state);
                }

                if (state.hasTile()) {
                    TileEntity tileEntity = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileEntity == null) {
                        tileEntity = block.createTileEntity(this.world, state);
                        this.world.setTileEntity(pos, tileEntity);
                    }

                    if (tileEntity != null) {
                        tileEntity.updateContainingBlockInfo();
                    }
                }
                this.dirty = true;
                return prev;
            }
        }
    }


    public void setBlockStateFast(int x, int y, int z, final IBlockState current, final IBlockState newState) {
        int i = x & 15, k = z & 15, l = k << 4 | i;
        if (y >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }
        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null) {
            storage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[y >> 4] = storage;
        }
        storage.set(i, y & 15, k, newState);

        if (current.hasTile()) {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            if (te != null && te.shouldRefresh(this.world, pos, current, newState))
                this.world.removeTileEntity(pos);
        }
    }


    public void setBlockStateFastNoTile(int x, int y, int z, final IBlockState state) {
        int l = (z &= 15) << 4 | (x &= 15);
        short[] pchm;
        if (y >= (pchm = this.precipitationHeightMap)[l] - 1) pchm[l] = -999;

        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null) {
            storage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[y >> 4] = storage;
        }
        storage.set(x, y & 15, z, state);
    }


    @Keep
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            return this.canSeeSky(pos) ? type.defaultLightValue : 0;
        } else if (type == EnumSkyBlock.SKY) {
            return !this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
        } else {
            return type == EnumSkyBlock.BLOCK ? extendedblockstorage.getBlockLight(i, j & 15, k) : type.defaultLightValue;
        }
    }


    @Keep
    public void setLightFor(final EnumSkyBlock type, final BlockPos pos, final int value) {
        final int ax = pos.getX() & 15, ay = pos.getY(), az = pos.getZ() & 15;
        ExtendedBlockStorage storage = this.storageArrays[ay >> 4];

        if (storage == NULL_BLOCK_STORAGE) {
            storage = new ExtendedBlockStorage(ay >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[ay >> 4] = storage;
            this.generateSkylightMap();
        }

        this.dirty = true;

        if (type == EnumSkyBlock.SKY) {
            if (this.world.provider.hasSkyLight()) {
                storage.setSkyLight(ax, ay & 15, az, value);
            }
        } else if (type == EnumSkyBlock.BLOCK) {
            storage.setBlockLight(ax, ay & 15, az, value);
        }
    }


    public int getLightSubtracted(BlockPos pos, int amount) {
        int i = pos.getX() & 15, j = pos.getY(), k = pos.getZ() & 15;
        ExtendedBlockStorage storage = this.storageArrays[j >> 4];

        if (storage == null) {
            return this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        } else {
            int l = !this.world.provider.hasSkyLight() ? 0 : storage.getSkyLight(i, j & 15, k);
            l = l - amount;
            int i1 = storage.getBlockLight(i, j & 15, k);

            if (i1 > l) l = i1;
            return l;
        }
    }


    public void addEntity(Entity entityIn) {
        this.hasEntities = true;
        if(ElcaCV.CHECK_LEGAL_CHUNK_CHANGE) {
            int i = MathHelper.floor(entityIn.posX)>>4;
            int j = MathHelper.floor(entityIn.posZ)>>4;

            if (i != this.x || j != this.z) {
                LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.x, this.z, entityIn);
                entityIn.setDead();
            }
        }
        int part = MathHelper.floor(entityIn.posY)>>4;

        if (part < 0) {
            part = 0;
        }

        if (part >= this.entityLists.length) {
            part = this.entityLists.length - 1;
        }

        MinecraftForge.EVENT_BUS.postF(world.enteringChunk.initEntering(entityIn, this.x, this.z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = this.x;
        entityIn.chunkCoordY = part;
        entityIn.chunkCoordZ = this.z;
        this.entityLists[part].add(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after an entity add
    }


    public void removeEntity(Entity entityIn) {
        this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
    }


    public void removeEntityAtIndex(Entity entityIn, int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= this.entityLists.length) {
            index = this.entityLists.length - 1;
        }
        this.entityLists[index].remove(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after entity removals
    }


    public boolean canSeeSky(BlockPos pos) {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        return y >= (this.heightMap[z << 4 | x] & 255);
    }


    public boolean canSeeSky(int x, int y, int z) {
        return y >= (this.heightMap[(z & 15) << 4 | x & 15] & 255);
    }


    @Nullable
    private TileEntity createNewTileEntity(BlockPos pos) {
        IBlockState state = this.getBlockState(pos);
        return !state.hasTile()? null : state.getBlock().createTileEntity(this.world, state);
    }


    @Nullable
    public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType creationMode) {
        TileEntity tileentity = this.tileEntities.get(pos);

        if (tileentity != null && tileentity.isInvalid()) {
            tileEntities.remove(pos);
            tileentity = null;
        }

        if (tileentity == null) {
            if (creationMode == Chunk.EnumCreateEntityType.IMMEDIATE) {
                tileentity = this.createNewTileEntity(pos);
                this.world.setTileEntity(pos, tileentity);
            }
            else if (creationMode == Chunk.EnumCreateEntityType.QUEUED) {
                this.tileEntityPosQueue.put(pos.toImmutable());
            }
        }

        return tileentity;
    }


    public void addTileEntity(TileEntity tileEntityIn) {
        this.addTileEntity(tileEntityIn.getPos(), tileEntityIn);
        if (this.loaded) {
            this.world.addTileEntity(tileEntityIn);
        }
    }


    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
        if (tileEntityIn.getWorld() != this.world)
            tileEntityIn.setWorld(this.world);
        tileEntityIn.setPos(pos);
        if (getBlockState(pos).hasTile()) {
            if (this.tileEntities.containsKey(pos))
                this.tileEntities.get(pos).invalidate();
            tileEntityIn.validate();
            this.tileEntities.put(pos, tileEntityIn);
        }
    }


    public void removeTileEntity(BlockPos pos) {
        if (this.loaded) {
            TileEntity tileentity;
            if ((tileentity = this.tileEntities.remove(pos)) != null)
                tileentity.invalidate();
        }
    }


    public void onLoad() {
        this.loaded = true;
        this.world.addTileEntities(this.tileEntities.values());
        for (ClassInheritanceMultiMap<Entity> entities : this.entityLists) {
            this.world.loadEntities(com.google.common.collect.ImmutableList.copyOf(entities));
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.postF(new net.minecraftforge.event.world.ChunkEvent.Load(this));
    }


    public void onUnload() {
        java.util.Arrays.stream(entityLists).forEach(map -> map.getByClass(net.minecraft.entity.player.EntityPlayer.class).forEach(pl -> world.updateEntityWithOptionalForce(pl, false))); // FORGE - Fix for MC-92916
        this.loaded = false;
        for (TileEntity tileentity : this.tileEntities.values()) {
            this.world.markTileEntityForRemoval(tileentity);
        }
        for (ClassInheritanceMultiMap<Entity> entities : this.entityLists) {
            this.world.unloadEntities(entities);
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.postF(new net.minecraftforge.event.world.ChunkEvent.Unload(this));
    }


    public void markDirty() {
        this.dirty = true;
    }


    public void getEntitiesWithinAABBForEntityLimited(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate <? super Entity > filter, int max) {
        int minY = MathUtls.clamp16c(aabb.minY - World.MAX_ENTITY_RADIUS),
            maxY = MathUtls.clamp16c(aabb.maxY + World.MAX_ENTITY_RADIUS);

        Entity[] parts;
        Entity entity;

        if(filter == null) {
            for (EntityList<Entity> raw; minY <= maxY; ++minY)
                if (!(raw = this.entityLists[minY]).isEmptyC())
                    for (int i = 0; i < raw.sizeC();i++)
                        if ((entity = raw.getC(i)) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            listToFill.add(entity);
                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                        listToFill.add(entity1);
                            if (--max < 0) return;
                        }
        } else {
            for (EntityList<Entity> raw; minY <= maxY; ++minY)
                if (!(raw = this.entityLists[minY]).isEmptyC())
                    for (int i = 0; i < raw.sizeC();i++)
                        if ((entity = raw.getC(i)) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            if (filter.apply(entity)) listToFill.add(entity);

                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                            && (filter.apply(entity1)))
                                        listToFill.add(entity1);
                            if (--max < 0) return;
                        }
        }
    }


    public final void getEntitiesWithinAABBForEntityLimitedC(@Nullable Entity entityIn, AxisAlignedBB aabb, EntityList<Entity> listToFill, @Nullable Predicate <? super Entity > filter, int max) {
        int minY = MathUtls.clamp16c(aabb.minY - World.MAX_ENTITY_RADIUS),
            maxY = MathUtls.clamp16c(aabb.maxY + World.MAX_ENTITY_RADIUS);

        Entity[] parts;
        Entity entity;

        if(filter == null) {
            for (EntityList<Entity> raw; minY <= maxY; ++minY)
                //if ((cursor = (raw = this.entityLists[minY].rawValues()).sizeC()-1) != 0)
                for (int cursor = (raw = this.entityLists[minY]).sizeC()-1; cursor != -1; cursor--)
                    if ((entity = raw.getC(cursor)) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                        listToFill.addF(entity);
                        if ((parts = entity.getParts()) != null)
                            for (Entity entity1 : parts)
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                    listToFill.addF(entity1);
                        if (--max < 0) return;
                    }
        } else {
            for (EntityList<Entity> raw; minY <= maxY; ++minY)
                //if ((cursor = (raw = this.entityLists[minY].rawValues()).sizeC()) != 0)
                for (int cursor = (raw = this.entityLists[minY]).sizeC()-1; cursor != -1; cursor--)
                    if ((entity = raw.getC(cursor)) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                        if (filter.apply(entity)) listToFill.addF(entity);

                        if ((parts = entity.getParts()) != null)
                            for (Entity entity1 : parts)
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                        && (filter.apply(entity1)))
                                    listToFill.addF(entity1);
                        if (--max < 0) return;
                    }
        }
    }


    public void getEntitiesWithinAABBForEntity(@Nullable final Entity entityIn, final AxisAlignedBB aabb, final List<Entity> listToFill, @Nullable final Predicate <? super Entity > filter) {
        int minY = MathUtls.clamp16c(aabb.minY - World.MAX_ENTITY_RADIUS),
            maxY = MathUtls.clamp16c(aabb.maxY + World.MAX_ENTITY_RADIUS);

        Entity entity;

        if(filter == null)
            for (EntityList<Entity> raw; minY <= maxY; ++minY){
                if (!(raw = this.entityLists[minY]).isEmptyC())
                    for (int i = 0; i < raw.sizeC();i++)
                        if ((entity = raw.getC(i)).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                            listToFill.add(entity);
                            if (entity.getParts() != null)
                                for (Entity entity1 : entity.getParts())
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                        listToFill.add(entity1);
                        }
            }
        else
            for (EntityList<Entity> raw; minY <= maxY; ++minY){
                if (!(raw = this.entityLists[minY]).isEmptyC())
                    for (int i = 0; i < raw.sizeC();i++)
                        if ((entity = raw.getC(i)).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                            if (filter.apply(entity)) listToFill.add(entity);
                            if (entity.getParts() != null)
                                for (Entity entity1 : entity.getParts())
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                            && (filter.apply(entity1)))
                                        listToFill.add(entity1);
                        }
            }
    }


    public <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class <? extends T > entityClass, final AxisAlignedBB aabb, final List<T> listToFill, @Nullable final Predicate <? super T > filter) {
        final int i = MathUtls.clamp16c(aabb.minY - World.MAX_ENTITY_RADIUS),
                  j = MathUtls.clamp16c(aabb.maxY + World.MAX_ENTITY_RADIUS);
        for (int k = i; k <= j; ++k)
            for (T t : this.entityLists[k].getByClass(entityClass))
                if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t)))
                    listToFill.add(t);
    }


    public boolean needsSaving(boolean flag) {
        if (flag) {
            if (this.hasEntities && this.world.getTotalWorldTime() != this.lastSaveTime || this.dirty) {
                return true;
            }
        } else if (this.hasEntities && this.world.getTotalWorldTime() >= this.lastSaveTime + 600L) {
            return true;
        }
        return this.dirty;
    }


    public Random getRandomWithSeed(long seed) {
        return new Random(this.world.getSeed() + ((long) this.x * this.x * 4987142) + (this.x * 5947611L) + ((long) this.z * this.z) * 4392871L + (this.z * 389711L) ^ seed);
    }


    public boolean isEmpty() {
        return false;
    }


    public void populate(IChunkProvider provider, IChunkGenerator gen) {
        Chunk chunk = provider.getLoadedChunk(this.x, this.z - 1);
        Chunk chunk1 = provider.getLoadedChunk(this.x + 1, this.z);
        Chunk chunk2 = provider.getLoadedChunk(this.x, this.z + 1);
        Chunk chunk3 = provider.getLoadedChunk(this.x - 1, this.z);

        if (chunk1 != null && chunk2 != null && provider.getLoadedChunk(this.x + 1, this.z + 1) != null) {
            this.populate(gen);
        }

        if (chunk3 != null && chunk2 != null && provider.getLoadedChunk(this.x - 1, this.z + 1) != null) {
            chunk3.populate(gen);
        }

        if (chunk != null && chunk1 != null && provider.getLoadedChunk(this.x + 1, this.z - 1) != null) {
            chunk.populate(gen);
        }

        if (chunk != null && chunk3 != null) {
            chunk = provider.getLoadedChunk(this.x - 1, this.z - 1);
            if (chunk != null) {
                chunk.populate(gen);
            }
        }
    }


    protected void populate(IChunkGenerator generator) {
        if (isTerrainPopulated) {
            if(inhabitedTime < 3600L){
                ((WorldServer)world).addSyncTask(
                        () -> {
                            if (generator.generateStructures(Chunk.this, x, z)) {
                                Chunk.this.markDirty();
                            }
                        }
                );
            }
        } else {
            isTerrainPopulated = true;
            generator.populate(this.x, this.z);
            GameRegistry.generateWorld(this.x, this.z, this.world, generator, this.world.getChunkProvider());
            this.markDirty();
            checkLight();
        }
    }


    public void onTick(boolean skipRecheckGaps) {
        if (!skipRecheckGaps && this.isGapLightingUpdated && world.provider.hasSkyLight()) {
            if(SERVER_COMPILE)recheckGapsServer();
            else {
                if(world.isRemote)this.recheckGaps(true);
                else recheckGapsServer();
            }
        }
        this.ticked = true;

        if (!this.isLightPopulated && this.isTerrainPopulated) {
            this.checkLight();
        }
        while (this.tileEntityPosQueue.hasNext()) {
            BlockPos blockpos = this.tileEntityPosQueue.flush();

            if (this.getTileEntity(blockpos, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(blockpos).hasTile()) {
                TileEntity tile = this.createNewTileEntity(blockpos);
                this.world.setTileEntity(blockpos, tile);
                this.world.markBlockRangeForRenderUpdate(blockpos, blockpos);
            }
        }
    }


    public boolean isPopulated() {
        return this.ticked && this.isTerrainPopulated && this.isLightPopulated;
    }


    public boolean wasTicked() {
        return this.ticked;
    }


    public ChunkPos getPos() {
        return new ChunkPos(this.x, this.z);
    }


    public boolean isEmptyBetween(int startY, int endY) {
        if (startY < 0) {
            startY = 0;
        }
        if (endY >= 256) {
            endY = 255;
        }
        for (int i = startY; i <= endY; i += 16) {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i >> 4];
            if (extendedblockstorage != null && !extendedblockstorage.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
        System.arraycopy(newStorageArrays, 0, this.storageArrays, 0, this.storageArrays.length);
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf, int availableSections, boolean groundUpContinuous) {
        for(TileEntity tileEntity : tileEntities.values()) {
            tileEntity.updateContainingBlockInfo();
            tileEntity.getBlockMetadata();
            tileEntity.getBlockType();
        }

        boolean flag = this.world.provider.hasSkyLight();

        for (int i = 0; i < this.storageArrays.length; ++i) {
            ExtendedBlockStorage array = this.storageArrays[i];
            if ((availableSections & 1 << i) == 0) {
                if (groundUpContinuous && array != null) {
                    this.storageArrays[i] = null;
                }
            } else {
                if (array == null) {
                    array = new ExtendedBlockStorage(i << 4, flag);
                    this.storageArrays[i] = array;
                }
                array.read(buf);
                buf.readBytes(array.getBlockLightData());
                if (flag) {
                    buf.readBytes(array.getSkyLightData());
                }
            }
        }
        if (groundUpContinuous) {
            buf.readBytes(this.blockBiomeArray);
        }
        for (int j = 0; j < this.storageArrays.length; ++j) {
            if (this.storageArrays[j] != null && (availableSections & 1 << j) != 0) {
                this.storageArrays[j].recalculateRefCounts();
            }
        }
        this.isLightPopulated = true;
        this.isTerrainPopulated = true;
        this.generateHeightMap();

        List<TileEntity> invalidList = new java.util.ArrayList<>();

        for (TileEntity tileentity : this.tileEntities.values()) {
            if (tileentity.shouldRefresh(this.world, tileentity.getPos(), tileentity.getBlockType().getStateFromMeta(tileentity.getBlockMetadata()), getBlockState(tileentity.getPos())))
                invalidList.add(tileentity);
            tileentity.updateContainingBlockInfo();
        }
        for (TileEntity te : invalidList) te.invalidate();
    }


    public Biome getBiome(BlockPos pos, BiomeProvider provider) {
        int x = pos.getX() & 15, z = pos.getZ() & 15, k = this.blockBiomeArray[z << 4 | x] & 255;
        if (k == 255) {
            Biome biome = world.isRemote ? Biomes.PLAINS : provider.getBiome(pos, Biomes.PLAINS);
            k = Biome.getIdForBiome(biome);
            this.blockBiomeArray[z << 4 | x] = (byte)(k & 255);
        }

        Biome biome1 = BiomeIndexes.get(k);
        return biome1 == null ? Biomes.PLAINS : biome1;
    }


    public Biome getBiome(int x, int z, BiomeProvider provider) {
        int index = this.blockBiomeArray[(z & 15) << 4 | x & 15] & 255;
        if (index == 255) {
            Biome biome = world.isRemote ? Biomes.PLAINS : provider.getBiome(x, z, Biomes.PLAINS);
            index = Biome.getIdForBiome(biome);
            this.blockBiomeArray[(z & 15) << 4 | (x & 15)] = (byte)(index & 255);
        }
        Biome biome1 = BiomeIndexes.get(index);
        return biome1 == null ? Biomes.PLAINS : biome1;
    }


    public byte[] getBiomeArray() {
        return this.blockBiomeArray;
    }


    public void setBiomeArray(byte[] biomeArray) {
        System.arraycopy(biomeArray, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
    }


    public void resetRelightChecks() {
        this.queuedLightChecks = 0;
    }


    public void enqueueRelightChecks() {
        if (this.queuedLightChecks < 4096) {
            int px = this.x << 4, pz = this.z << 4, i, j, k, l;
            boolean flag;
            for (i = 0; i < 8; ++i) {
                if (this.queuedLightChecks >= 4096) return;
                j = this.queuedLightChecks & 15;
                k = (this.queuedLightChecks >> 4) & 15;
                l = this.queuedLightChecks >> 8;
                ++this.queuedLightChecks;

                for (int i1 = 0; i1 < 16; ++i1) {
                    if(this.storageArrays[j] == null)
                        flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;
                    else
                        flag = this.storageArrays[j].get(k, i1, l).getMaterial() == Material.AIR;

                    if(flag) {
                        int ly = (j << 4) + i1;
                        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();
                        for (EnumFacing enumfacing : EnumFacing.VALUES) {
                            if (getBlockState(
                                    pos2.setPos(
                                            px + k + enumfacing.getXOffset(), ly + enumfacing.getYOffset(), pz + l + enumfacing.getZOffset()
                                    )
                            ).getLightValue(this.world, pos2) > 0)
                                this.world.checkLight(pos2);
                        }
                        this.world.checkLight(pos2.setPos(px + k, ly, pz + l));
                    }
                }
            }
        }
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    public void checkLight() {
        this.isTerrainPopulated = true;
        if (!((IChunkLightingData)this).isLightInitialized()) {
            LightingHooks.initChunkLighting(this, world);
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (x != 0 || z != 0) {
                    final Chunk nChunk = world.getChunkProvider().getLoadedChunk(this.x + x, this.z + z);
                    if (nChunk == null || !((IChunkLightingData)nChunk).isLightInitialized()) {
                        return;
                    }
                }
            }
        }
        isLightPopulated = true;
    }


    public void setSkylightUpdated() {
        //updateSkylights.length = 16
        for (int i = 0 ; i < 16; i++){
            updateSkylights[i] = ~0;
        }
        this.recheckGaps(false);
    }


    private void checkLightSide(EnumFacing facing) {
        if (this.isTerrainPopulated) {
            if (facing == EnumFacing.EAST) {
                for (int i = 0; i < 16; ++i) {
                    this.checkLight(15, i);
                }
            } else if (facing == EnumFacing.WEST) {
                for (int j = 0; j < 16; ++j) {
                    this.checkLight(0, j);
                }
            }
            else if (facing == EnumFacing.SOUTH) {
                for (int k = 0; k < 16; ++k) {
                    this.checkLight(k, 15);
                }
            } else if (facing == EnumFacing.NORTH) {
                for (int l = 0; l < 16; ++l) {
                    this.checkLight(l, 0);
                }
            }
        }
    }


    private boolean checkLight(int x, int z) {
        int i = this.getTopFilledSegment();
        boolean flag = false, flag1 = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j) {
            pos.setPos(pos.getX(), j, pos.getZ());
            int k = this.getBlockLightOpacity(pos);

            if (k == 255 && pos.getY() < this.world.getSeaLevel()) {
                flag1 = true;
            }

            if (!flag && k > 0) {
                flag = true;
            }
            else if (flag && k == 0 && !this.world.checkLight(pos)) {
                return false;
            }
        }

        for (int l = pos.getY(); l > 0; --l) {
            pos.setPos(pos.getX(), l, pos.getZ());
            if (this.getBlockState(pos).getLightValue(this.world, pos) > 0) {
                this.world.checkLight(pos);
            }
        }
        return true;
    }


    public boolean isLoaded() {
        return this.loaded;
    }


    @SideOnly(Side.CLIENT)
    public void markLoaded(boolean loaded) {
        this.loaded = loaded;
    }


    public World getWorld() {
        return this.world;
    }


    public int[] getHeightMap() {
        int[] heightMap = new int[256];
        byte[] realMap = this.heightMap;
        for (int i = 0; i < 255; i++){
            heightMap[i] = realMap[i] & 255;
        }
        return heightMap;
    }


    public byte[] getHeightMapRaw(){
        return this.heightMap;
    }


    public void setHeightMap(int[] newHeightMap) {
        final byte[] realMap = this.heightMap;
        for (int i = 0; i < 255; i++){
            realMap[i] = (byte) newHeightMap[i];
        }
        this.heightMapMinimum = com.google.common.primitives.Ints.min(newHeightMap); // Forge: fix MC-117412
    }


    public void setHeightMap(byte[] newHeightMap) {
        System.arraycopy(newHeightMap, 0, this.heightMap, 0, 255);
        this.heightMapMinimum = getMinHeightMap(newHeightMap);
    }


    private static int getMinHeightMap(byte... array) {
        int min = array[0] & 255;

        for(int i = 1; i < array.length; ++i) {
            int n = array[i] & 255;
            if (n < min) {
                min = n;
            }
        }
        return min;
    }


    public Map<BlockPos, TileEntity> getTileEntityMap() {
        return this.tileEntities;
    }


    public ClassInheritanceMultiMap<Entity>[] getEntityLists() {
        return this.entityLists;
    }


    public boolean isTerrainPopulated() {
        return this.isTerrainPopulated;
    }


    public void setTerrainPopulated(boolean terrainPopulated) {
        this.isTerrainPopulated = terrainPopulated;
    }


    public boolean isLightPopulated() {
        return this.isLightPopulated;
    }


    public void setLightPopulated(boolean lightPopulated) {
        this.isLightPopulated = lightPopulated;
    }


    public void setModified(boolean modified) {
        this.dirty = modified;
    }


    public void setHasEntities(boolean hasEntitiesIn) {
        this.hasEntities = hasEntitiesIn;
    }


    public void setLastSaveTime(long saveTime) {
        this.lastSaveTime = saveTime;
    }


    public int getLowestHeight() {
        return this.heightMapMinimum;
    }


    public long getInhabitedTime() {
        return this.inhabitedTime;
    }


    public void setInhabitedTime(long newInhabitedTime) {
        this.inhabitedTime = newInhabitedTime;
    }


    public enum EnumCreateEntityType {
        IMMEDIATE,
        QUEUED,
        CHECK;
    }

    /* ======================================== FORGE START =====================================*/
    /**
     * Removes the tile entity at the specified position, only if it's
     * marked as invalid.
     */
    public void removeInvalidTileEntity(BlockPos pos) {
        if (loaded) {
            TileEntity entity = tileEntities.get(pos);
            if (entity != null && entity.isInvalid()) {
                tileEntities.remove(pos);
            }
        }
    }


    private void logCascadingWorldGeneration()
    {
//        net.minecraftforge.fml.common.ModContainer activeModContainer = net.minecraftforge.fml.common.Loader.instance().activeModContainer();
//        String format = "{} loaded a new chunk {} in dimension {} ({}) while populating chunk {}, causing cascading worldgen lag.";
//
//        if (activeModContainer == null) { // vanilla minecraft has problems too (MC-114332), log it at a quieter level.
//            net.minecraftforge.fml.common.FMLLog.log.debug(format, "Minecraft", this.getPos(), this.world.provider.getDimension(), this.world.provider.getDimensionType().getName(), populating);
//            net.minecraftforge.fml.common.FMLLog.log.debug("Consider setting 'fixVanillaCascading' to 'true' in the Forge config to fix many cases where this occurs in the base game.");
//        } else {
//            net.minecraftforge.fml.common.FMLLog.log.warn(format, activeModContainer.getName(), this.getPos(), this.world.provider.getDimension(), this.world.provider.getDimensionType().getName(), populating);
//            net.minecraftforge.fml.common.FMLLog.log.warn("Please report this to the mod's issue tracker. This log can be disabled in the Forge config.");
//        }
    }


    //private net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;
    //capabilities = net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(this);
    @Nullable
    public net.minecraftforge.common.capabilities.CapabilityDispatcher getCapabilities() {
        return null;//capabilities
    }
    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable EnumFacing facing) {
        return false; //return capabilities != null && capabilities.hasCapability(capability, facing);
    }
    @Override
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable EnumFacing facing) {
        return null; //return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }


    public int getLightSubtracted(int x, int y, int z, int amount) {
        int i = x & 15, k = z & 15;
        ExtendedBlockStorage storage = this.storageArrays[y >> 4];
        if (storage == null) {
            return this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        } else {
            i = storage.getBlockLight(i, y & 15, k);
            k = (!this.world.provider.hasSkyLight() ? 0 : storage.getSkyLight(i, y & 15, k)) - amount;
            if (i > k) k = i;
            return k;
        }
    }


    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return getPrecipitationHeight(pos.getX(), pos.getZ());
    }


    public BlockPos getPrecipitationHeight(int x, int z) {
        return new BlockPos(x, getPrecipitationHeightY(x, z), z);
    }


    public int getPrecipitationHeightY(int x, int z) {
        int i = x & 15, j = z & 15, k = i | j << 4;
        int pY = precipitationHeightMap[k];
        if (pY == -999) {
            pY = this.getTopFilledSegment() + 15;
            int i1 = -1;
            while (pY > 0 && i1 == -1) {
                IBlockState iblockstate = this.getBlockState(x, pY, z);
                Material material = iblockstate.getMaterial();
                if (!material.blocksMovement() && !material.isLiquid()) {
                    --pY;
                } else {
                    i1 = pY + 1;
                }
            }
            this.precipitationHeightMap[k] = (short) i1;
        }
        return this.precipitationHeightMap[k];
    }


    @Override
    public String toString() {
        String base = "Chunk["+x+", "+z+"]";
        if(isEmpty()){
            base += ", empty";
        } else {
            if(world != null){
                if(world.isRemote){
                    base += ", remote";
                }
                base += ", dim:" + world.provider.getDimension();
            }
            if(isLoaded()) {
                base += ", loaded";
            }
            if(isPopulated()){
                base += ", populated";
            }
            if(isNowSaving()){
                base += ", saving";
            }
            if(isLightPopulated()){
                base += ", lighted";
            }
            if(dirty){
                base += ", dirty";
            }
        }
        return base;
    }
}
