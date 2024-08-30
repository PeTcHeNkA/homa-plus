package plus.fserver.mixins.light;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.*;
import net.minecraft.util.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.util.math.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.*;
import plus.fserver.world.light.WorldChunkSlice;
import plus.fserver.world.light.LightingHooks;
import plus.fserver.world.light.api.IChunkLighting;
import plus.fserver.world.light.api.IChunkLightingData;
import plus.fserver.world.light.api.ILightingEngineProvider;
import java.util.Map;
import static net.minecraft.world.chunk.Chunk.HORIZONTAL;


@Mixin(Chunk.class)
public abstract class MixinChunk implements IChunkLighting, IChunkLightingData, ILightingEngineProvider {
    @Shadow protected abstract int getBlockLightOpacity(final int p0, final int p1, final int p2);
    @Shadow public abstract boolean canSeeSky(final BlockPos p0);
    @Shadow public abstract int getHeightValue(final int p0, final int p1);
    @Shadow @Final private World world;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final private int[] heightMap;
    @Shadow private int heightMapMinimum;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isGapLightingUpdated;
    @Shadow @Final private short[] updateSkylights;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow private boolean dirty;
    @Shadow private boolean loaded;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;

    private short[] neighborLightChecks;
    private boolean isLightInitialized;


    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    public int getLightSubtracted(BlockPos pos, int amount) {
        this.getLightingEngine().processLightUpdates();

        int i = pos.getX() & 15, j = pos.getY(), k = pos.getZ() & 15;
        ExtendedBlockStorage storage = this.storageArrays[j >> 4];
        if (storage == null) {
            return this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        } else {
            int l = !this.world.provider.hasSkyLight() ? 0 : storage.getSkyLight(i, j & 15, k) - amount;
            int i1 = storage.getBlockLight(i, j & 15, k);
            if (i1 > l) l = i1;
            return l;
        }
    }


    /**
     * @author Phosphor + Te4hno
     * @reason faster light
     */
    @Overwrite
    public void onLoad() {
        this.loaded = true;
        this.world.addTileEntities(this.tileEntities.values());
        for (ClassInheritanceMultiMap<Entity> entities : this.entityLists)
            this.world.loadEntities(ImmutableList.copyOf(entities));
        MinecraftForge.EVENT_BUS.postF(new ChunkEvent.Load((Chunk)(Object)this));
        LightingHooks.scheduleRelightChecksForChunkBoundaries(this.world, (Chunk)(Object)this);
    }


