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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.asyutl.buf.FastBuffer;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.api.PWorldServer;
import plus.fserver.event.ElcaCV;
import plus.fserver.world.light.api.ILightingEngine;
import plus.fserver.world.light.api.ILightingEngineProvider;
import plus.tson.utl.Te4HashMap;


public class Chunk implements net.minecraftforge.common.capabilities.ICapabilityProvider, ILightingEngineProvider {
    public static final EnumFacing[] HORIZONTAL = EnumFacing.Plane.HORIZONTAL.facings();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
    private final ExtendedBlockStorage[] storageArrays;
    private final byte[] blockBiomeArray;
    private final short[] precipitationHeightMap;
    private final short[] updateSkylights;
    //private final boolean[] updateSkylightColumns;
    private boolean loaded;
    private final World world;
    private final int[] heightMap;
    public final int x;
    public final int z;
    private boolean isGapLightingUpdated;
    private final Map<BlockPos, TileEntity> tileEntities;
    private final ClassInheritanceMultiMap<Entity>[] entityLists;
    private boolean isTerrainPopulated;
    private boolean isLightPopulated;
    private boolean ticked;
    private boolean dirty;
    private boolean hasEntities;
    private long lastSaveTime;
    private int heightMapMinimum;
    private long inhabitedTime;
    private int queuedLightChecks;
    private final FastBuffer<BlockPos> tileEntityPosQueue;
//    public boolean unloadQueued;
    public byte state = 0;
    //00 (0) = default
    //01 (1) = tracked
    //10 (2) = requested

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
        return (state & 3) == 0;
    }

    public Chunk(World worldIn, int x, int z) {
        this.storageArrays = new ExtendedBlockStorage[16];
        this.blockBiomeArray = new byte[256];
        this.precipitationHeightMap = new short[256];
        this.updateSkylights = new short[16];

        this.tileEntities = new Te4HashMap<>();
        this.queuedLightChecks = 4096;
        this.tileEntityPosQueue = new FastBuffer<>();
        this.entityLists = new ClassInheritanceMultiMap[16];
        this.world = worldIn;
        this.x = x;
        this.z = z;
        this.heightMap = new int[256];

        for (int i = 0; i < this.entityLists.length; ++i) {
            this.entityLists[i] = new ClassInheritanceMultiMap<>(Entity.class);
        }

        Arrays.fill(this.precipitationHeightMap, (short) -999);
        Arrays.fill(this.blockBiomeArray, (byte) - 1);
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
        for (int yPos = 0, xPos, sae, zPOs, ids, pids=-10; yPos < 256; ++yPos) {
            sae = yPos >> 4;
            for (xPos = 0; xPos < 16; ++xPos) {
                for (zPOs = 0; zPOs < 16; ++zPOs) {
                    ids = primer.getBlockStateId(xPos, yPos, zPOs);
                    if (ids != pids)
                        flag2 = (state = BlockIDIndexes.BLOCK_INDEX.get(pids = ids)) != null
                                && state.getMaterial() != Material.AIR;

                    if (flag2) {
                        if (this.storageArrays[sae] == null) {
                            this.storageArrays[sae] = new ExtendedBlockStorage(sae << 4, flag);
                        }
                        this.storageArrays[sae].set(xPos, yPos & 15, zPOs, state);
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
        return this.heightMap[z << 4 | x];
    }


    @Nullable
    private ExtendedBlockStorage getLastExtendedBlockStorage() {
        for (int i = this.storageArrays.length - 1; i >= 0; --i)
            if (this.storageArrays[i] != null)
                return this.storageArrays[i];
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
                        this.heightMap[k << 4 | j] = l;
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
        this.heightMapMinimum = Integer.MAX_VALUE;

        for (int xPos = 0; xPos < 16; ++xPos) {
            for (int zPos = 0; zPos < 16; ++zPos) {
                this.precipitationHeightMap[xPos + (zPos << 4)] = -999;
                for (int l = i + 16; l > 0; --l) {
                    if (this.getBlockLightOpacity(xPos, l - 1, zPos) != 0) {
                        this.heightMap[zPos << 4 | xPos] = l;
                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }
                        break;
                    }
                }

                if (this.world.provider.hasSkyLight()) {
                    int k1 = 15, i1 = i + 16 - 1;
                    while (true) {
                        int j1 = this.getBlockLightOpacity(xPos, i1, zPos);

                        if (j1 == 0 && k1 != 15) {
                            j1 = 1;
                        }
                        k1 -= j1;
                        if (k1 > 0) {
                            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                            if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                extendedblockstorage.setSkyLight(xPos, i1 & 15, zPos, k1);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + xPos, i1, (this.z << 4) + zPos));
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
//        this.updateSkylightColumns[x + z * 16] = true;
        this.isGapLightingUpdated = true;
    }


    private void recheckGaps(boolean onlyOne) {
        if (this.world.isAreaLoaded(new BlockPos(this.x * 16 + 8, 0, this.z * 16 + 8), 16)) {
            int mask;
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    mask = 1<<z;
                    if ((updateSkylights[x] & mask) == mask) {
                        updateSkylights[x] &= ~mask;
                        //this.updateSkylightColumns[x + z * 16] = false;
                        int l = this.x * 16 + x;
                        int i1 = this.z * 16 + z;
                        int j1 = Integer.MAX_VALUE;

                        for (EnumFacing enumfacing : HORIZONTAL) {
                            j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumfacing.getXOffset(), i1 + enumfacing.getZOffset()));
                        }
                        this.checkSkylightNeighborHeight(l, i1, j1);

                        for (EnumFacing facing : HORIZONTAL) {
                            this.checkSkylightNeighborHeight(l + facing.getXOffset(), i1 + facing.getZOffset(), getHeightValue(x, z));
                        }
                        if (onlyOne) return;
                    }
                }
            }
            this.isGapLightingUpdated = false;
        }
    }


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


    //todo in work
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
            this.heightMap[z << 4 | x] = j;
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

            int l1 = this.heightMap[z << 4 | x];
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
        if (y >= 0 && y >> 4 < storageArrays.length) {
            ExtendedBlockStorage storage = this.storageArrays[y >> 4];
            if (storage != null) return storage.get(x & 15, y & 15, z & 15);
        }
        return BlockIDIndexes.AIR_STATE;
    }


    @Nullable
    public IBlockState setBlockState(final BlockPos pos, final IBlockState state) {
        int i = pos.getX() & 15, j = pos.getY(), k = pos.getZ() & 15, l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }
        IBlockState iblockstate = this.getBlockState(pos);

        if (iblockstate == state) {
            return null;
        } else {
            int i1 = this.heightMap[l];
            Block block = state.getBlock(), block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(this.world, pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage storage = this.storageArrays[j >> 4];
            boolean flag = false;

            if (storage == null) {
                if (block == Blocks.AIR) {
                    return null;
                }
                storage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[j >> 4] = storage;
                flag = j >= i1;
            }

            storage.set(i, j & 15, k, state);

            if (!this.world.isRemote) {
                if (block1 != block) block1.breakBlock(this.world, pos, iblockstate);

                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                    this.world.removeTileEntity(pos);

            } else if (block1.hasTileEntity(iblockstate)) {
                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                    this.world.removeTileEntity(pos);
            }

            if (storage.get(i, j & 15, k).getBlock() != block) {
                return null;
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int j1 = state.getLightOpacity(this.world, pos);

                    if (j1 > 0) {
                        if (j >= i1) {
                            this.relightBlock(i, j + 1, k);
                        }
                    } else if (j == i1 - 1) {
                        this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(i, k);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                if (!this.world.isRemote && block1 != block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                    block.onBlockAdded(this.world, pos, state);
                }

                if (block.hasTileEntity(state)) {
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
                return iblockstate;
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

        if (current.getBlock().hasTileEntity(current)) {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            if (te != null && te.shouldRefresh(this.world, pos, current, newState))
                this.world.removeTileEntity(pos);
        }
    }


    public void setBlockStateFastNoTile(int x, int y, int z, final IBlockState state) {
        int l = (z &= 15) << 4 | (x &= 15);
        if (y >= this.precipitationHeightMap[l] - 1) this.precipitationHeightMap[l] = -999;

        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null) {
            storage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[y >> 4] = storage;
        }
        storage.set(x, y & 15, z, state);
    }


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


    public void setLightFor(EnumSkyBlock type, BlockPos pos, int value) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[j >> 4] = extendedblockstorage;
            this.generateSkylightMap();
        }

        this.dirty = true;

        if (type == EnumSkyBlock.SKY) {
            if (this.world.provider.hasSkyLight()) {
                extendedblockstorage.setSkyLight(i, j & 15, k, value);
            }
        } else if (type == EnumSkyBlock.BLOCK) {
            extendedblockstorage.setBlockLight(i, j & 15, k, value);
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

        MinecraftForge.EVENT_BUS.post(world.enteringChunk.initEntering(entityIn, this.x, this.z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
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
        return y >= this.heightMap[z << 4 | x];
    }


    public boolean canSeeSky(int x, int y, int z) {
        return y >= this.heightMap[(z & 15) << 4 | x & 15];
    }


    @Nullable
    private TileEntity createNewTileEntity(BlockPos pos)
    {
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return !block.hasTileEntity(iblockstate) ? null : block.createTileEntity(this.world, iblockstate);
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
        IBlockState state = getBlockState(pos);
        if (state.getBlock().hasTileEntity(state)) {
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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(this));
    }


    public void onUnload() {
        java.util.Arrays.stream(entityLists).forEach(multimap -> com.google.common.collect.Lists.newArrayList(multimap.getByClass(net.minecraft.entity.player.EntityPlayer.class)).forEach(player -> world.updateEntityWithOptionalForce(player, false))); // FORGE - Fix for MC-92916
        this.loaded = false;
        for (TileEntity tileentity : this.tileEntities.values()) {
            this.world.markTileEntityForRemoval(tileentity);
        }
        for (ClassInheritanceMultiMap<Entity> entities : this.entityLists) {
            this.world.unloadEntities(entities);
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(this));
    }


    public void markDirty() {
        this.dirty = true;
    }


    public void getEntitiesWithinAABBForEntityLimited(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate <? super Entity > filter, int max) {
        int i = MathHelper.clamp(
                MathHelper.floor(aabb.minY - World.MAX_ENTITY_RADIUS)>>4,
                0, 15
        ),
                j = MathHelper.clamp(
                        MathHelper.floor(aabb.maxY + World.MAX_ENTITY_RADIUS)>>4,
                        0, 15
                );
        Entity[] parts;
        Entity entity;

        if(filter == null) {
            for (;i <= j; ++i)
                if (!this.entityLists[i].isEmpty()) {
                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                         iter.hasNext();
                    )
                        if ((entity = iter.readAndNext()) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            listToFill.add(entity);
                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                        listToFill.add(entity1);
                            if(--max<0)return;
                        }
                }
        } else {
            for (;i <= j; ++i)
                if (!this.entityLists[i].isEmpty()) {
                    for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                         iter.hasNext();
                    )
                        if ((entity = iter.readAndNext()) != entityIn && entity.getEntityBoundingBox().intersects(aabb)) {
                            if (filter.apply(entity)) listToFill.add(entity);

                            if ((parts = entity.getParts()) != null)
                                for (Entity entity1 : parts)
                                    if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                            && (filter.apply(entity1)))
                                        listToFill.add(entity1);
                            if(--max<0)return;
                        }
                }
        }
    }


    public void getEntitiesWithinAABBForEntity(@Nullable final Entity entityIn, final AxisAlignedBB aabb, final List<Entity> listToFill, @Nullable final Predicate <? super Entity > filter) {
        int i = MathHelper.clamp(
                    MathHelper.floor(aabb.minY - World.MAX_ENTITY_RADIUS)>>4,
                    0, 15
            ),
            j = MathHelper.clamp(
                    MathHelper.floor(aabb.maxY + World.MAX_ENTITY_RADIUS)>>4,
                    0, 15
            );
        if(filter == null) {
            for (Entity entity;i <= j; ++i) if (!this.entityLists[i].isEmpty()) {
                for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                     iter.hasNext();
                )
                    if ((entity = iter.readAndNext()).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        listToFill.add(entity);
                        if (entity.getParts() != null)
                            for (Entity entity1 : entity.getParts())
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb))
                                    listToFill.add(entity1);
                    }
            }
        } else {
            for (Entity entity;i <= j; ++i) if (!this.entityLists[i].isEmpty()) {
                for (ClassInheritanceMultiMap<Entity>.FastIter iter = this.entityLists[i].fastIter();
                     iter.hasNext();
                )
                    if ((entity = iter.readAndNext()).getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        if (filter.apply(entity)) listToFill.add(entity);
                        if (entity.getParts() != null)
                            for (Entity entity1 : entity.getParts())
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb)
                                        && (filter.apply(entity1)))
                                    listToFill.add(entity1);
                    }
            }
        }
    }


    public <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class <? extends T > entityClass, final AxisAlignedBB aabb, final List<T> listToFill, @Nullable final Predicate <? super T > filter) {
        final int i = MathHelper.clamp(
                MathHelper.floor(aabb.minY - World.MAX_ENTITY_RADIUS)>>4,
                0, 15
        ),
                j = MathHelper.clamp(
                        MathHelper.floor(aabb.maxY + World.MAX_ENTITY_RADIUS)>>4,
                        0, 15
                );
        for (int k = i; k <= j; ++k) {
            for (T t : this.entityLists[k].getByClass(entityClass)) {
                if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t)))
                    listToFill.add(t);
            }
        }
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


    public void populate(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
        Chunk chunk = chunkProvider.getLoadedChunk(this.x, this.z - 1);
        Chunk chunk1 = chunkProvider.getLoadedChunk(this.x + 1, this.z);
        Chunk chunk2 = chunkProvider.getLoadedChunk(this.x, this.z + 1);
        Chunk chunk3 = chunkProvider.getLoadedChunk(this.x - 1, this.z);

        if (chunk1 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.x + 1, this.z + 1) != null) {
            this.populate(chunkGenrator);
        }

        if (chunk3 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.x - 1, this.z + 1) != null) {
            chunk3.populate(chunkGenrator);
        }

        if (chunk != null && chunk1 != null && chunkProvider.getLoadedChunk(this.x + 1, this.z - 1) != null) {
            chunk.populate(chunkGenrator);
        }

        if (chunk != null && chunk3 != null) {
            chunk = chunkProvider.getLoadedChunk(this.x - 1, this.z - 1);
            if (chunk != null) {
                chunk.populate(chunkGenrator);
            }
        }
    }


    protected void populate(IChunkGenerator generator) {
        if (isTerrainPopulated) {
            if(inhabitedTime < 3600L){
                ((PWorldServer)world).addSyncTask(
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
        if (this.isGapLightingUpdated && this.world.provider.hasSkyLight() && !skipRecheckGaps) {
            this.recheckGaps(this.world.isRemote);
        }
        this.ticked = true;

        if (!this.isLightPopulated && this.isTerrainPopulated) {
            this.checkLight();
        }
        while (this.tileEntityPosQueue.hasNext()) {
            BlockPos blockpos = this.tileEntityPosQueue.flush();

            if (this.getTileEntity(blockpos, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(blockpos).getBlock().hasTileEntity(this.getBlockState(blockpos))) {
                TileEntity tileentity = this.createNewTileEntity(blockpos);
                this.world.setTileEntity(blockpos, tileentity);
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
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i];
            if ((availableSections & 1 << i) == 0) {
                if (groundUpContinuous && extendedblockstorage != null) {
                    this.storageArrays[i] = null;
                }
            } else {
                if (extendedblockstorage == null) {
                    extendedblockstorage = new ExtendedBlockStorage(i << 4, flag);
                    this.storageArrays[i] = extendedblockstorage;
                }
                extendedblockstorage.getData().read(buf);
                buf.readBytes(extendedblockstorage.getBlockLight().getData());
                if (flag) {
                    buf.readBytes(extendedblockstorage.getSkyLight().getData());
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

        Biome biome1 = Biome.getBiome(k);
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
                j = this.queuedLightChecks % 16;
                k = this.queuedLightChecks / 16 % 16;
                l = this.queuedLightChecks / 256;
                ++this.queuedLightChecks;

                for (int i1 = 0; i1 < 16; ++i1) {
                    if(this.storageArrays[j] == null){
                        flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;
                    } else {
                        flag = this.storageArrays[j].get(k, i1, l).getMaterial() == Material.AIR;
                    }
                    if(flag) {
                        int ly = (j << 4) + i1;
                        for (EnumFacing enumfacing : EnumFacing.values()) {
                            BlockPos pos2 = offset(px + k, ly, pz + l, enumfacing);
                            if (getBlockState(pos2).getLightValue(this.world, pos2) > 0) {
                                this.world.checkLight(pos2);
                            }
                        }
                        this.world.checkLight(new BlockPos(px + k, ly, pz + l));
                    }
                }
            }
        }
    }


    private BlockPos offset(int x, int y, int z, EnumFacing facing) {
        return new BlockPos(
                x + facing.getXOffset(),
                y + facing.getYOffset(),
                z + facing.getZOffset()
        );
    }


    //Overwrited
    public void checkLight() {
        this.isTerrainPopulated = true;
        this.isLightPopulated = true;
        BlockPos blockpos = new BlockPos(this.x << 4, 0, this.z << 4);

        if (this.world.provider.hasSkyLight()) {
            if (this.world.isAreaLoaded(blockpos.add(-1, 0, -1), blockpos.add(16, this.world.getSeaLevel(), 16))) {
                label44:
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        if (!this.checkLight(i, j)) {
                            this.isLightPopulated = false;
                            break label44;
                        }
                    }
                }
                if (this.isLightPopulated) {
                    for (EnumFacing enumfacing : HORIZONTAL) {
                        int k = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                        this.world.getChunk(blockpos.offset(enumfacing, k)).checkLightSide(enumfacing.getOpposite());
                    }
                    this.setSkylightUpdated();
                }
            } else {
                this.isLightPopulated = false;
            }
        }
    }


    public void setSkylightUpdated() {
        for (int i=0;i<updateSkylights.length;i++){
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
        return this.heightMap;
    }


    public void setHeightMap(int[] newHeightMap) {
        System.arraycopy(newHeightMap, 0, this.heightMap, 0, this.heightMap.length);
        this.heightMapMinimum = com.google.common.primitives.Ints.min(this.heightMap); // Forge: fix MC-117412
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
        return new BlockPos(x, this.precipitationHeightMap[k], z);
    }
}
