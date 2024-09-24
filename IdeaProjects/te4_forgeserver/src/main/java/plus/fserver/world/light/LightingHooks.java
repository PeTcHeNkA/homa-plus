package plus.fserver.world.light;

import net.minecraft.util.*;
import net.minecraft.world.chunk.*;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import plus.fserver.world.light.api.IChunkLightingData;


public class LightingHooks {
    private static final EnumSkyBlock[] ENUM_SKY_BLOCK_VALUES;
    private static final EnumFacing.AxisDirection[] ENUM_AXIS_DIRECTION_VALUES;


    public static void relightSkylightColumn(final World world, final Chunk chunk, final int x, final int z, final int height1, final int height2) {
        final int yMin = Math.min(height1, height2);
        final int yMax = Math.max(height1, height2) - 1;
        final ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        final int xBase = (chunk.x << 4) + x;
        final int zBase = (chunk.z << 4) + z;
        scheduleRelightChecksForColumn(world, EnumSkyBlock.SKY, xBase, zBase, yMin, yMax);
        if (sections[yMin >> 4] == Chunk.NULL_BLOCK_STORAGE && yMin > 0) {
            world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(xBase, yMin - 1, zBase));
        }
        short emptySections = 0;
        for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
            if (sections[sec] == Chunk.NULL_BLOCK_STORAGE) {
                emptySections |= (short)(1 << sec);
            }
        }
        if (emptySections != 0) {
            for (final EnumFacing dir : EnumFacing.HORIZONTALS) {
                final int xOffset = dir.getXOffset();
                final int zOffset = dir.getZOffset();
                final boolean neighborColumnExists = ((x + xOffset | z + zOffset) & 0x10) == 0x0 || world.getChunkProvider().getLoadedChunk(chunk.x + xOffset, chunk.z + zOffset) != null;
                if (neighborColumnExists) {
                    for (int sec2 = yMax >> 4; sec2 >= yMin >> 4; --sec2) {
                        if ((emptySections & 1 << sec2) != 0x0) {
                            scheduleRelightChecksForColumn(world, EnumSkyBlock.SKY, xBase + xOffset, zBase + zOffset, sec2 << 4, (sec2 << 4) + 15);
                        }
                    }
                }
                else {
                    flagChunkBoundaryForUpdate(chunk, emptySections, EnumSkyBlock.SKY, dir, getAxisDirection(dir, x, z), EnumBoundaryFacing.OUT);
                }
            }
        }
    }


    public static void scheduleRelightChecksForArea(final World world, final EnumSkyBlock lightType, final int xMin, final int yMin, final int zMin, final int xMax, final int yMax, final int zMax) {
        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                scheduleRelightChecksForColumn(world, lightType, x, z, yMin, yMax);
            }
        }
    }


    private static void scheduleRelightChecksForColumn(final World world, final EnumSkyBlock lightType, final int x, final int z, final int yMin, final int yMax) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = yMin; y <= yMax; ++y) {
            world.checkLightFor(lightType, pos.setPos(x, y, z));
        }
    }


    public static void flagChunkBoundaryForUpdate(final Chunk chunk, final short sectionMask, final EnumSkyBlock lightType, final EnumFacing dir, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        initNeighborLightChecks(chunk);
        final short[] neighborLightChecks = ((IChunkLightingData)chunk).getNeighborLightChecks();
        final int flagIndex = getFlagIndex(lightType, dir, axisDirection, boundaryFacing);
        neighborLightChecks[flagIndex] |= sectionMask;
        chunk.markDirty();
    }


    public static int getFlagIndex(final EnumSkyBlock lightType, final int xOffset, final int zOffset, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        return ((lightType == EnumSkyBlock.BLOCK) ? 0 : 16) | xOffset + 1 << 2 | zOffset + 1 << 1 | axisDirection.getOffset() + 1 | boundaryFacing.ordinal();
    }


    public static int getFlagIndex(final EnumSkyBlock lightType, final EnumFacing dir, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        return getFlagIndex(lightType, dir.getXOffset(), dir.getZOffset(), axisDirection, boundaryFacing);
    }


    private static EnumFacing.AxisDirection getAxisDirection(final EnumFacing dir, final int x, final int z) {
        return ((((dir.getAxis() == EnumFacing.Axis.X) ? z : x) & 0xF) < 8) ? EnumFacing.AxisDirection.NEGATIVE : EnumFacing.AxisDirection.POSITIVE;
    }

    
    public static void scheduleRelightChecksForChunkBoundaries(final World world, final Chunk chunk) {
        for (final EnumFacing dir : EnumFacing.HORIZONTALS) {
            final int xOffset = dir.getXOffset();
            final int zOffset = dir.getZOffset();
            final Chunk nChunk = world.getChunkProvider().getLoadedChunk(chunk.x + xOffset, chunk.z + zOffset);
            if (nChunk != null) {
                for (final EnumSkyBlock lightType : LightingHooks.ENUM_SKY_BLOCK_VALUES) {
                    for (final EnumFacing.AxisDirection axisDir : LightingHooks.ENUM_AXIS_DIRECTION_VALUES) {
                        mergeFlags(lightType, chunk, nChunk, dir, axisDir);
                        mergeFlags(lightType, nChunk, chunk, dir.getOpposite(), axisDir);
                        scheduleRelightChecksForBoundary(world, chunk, nChunk, null, lightType, xOffset, zOffset, axisDir);
                        scheduleRelightChecksForBoundary(world, nChunk, chunk, null, lightType, -xOffset, -zOffset, axisDir);
                        scheduleRelightChecksForBoundary(world, nChunk, null, chunk, lightType, (zOffset != 0) ? axisDir.getOffset() : 0, (xOffset != 0) ? axisDir.getOffset() : 0, (dir.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) ? EnumFacing.AxisDirection.NEGATIVE : EnumFacing.AxisDirection.POSITIVE);
                    }
                }
            }
        }
    }


    private static void mergeFlags(final EnumSkyBlock lightType, final Chunk inChunk, final Chunk outChunk, final EnumFacing dir, final EnumFacing.AxisDirection axisDir) {
        final IChunkLightingData outChunkLightingData = (IChunkLightingData)outChunk;
        if (outChunkLightingData.getNeighborLightChecks() == null) {
            return;
        }
        final IChunkLightingData inChunkLightingData = (IChunkLightingData)inChunk;
        initNeighborLightChecks(inChunk);
        final int inIndex = getFlagIndex(lightType, dir, axisDir, EnumBoundaryFacing.IN);
        final int outIndex = getFlagIndex(lightType, dir.getOpposite(), axisDir, EnumBoundaryFacing.OUT);
        final short[] neighborLightChecks = inChunkLightingData.getNeighborLightChecks();
        neighborLightChecks[inIndex] |= outChunkLightingData.getNeighborLightChecks()[outIndex];
    }

    
    private static void scheduleRelightChecksForBoundary(final World world, final Chunk chunk, Chunk nChunk, Chunk sChunk, final EnumSkyBlock lightType, final int xOffset, final int zOffset, final EnumFacing.AxisDirection axisDir) {
        final IChunkLightingData chunkLightingData = (IChunkLightingData)chunk;
        if (chunkLightingData.getNeighborLightChecks() == null) return;

        final int flagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDir, EnumBoundaryFacing.IN);
        final int flags = chunkLightingData.getNeighborLightChecks()[flagIndex];
        if (flags == 0) {
            return;
        }
        if (nChunk == null) {
            nChunk = world.getChunkProvider().getLoadedChunk(chunk.x + xOffset, chunk.z + zOffset);
            if (nChunk == null) {
                return;
            }
        }
        if (sChunk == null) {
            sChunk = world.getChunkProvider().getLoadedChunk(chunk.x + ((zOffset != 0) ? axisDir.getOffset() : 0), chunk.z + ((xOffset != 0) ? axisDir.getOffset() : 0));
            if (sChunk == null) {
                return;
            }
        }
        final int reverseIndex = getFlagIndex(lightType, -xOffset, -zOffset, axisDir, EnumBoundaryFacing.OUT);
        chunkLightingData.getNeighborLightChecks()[flagIndex] = 0;
        final IChunkLightingData nChunkLightingData = (IChunkLightingData)nChunk;
        if (nChunkLightingData.getNeighborLightChecks() != null) {
            nChunkLightingData.getNeighborLightChecks()[reverseIndex] = 0;
        }
        chunk.markDirty();
        nChunk.markDirty();
        int xMin = chunk.x << 4,
                zMin = chunk.z << 4;
        if ((xOffset | zOffset) > 0) {
            xMin += 15 * xOffset;
            zMin += 15 * zOffset;
        }
        if (axisDir == EnumFacing.AxisDirection.POSITIVE) {
            xMin += 8 * (zOffset & 0x1);
            zMin += 8 * (xOffset & 0x1);
        }
        final int xMax = xMin + 7 * (zOffset & 0x1),
                zMax = zMin + 7 * (xOffset & 0x1);
        for (int y = 0; y < 16; ++y) {
            if ((flags & 1 << y) != 0x0) {
                scheduleRelightChecksForArea(world, lightType, xMin, y << 4, zMin, xMax, (y << 4) + 15, zMax);
            }
        }
    }


    public static void initNeighborLightChecks(final Chunk chunk) {
        final IChunkLightingData lightingData = (IChunkLightingData)chunk;
        if (lightingData.getNeighborLightChecks() == null) {
            lightingData.setNeighborLightChecks(new short[32]);
        }
    }
