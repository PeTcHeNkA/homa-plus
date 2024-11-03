package plus.fserver.world.light;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.*;
import net.minecraft.block.state.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.utls.collections.PooledLongQueue;
import java.util.concurrent.locks.ReentrantLock;


public class LightingEngine implements ILightingEngine {
    public static final Logger LOGGER = LogManager.getLogger("PhosphorLE");
    private final World world;
    private final PooledLongQueue[] queuedLightUpdates;
    private final PooledLongQueue[] queuedDarkenings;
    private final PooledLongQueue[] queuedBrightenings;
    private final PooledLongQueue initialBrightenings;
    private final PooledLongQueue initialDarkenings;
    private boolean updating;
    private static final long[] neighborShifts;
    private final BlockPos.MutableBlockPos curPos;
    private Chunk curChunk;
    private long curChunkIdentifier;
    private long curData;
    private boolean isNeighborDataValid;
    private final NeighborInfo[] neighborInfos;
    private final PooledLongQueue.LongQueueIterator queueIt = new PooledLongQueue.LongQueueIterator();
    private final ReentrantLock lock = new ReentrantLock();
    private PooledLongQueue swap;
    private final boolean hasSkyLight;

    public LightingEngine(final World world, boolean skyLight) {
        this.queuedLightUpdates = new PooledLongQueue[EnumSkyBlock.values().length];
        this.queuedDarkenings = new PooledLongQueue[16];
        this.queuedBrightenings = new PooledLongQueue[16];
        this.updating = false;
        this.curPos = new BlockPos.MutableBlockPos();
        this.isNeighborDataValid = false;
        this.neighborInfos = new NeighborInfo[6];
        this.world = world;
        final PooledLongQueue.Pool pool = new PooledLongQueue.Pool();
        this.initialBrightenings = new PooledLongQueue(pool);
        this.initialDarkenings = new PooledLongQueue(pool);
        for (int i = 0; i < EnumSkyBlock.values().length; ++i) {
            this.queuedLightUpdates[i] = new PooledLongQueue(pool);
        }
        for (int i = 0; i < this.queuedDarkenings.length; ++i) {
            this.queuedDarkenings[i] = new PooledLongQueue(pool);
        }
        for (int i = 0; i < this.queuedBrightenings.length; ++i) {
            this.queuedBrightenings[i] = new PooledLongQueue(pool);
        }
        for (int i = 0; i < this.neighborInfos.length; ++i) {
            this.neighborInfos[i] = new NeighborInfo();
        }
        swap = new PooledLongQueue(pool);
        hasSkyLight = skyLight;
    }


    @Override
    public void scheduleLightUpdate(final EnumSkyBlock lightType, final BlockPos pos) {
        this.scheduleLightUpdate(lightType, encodeWorldCoord(pos));
    }


    @Override
    public void scheduleLightUpdate(final EnumSkyBlock lightType, final int x, final int y, final int z) {
        this.scheduleLightUpdate(lightType, encodeWorldCoord(x, y, z));
    }


    private void scheduleLightUpdate(final EnumSkyBlock lightType, final long pos) {
        final PooledLongQueue queue = this.queuedLightUpdates[lightType.ordinal()];
        queue.add(pos);
        if (queue.size() >= 4194304) {
            this.processLightUpdatesForType(lightType);
        }
    }


    @Override
    public void processLightUpdates() {
        this.processLightUpdatesForType(EnumSkyBlock.SKY);
        this.processLightUpdatesForType(EnumSkyBlock.BLOCK);
    }


    @Override
    public void awaitUnlock() {
        lock.lock();
        lock.unlock();
    }


    @Override
    public void processLightUpdatesForType(final EnumSkyBlock lightType) {
        if (this.updating || this.world.isRemote && !this.isCallingFromMainThread()) {
            return;
        }
        PooledLongQueue[] updates;
        final PooledLongQueue queue = (updates = this.queuedLightUpdates)[lightType.ordinal()];
        if (queue.isEmpty()) return;
        ReentrantLock lock;
        if((lock = this.lock).tryLock()) {
            updates[lightType.ordinal()] = swap;
            try {
                queue.awaitUnlock();
                this.processLightUpdatesForTypeInner(lightType, queue);
            } catch (Exception e) {
                onError(e);
            } finally {
                swap = queue;
                lock.unlock();
            }
        }
    }