    @Redirect(method = { "setLightFor" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"), expect = 0)
    private void setLightForRedirectGenerateSkylightMap(final Chunk chunk, final EnumSkyBlock type, final BlockPos pos, int value) {
        LightingHooks.initSkylightForSection(this.world,  (Chunk)(Object)this, this.storageArrays[pos.getY() >> 4]);
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    private void relightBlock(final int x, final int y, final int z) {
        int j;
        final int i = j = (this.heightMap[z << 4 | x] & 0xFF);
        if (y > i) {
            j = y;
        }
        while (j > 0 && this.getBlockLightOpacity(x, j - 1, z) == 0) {
            --j;
        }
        if (j != i) {
            this.heightMap[z << 4 | x] = j;
            if (this.world.provider.hasSkyLight()) {
                LightingHooks.relightSkylightColumn(this.world, (Chunk)(Object)this, x, z, i, j);
            }
            final int l1 = this.heightMap[z << 4 | x];
            if (l1 < this.heightMapMinimum) {
                this.heightMapMinimum = l1;
            }
        }
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public int getLightFor(final EnumSkyBlock type, final BlockPos pos) {
        this.getLightingEngine().processLightUpdatesForType(type);

        return this.getCachedLightFor(type, pos);
    }


//    /**
//     * @author Phosphor
//     * @reason faster light
//     */
//    @Overwrite
//    public void checkLight() {
//        this.isTerrainPopulated = true;
//        LightingHooks.checkChunkLighting((Chunk)(Object)this, this.world);
//    }


    /**
     * @author Phosphor
     * @reason faster light
     */
//    @Overwrite
//    private void recheckGaps(final boolean onlyOne) {
//        final WorldChunkSlice slice = new WorldChunkSlice(this.world, this.x, this.z);
//        if (this.world.isAreaLoaded(new BlockPos(this.x * 16 + 8, 0, this.z * 16 + 8), 16)) {
//            for (int x = 0; x < 16; ++x) {
//                for (int z = 0; z < 16; ++z) {
//                    if (this.recheckGapsForColumn(slice, x, z) && onlyOne) {
//                        this.world.profiler.endSection();
//                        return;
//                    }
//                }
//            }
//            this.isGapLightingUpdated = false;
//        }
//    }
//
//
//    private boolean recheckGapsForColumn(final WorldChunkSlice slice, final int x, final int z) {
//        final int mask = 1<<z;
//        if ((updateSkylights[x] & mask) == mask) {
//            updateSkylights[x] &= ~mask;
//            final int x2 = (this.x << 4) + x;
//            final int z2 = (this.z << 4) + z;
//            this.recheckGapsSkylightNeighborHeight(
//                    slice, x2, z2,
//                    getHeightValue(x, z),
//                    recheckGapsGetLowestHeight(slice, x2, z2)
//            );
//            return true;
//        }
//        return false;
//    }
//
//
//    private int recheckGapsGetLowestHeight(final WorldChunkSlice slice, final int x, final int z) {
//        int max = Integer.MAX_VALUE;
//        for (final EnumFacing facing : HORIZONTAL) {
//            final Chunk chunk = slice.getChunkFromWorldCoords(
//                    x + facing.getXOffset(),
//                    z + facing.getZOffset()
//            );
//            if (chunk != null) {
//                max = Math.min(max, chunk.getLowestHeight());
//            }
//        }
//        return max;
//    }
//
//
//    private void recheckGapsSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int height, final int max) {
//        this.checkSkylightNeighborHeight(slice, x, z, max);
//        for (final EnumFacing facing : HORIZONTAL)
//            this.checkSkylightNeighborHeight(slice,
//                    x + facing.getXOffset(),
//                    z + facing.getZOffset(), height
//            );
//    }
//
//
//    private void checkSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int maxValue) {
//        if (slice.getChunkFromWorldCoords(x, z) == null) return;
//        final int i = slice.getChunkFromWorldCoords(x, z).getHeightValue(x & 0xF, z & 0xF);
//        if (i > maxValue)
//            this.updateSkylightNeighborHeight(slice, x, z, maxValue, i + 1);
//        else if (i < maxValue)
//            this.updateSkylightNeighborHeight(slice, x, z, i, maxValue + 1);
//    }
//
//
//    private void updateSkylightNeighborHeight(final WorldChunkSlice slice, final int x, final int z, final int startY, final int endY) {
//        if (endY > startY) {
//            if (!slice.isLoaded(x, z, 16)) return;
//            for (int i = startY; i < endY; ++i) {
//                this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, i, z));
//            }
//            this.dirty = true;
//        }
//    }
    

    @Override
    public short[] getNeighborLightChecks() {
        return this.neighborLightChecks;
    }


    @Override
    public void setNeighborLightChecks(final short[] data) {
        this.neighborLightChecks = data;
    }


    @Override
    public boolean isLightInitialized() {
        return this.isLightInitialized;
    }


    @Override
    public void setLightInitialized(final boolean lightInitialized) {
        this.isLightInitialized = lightInitialized;
    }


    @Override
    public int getCachedLightFor(final EnumSkyBlock type, final BlockPos pos) {
        final int i = pos.getX() & 0xF, j = pos.getY(), k = pos.getZ() & 0xF;
        final ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
        if (extendedblockstorage == null) {
            if (this.canSeeSky(pos)) return type.defaultLightValue;
            return 0;
        } else if (type == EnumSkyBlock.SKY) {
            if (!this.world.provider.hasSkyLight()) return 0;
            return extendedblockstorage.getSkyLight(i, j & 0xF, k);
        } else {
            if (type == EnumSkyBlock.BLOCK) {
                return extendedblockstorage.getBlockLight(i, j & 0xF, k);
            }
            return type.defaultLightValue;
        }
    }
}