//
//
//    public static void writeNeighborLightChecksToNBT(final Chunk chunk, final NBTTagCompound nbt) {
//        final short[] neighborLightChecks = ((IChunkLightingData)chunk).getNeighborLightChecks();
//        if (neighborLightChecks == null) {
//            return;
//        }
//        boolean empty = true;
//        final NBTTagList list = new NBTTagList();
//        for (final short flags : neighborLightChecks) {
//            list.appendTag(new NBTTagShort(flags));
//            if (flags != 0) {
//                empty = false;
//            }
//        }
//        if (!empty) nbt.setTag("NeighborLightChecks", (NBTBase)list);
//    }


    public static void readNeighborLightChecksFromNBT(final Chunk chunk, final NBTTagCompound nbt) {
        if (nbt.hasKey("NeighborLightChecks", 9)) {
            final NBTTagList list = nbt.getTagList("NeighborLightChecks", 2);
            if (list.tagCount() == 32) {
                initNeighborLightChecks(chunk);
                final short[] neighborLightChecks = ((IChunkLightingData)chunk).getNeighborLightChecks();
                for (int i = 0; i < 32; ++i) {
                    neighborLightChecks[i] = ((NBTTagShort)list.get(i)).getShort();
                }
            } else {
                LightingEngine.LOGGER.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})", (Object)"NeighborLightChecks", (Object)chunk.x, (Object)chunk.z);
            }
        }
    }


    public static void initChunkLighting(final Chunk chunk, final World world) {
        final int xBase = chunk.x << 4, zBase = chunk.z << 4;

        if (world.isAreaLoaded(xBase-16, 0, zBase+16, xBase+31, 255, zBase+31, false)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(xBase, 0, zBase);

            final ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
            for (int j = 0; j < storages.length; ++j) {
                final ExtendedBlockStorage storage = storages[j];
                if (storage != Chunk.NULL_BLOCK_STORAGE) {
                    final int yBase = j * 16;
                    for (int y = 0; y < 16; ++y)
                        for (int z = 0; z < 16; ++z)
                            for (int x = 0; x < 16; ++x)
                                if (storage.get(x, y, z).getLightValue(world, pos) > 0)
                                    world.checkLightFor(EnumSkyBlock.BLOCK, pos.setPos(xBase + x, yBase + y, zBase + z));
                }
            }
            if (world.provider.hasSkyLight()) {
                chunk.setSkylightUpdated();
            }
            ((IChunkLightingData)chunk).setLightInitialized(true);

            for (final ExtendedBlockStorage storage : storages) {
                if (storage != Chunk.NULL_BLOCK_STORAGE) storage.tryFreeLight();
            }
        }
    }


    public static void checkChunkLighting(final Chunk chunk, final World world) {
        if (!((IChunkLightingData)chunk).isLightInitialized()) {
            initChunkLighting(chunk, world);
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (x != 0 || z != 0) {
                    final Chunk nChunk = world.getChunkProvider().getLoadedChunk(chunk.x + x, chunk.z + z);
                    if (nChunk == null || !((IChunkLightingData)nChunk).isLightInitialized()) {
                        return;
                    }
                }
            }
        }
        chunk.setLightPopulated(true);
    }


    public static void initSkylightForSection(final World world, final Chunk chunk, final ExtendedBlockStorage section) {
        if (world.provider.hasSkyLight()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (chunk.getHeightValue(x, z) <= section.getYLocation()) {
                        for (int y = 0; y < 16; ++y) {
                            section.setSkyLight(x, y, z, EnumSkyBlock.SKY.defaultLightValue);
                        }
                    }
                }
            }
        }
    }
    
    static {
        ENUM_SKY_BLOCK_VALUES = EnumSkyBlock.values();
        ENUM_AXIS_DIRECTION_VALUES = EnumFacing.AxisDirection.values();
    }

    public enum EnumBoundaryFacing {
        IN,
        OUT;
    }
}