    private void onError(Exception e){
        if(!world.isRemote){
            world.getMinecraftServer().ticker().onError((WorldServer) world, e);
        } else {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void processSync() {
        this.processSync(EnumSkyBlock.SKY);
        this.processSync(EnumSkyBlock.BLOCK);
    }


    @Override
    public void processSync(final EnumSkyBlock lightType){
        if (this.updating || this.world.isRemote && !this.isCallingFromMainThread()) {
            return;
        }
        PooledLongQueue[] updates;
        final PooledLongQueue queue = (updates = this.queuedLightUpdates)[lightType.ordinal()];
        if (queue.isEmpty()) return;
        ReentrantLock lock;
        (lock = this.lock).lock();
        updates[lightType.ordinal()] = swap;
        try {
            this.processLightUpdatesForTypeInner(lightType, queue);
        } catch (Exception e) {
            onError(e);
        } finally {
            swap = queue;
            lock.unlock();
        }
    }


    @SideOnly(Side.CLIENT)
    private boolean isCallingFromMainThread() {
        return Minecraft.getMinecraft().isCallingFromMinecraftThread();
    }


    private void initIter(PooledLongQueue queue){
        this.queueIt.init(queue);
    }


    private void processLightUpdatesForTypeInner(final EnumSkyBlock lightType, final PooledLongQueue queue) {
        this.curChunkIdentifier = -1L;
        initIter(queue);

        BlockPos.MutableBlockPos curPos = this.curPos;

        PooledLongQueue initialBrightenings = this.initialBrightenings;
        PooledLongQueue initialDarkenings = this.initialDarkenings;
        while (this.nextItem()) {
            if (this.curChunk == null) continue;
            final int oldLight = this.getCursorCachedLight(lightType),
                    newLight = this.calculateNewLightFromCursor(curPos, lightType);
            if (oldLight < newLight)
                initialBrightenings.add((long)newLight << 60 | this.curData);
            else {
                if (oldLight <= newLight) continue;
                initialDarkenings.add(this.curData);
            }
        }
        initIter(initialBrightenings);

        PooledLongQueue[] queuedDarkenings = this.queuedDarkenings;
        PooledLongQueue[] queuedBrightenings = this.queuedBrightenings;

        while (this.nextItem()) {
            final int newLight2 = (int)(this.curData >> 60 & 0xFL);
            if (newLight2 > this.getCursorCachedLight(lightType)) {
                enqueueBrightening(queuedBrightenings, curPos, this.curData & 0xFFFFFFFFFFFFFFFL, newLight2, this.curChunk, lightType);
            }
        }

        initIter(initialDarkenings);
        while (this.nextItem()) {
            final int oldLight = this.getCursorCachedLight(lightType);
            if (oldLight != 0) {
                enqueueDarkening(queuedDarkenings, curPos, this.curData, oldLight, this.curChunk, lightType);
            }
        }

        World world = this.world;
        for (int curLight = 15; curLight >= 0; --curLight) {
            initIter(queuedDarkenings[curLight]);
            while (this.nextItem()) {
                if (this.getCursorCachedLight(lightType) >= curLight) continue;
                final IBlockState state = posToState(curPos, this.curChunk);
                final int luminosity = this.getCursorLuminosity(state, lightType);
                int opacity;
                if (luminosity >= 14) {
                    opacity = 1;
                } else {
                    opacity = getPosOpacity(world, curPos, state);
                }
                if (this.calculateNewLightFromCursor(luminosity, opacity, lightType) < curLight) {
                    int newLight3 = luminosity;
                    this.fetchNeighborDataFromCursor(lightType);
                    for (final NeighborInfo info : this.neighborInfos) {
                        final Chunk nChunk = info.chunk;
                        if (nChunk != null) {
                            final int nLight = info.light;
                            if (nLight > 0) { //fixed `-1` value
                                //final BlockPos.MutableBlockPos nPos = info.pos;
                                if (curLight - getPosOpacity(world, info, info.getState()) >= nLight) {
                                    enqueueDarkening(queuedDarkenings, info, info.key, nLight, nChunk, lightType);
                                }
                                else {
                                    newLight3 = Math.max(newLight3, nLight - opacity);
                                }
                            }
                        }
                    }
                    this.enqueueBrighteningFromCursor(newLight3, lightType);
                }
                else {
                    this.enqueueBrighteningFromCursor(curLight, lightType);
                }
            }
            initIter(queuedBrightenings[curLight]);
            while (this.nextItem()) {
                final int oldLight2 = this.getCursorCachedLight(lightType);
                if (oldLight2 == curLight) {
                    world.notifyLightSet(curPos);
                    if (curLight <= 1) {
                        continue;
                    }
                    this.spreadLightFromCursor(curLight, lightType);
                }
            }
        }
        this.updating = false;
    }


    private void fetchNeighborDataFromCursor(final EnumSkyBlock lightType) {
        if (this.isNeighborDataValid) return;

        this.isNeighborDataValid = true;
        NeighborInfo[] neighborInfos = this.neighborInfos;
        for (int i = 0; i < neighborInfos.length; ++i) {
            final NeighborInfo info = neighborInfos[i];
            final long key = this.curData + LightingEngine.neighborShifts[i];
            info.key = key;
            if ((key & 0x1000000000000000L) != 0x0L) {
                info.chunk = null;
                info.section = null;
            } else {
                decodeWorldCoord(info, key);

                Chunk nChunk;
                if ((key & 0xFFFFFC3FFFFF0L) == this.curChunkIdentifier)
                    info.chunk = nChunk = this.curChunk;
                else
                    info.chunk = nChunk = this.getChunk(info);
                if (nChunk != null) {
                    final ExtendedBlockStorage nSection = nChunk.getBlockStorageArray()[info.getY() >> 4];
                    info.light = getCachedLightFor(nChunk, nSection, info, lightType);
                    info.section = nSection;
                }
            }
        }
    }


    private int getCachedLightFor(final Chunk chunk, final ExtendedBlockStorage storage, final BlockPos pos, final EnumSkyBlock type) {
        if (storage == null) {
            if (type == EnumSkyBlock.SKY && chunk.canSeeSky(pos))
                return type.defaultLightValue;
            return 0;
        } else if (type == EnumSkyBlock.SKY) {
            if (!hasSkyLight) return 0;
            return storage.getSkyLight(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
        } else {
            if (type == EnumSkyBlock.BLOCK)
                return storage.getBlockLight(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
            return type.defaultLightValue;
        }
    }


    private int getCursorCachedLight(final EnumSkyBlock type) {
        final int y;
        Chunk chunk;
        BlockPos.MutableBlockPos pos;
        final ExtendedBlockStorage storage = (chunk = this.curChunk).getBlockStorageArray()[(y = (pos = this.curPos).getY()) >> 4];
        if (storage == null) {
            if (chunk.canSeeSky(pos)) return type.defaultLightValue;
            return 0;
        } else if (type == EnumSkyBlock.SKY) {
            if (!hasSkyLight) return 0;
            return storage.getSkyLight(pos.getX() & 0xF, y & 0xF, pos.getZ() & 0xF);
        } else {
            if (type == EnumSkyBlock.BLOCK) {
                return storage.getBlockLight(pos.getX() & 0xF, y & 0xF, pos.getZ() & 0xF);
            }
            return type.defaultLightValue;
        }
    }


    private int calculateNewLightFromCursor(final BlockPos.MutableBlockPos pos, final EnumSkyBlock lightType) {
        final Chunk chunk;
        final IBlockState state = posToState(pos, chunk = this.curChunk);
        final int luminosity;
        return this.calculateNewLightFromCursor(
                luminosity = getCursorLuminosity(chunk, pos, state, lightType),
                (luminosity >= 14)?1: getPosOpacity(this.world, pos, state),
                lightType
        );
    }


    private static int getCursorLuminosity(final Chunk curChunk, final BlockPos.MutableBlockPos pos,
                                           final IBlockState state, final EnumSkyBlock lightType) {
        if (lightType != EnumSkyBlock.SKY)
            return MathHelper.clamp(state.getLightValue(curChunk.getWorld(), pos), 0, 15);
        if (curChunk.canSeeSky(pos))
            return EnumSkyBlock.SKY.defaultLightValue;
        return 0;
    }


    private int calculateNewLightFromCursor(final int luminosity, final int opacity, final EnumSkyBlock lightType) {
        if (luminosity >= 15 - opacity) return luminosity;

        int newLight = luminosity;
        this.fetchNeighborDataFromCursor(lightType);
        for (final NeighborInfo info : this.neighborInfos) {
            if (info.chunk != null) {
                final int nLight = info.light;
                newLight = Math.max(nLight - opacity, newLight);
            }
        }
        return newLight;
    }


    private void spreadLightFromCursor(final int curLight, final EnumSkyBlock lightType) {
        this.fetchNeighborDataFromCursor(lightType);
        for (final NeighborInfo info : this.neighborInfos) {
            final Chunk nChunk = info.chunk;
            if (nChunk != null) {
                final int newLight = curLight - getPosOpacity(this.world, info, info.getState());
                if (newLight > info.light)
                    enqueueBrightening(this.queuedBrightenings, info, info.key, newLight, nChunk, lightType);
            }
        }
    }


    private void enqueueBrighteningFromCursor(final int newLight, final EnumSkyBlock lightType) {
        enqueueBrightening(this.queuedBrightenings, this.curPos, this.curData, newLight, this.curChunk, lightType);
    }


    private static void enqueueBrightening(final PooledLongQueue[] queuedBrightenings, final BlockPos pos, final long longPos, final int newLight, final Chunk chunk, final EnumSkyBlock lightType) {
        queuedBrightenings[newLight].add(longPos);
        chunk.setLightFor(lightType, pos, newLight);
    }


    private static void enqueueDarkening(final PooledLongQueue[] queuedDarkenings, final BlockPos pos, final long longPos, final int oldLight, final Chunk chunk, final EnumSkyBlock lightType) {
        queuedDarkenings[oldLight].add(longPos);
        chunk.setLightFor(lightType, pos, 0);
    }


    private static void decodeWorldCoord(final BlockPos.MutableBlockPos pos, final long longPos) {
        pos.setPosB(
                (int)(longPos >> 26 & 0x3FFFFFFL) - 33554432,
                    (int)(longPos >> 52 & 0xFFL),
                (int)(longPos & 0x3FFFFFFL) - 33554432
        );
    }


    private static long encodeWorldCoord(final int x, final int y, final int z) {
        return (long) y << 52 | x + 33554432L << 26 | z + 33554432L;
    }


    private static long encodeWorldCoord(final BlockPos pos) {
        return (long) pos.getY() << 52 | pos.getX() + 33554432L << 26 | pos.getZ() + 33554432L;
    }


    private boolean nextItem() {
        PooledLongQueue.LongQueueIterator queueIt;
        if (!(queueIt = this.queueIt).hasNext()) {
            queueIt.finish();
            return false;
        }
        long curData = this.curData = queueIt.next();
        BlockPos.MutableBlockPos curPos;
        this.isNeighborDataValid = false;
        decodeWorldCoord(curPos = this.curPos, curData);
        final long chunkIdentifier = curData & 0xFFFFFC3FFFFF0L;
        if (this.curChunkIdentifier != chunkIdentifier) {
            this.curChunk = this.getChunk(curPos);
            this.curChunkIdentifier = chunkIdentifier;
        }
        return true;
    }
//
//
//    private int getCursorCachedLight(final EnumSkyBlock lightType) {
//        return this.curChunk.getCachedLightFor(lightType, this.curPos);
//    }


    private int getCursorLuminosity(final IBlockState state, final EnumSkyBlock lightType) {
        if (lightType != EnumSkyBlock.SKY)
            return MathHelper.clamp(state.getLightValue(world, curPos), 0, 15);
        if (this.curChunk.canSeeSky(this.curPos))
            return EnumSkyBlock.SKY.defaultLightValue;
        return 0;
    }


    private static int getPosOpacity(final IBlockAccess access, final BlockPos pos, final IBlockState state) {
        int opacity = state.getLightOpacity(access, pos);
        return opacity < 2 ? 1 : opacity > 15 ? 15 : opacity;
    }


    private Chunk getChunk(final BlockPos pos) {
        return this.world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }


    private static IBlockState posToState(final BlockPos pos, final Chunk chunk) {
        return posToState(pos, chunk.getBlockStorageArray()[pos.getY() >> 4]);
    }


    private static IBlockState posToState(final BlockPos pos, final ExtendedBlockStorage section) {
        if (section != Chunk.NULL_BLOCK_STORAGE)try {
            return section.get(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
        } catch (IndexOutOfBoundsException ignored){}
        return BlockIDIndexes.AIR_STATE;
    }


    static {
        neighborShifts = new long[6];
        for (int i = 0; i < 6; ++i) {
            final Vec3i offset = EnumFacing.VALUES[i].getDirectionVec();
            LightingEngine.neighborShifts[i] = ((long)offset.getY() << 52 | (long)offset.getX() << 26 | (long) offset.getZ());
        }
    }


    private static final class NeighborInfo extends BlockPos.MutableBlockPos{
        Chunk chunk;
        ExtendedBlockStorage section;
        int light;
        long key;
        
        NeighborInfo() {}

        IBlockState getState() {
            ExtendedBlockStorage section = this.section;
            if (section != Chunk.NULL_BLOCK_STORAGE)try {
                return section.get(this.getX() & 0xF, this.getY() & 0xF, this.getZ() & 0xF);
            } catch (IndexOutOfBoundsException ignored){}
            return BlockIDIndexes.AIR_STATE;
        }
    }
}