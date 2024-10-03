package net.minecraft.world;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.*;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaConfig;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.ElcaCV;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkLCache;
import plus.fserver.utls.math.MathUtls;
import plus.fserver.entity.CollisionPredicate;
import plus.fserver.server.elca.AABKey;
import plus.fserver.utls.collections.*;
import plus.fserver.utls.math.BlockPosDelta;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.utls.math.Vec3dMut;
import plus.fserver.world.RedstoneEngine;
import plus.fserver.world.light.LightingEngine;
import plus.fserver.world.light.WorldChunkSlice;
import plus.fserver.world.light.ILightingEngineProvider;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public abstract class World implements IBlockEAccess, net.minecraftforge.common.capabilities.ICapabilityProvider, ILightingEngineProvider {
    /**
     * Used in the getEntitiesWithinAABB functions to expand the search area for entities.
     * Modders should change this variable to a higher value if it is less then the radius
     * of one of there entities.
     */
    public static double MAX_ENTITY_RADIUS = 2.0D;
    static boolean isDebug = false;

    private int seaLevel = 63;
    protected int scheduledUpdatesAreImmediate = 0;
    public final List<Entity> loadedEntityList;
    public final EntityIDHashSplitedList realEntityList;
    protected final List<Entity> unloadedEntityList = Lists.newArrayList();
    public final List<TileEntity> loadedTileEntityList= new ArrayRemSwapListE15<>();
    public final ArrayRemSwapListE15<TileEntity> tickableTileEntities = new ArrayRemSwapListE15<>();
    private final List<TileEntity> addedTileEntityList = Lists.newArrayList();
    private final List<TileEntity> tileEntitiesToBeRemoved = Lists.newArrayList();

    public final List<EntityPlayer> playerEntities = Lists.newArrayList();
    public List<EntityPlayer> curPlayerEntities = Lists.newArrayList();
    public boolean playersModified = false;

    public final List<Entity> weatherEffects = Lists.newArrayList();
    public final IntHashMap<Entity> entitiesById = new IntHashMap<>();
    private final long cloudColour = 16777215L;
    protected int skylightSubtracted;
    protected int updateLCG = (new Random()).nextInt();
    protected final int DIST_HASH_MAGIC = 1013904223;
    public float prevRainingStrength;
    public float rainingStrength;
    public float prevThunderingStrength;
    public float thunderingStrength;
    private int lastLightningBolt;
    public final Random rand = new Random();
    public final WorldProvider provider;
    public PathWorldListener pathListener = new PathWorldListener();
    protected List<IWorldEventListener> eventListeners;
    protected IChunkProvider chunkProvider;
    protected final ISaveHandler saveHandler;
    protected WorldInfo worldInfo;
    protected boolean findingSpawnPoint;
    protected MapStorage mapStorage;
    public VillageCollection villageCollection;
    protected LootTableManager lootTable;
    protected AdvancementManager advancementManager;
    protected FunctionManager functionManager;
    public final Profiler profiler;
    private final Calendar calendar;
    protected Scoreboard worldScoreboard;

    public final boolean isRemote;

    protected boolean spawnHostileMobs;
    protected boolean spawnPeacefulMobs;
    private boolean processingLoadedTiles;
    private final WorldBorder worldBorder;


    public boolean restoringBlockSnapshots = false;
    public boolean captureBlockSnapshots = false;
    public java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot> capturedBlockSnapshots = new java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot>();
    private net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;
    private net.minecraftforge.common.util.WorldCapabilityData capabilityData;
    public boolean loaded = true;

    //redstone boost
    RedstoneEngine.Cache redstoneCache;
    //todo warning ensure there will be no simultaneous use
    public final BlockPos.MutableBlockPos localTempPos  = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos localTempPos2 = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos localTempPos3 = new BlockPos.MutableBlockPos();

    public long tryUpdateNavigationLimit = 20L;
    public final LightingEngine lightingEngine = new LightingEngine(this);
    public final ActionResult<?> pooledAction = new ActionResult<>(null,null);

    final SyncListCache.AABB bbCache;
    final SyncListCache.EList eeCache;
    protected IBlockEAccess effectiveAcces;
    //ONLY SERVER SIDE NOT NULL!
    public final ChunkSelector localSelector;
    public final ChunkLCache   bigLocalCache = new ChunkLCache(this, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public ChunkProviderServer chunkProviderServer;
    //END UNSAFE

    private Thread curThread;

    protected World(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        this(null, saveHandlerIn, info, providerIn, profilerIn, client);
    }

    protected World(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        this.eventListeners = Lists.newArrayList(this.pathListener);
        this.calendar = Calendar.getInstance();
        this.worldScoreboard = new Scoreboard();
        this.spawnHostileMobs = true;
        this.spawnPeacefulMobs = true;
        this.saveHandler = saveHandlerIn;
        this.profiler = profilerIn;
        this.worldInfo = info;
        this.provider = providerIn;

        if(SERVER_COMPILE){
            this.isRemote = false;
        } else {
            this.isRemote = client;
        }

        loadedEntityList = realEntityList = new EntityIDHashSplitedList();

        this.worldBorder = providerIn.createWorldBorder();
        perWorldStorage = new MapStorage((ISaveHandler)null);

        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            eeCache = new SyncListCache.EList(10, 60);
            bbCache = new SyncListCache.AABB(10, 180);
        } else {
            eeCache = null;
            bbCache = null;
        }
        localSelector = initSelector();
        effectiveAcces = this;

        //locals = ElcaServerUtls.createLocals(this);
    }


    public void setCurThread(){
        curThread = Thread.currentThread();
    }


    public RedstoneEngine.Cache getRedstoneCache(){
        RedstoneEngine.Cache cache;
        if((cache = this.redstoneCache) == null)return redstoneCache = new RedstoneEngine.Cache();
        return cache;
    }


    public void freeCurThread(){
        curThread = null;
    }


    public boolean isCurThread(){
        Thread cur;
        if((cur = curThread) == null)return true;
        return cur == Thread.currentThread();
    }


    private int nextEntityID = 0;
    private int maxEntityId  = 0x7FFFFFF0;

    public int nextEntityId(){
        l1: if(nextEntityID == maxEntityId){
            int maxId = 0;
            int minId = Integer.MAX_VALUE;

            EntityIDHashSplitedList realEntityList = this.realEntityList;
            ArrayListE10<Entity> toRecreateId = new ArrayListE10<>();
            for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
                for (int cursor = 0; cursor < list.sizeC(); ++cursor) {
                    Entity entity = list.getC(cursor);
                    int id = entity.getEntityId();
                    if(id > maxId){
                        maxId = id;
                    }
                    if(id < minId){
                        minId = id;
                    }
                    if(entity.trackerEntry == null || entity.trackerEntry.trackingPlayers.isEmpty()){
                        toRecreateId.addF(entity);
                        list.removeSwap(cursor);
                        --cursor;
                    }
                }
            }

            Entity[] arr = realEntityList.toArray(new Entity[0]);
            Arrays.sort(arr, new Comparator<Entity>() {
                @Override
                public int compare(Entity o1, Entity o2) {
                    return Integer.compare(o1.getEntityId(), o2.getEntityId());
                }
            });

            int maxRangeIndex = -1, recreateIndex = -1;
            int maxRange = -1, recEffRange = -2, recMin = toRecreateId.sizeC()+1;

            for (int i = 1; i < arr.length; i++){
                int range = arr[i].getEntityId() - arr[i-1].getEntityId();
                if(range > maxRange){
                    maxRange = range;
                    maxRangeIndex = i-1;
                    continue;
                }
//                if(recEffRange != recMin){
//                    if(range >= recMin){
//                        if(recEffRange == -2){
//                            recEffRange = range;
//                            recreateIndex = i-1;
//                        } else {
//                            if(range < recEffRange){
//                                recEffRange = range;
//                                recreateIndex = i-1;
//                            }
//                        }
//                    }
//                }
            }

            int recStartId, nextId;

            if(maxRangeIndex == -1){
                LogManager.getLogger().error("Cant found free ranges for entity ID. Please restart server");
                realEntityList.addAll(toRecreateId);
                break l1;
            } else {
                int maxBound = arr[maxRangeIndex+1].getEntityId();
                int minBound = arr[maxRangeIndex  ].getEntityId()+1;

                nextId = maxId;

                if(maxBound - minBound < 0x7FFFFFF0 - nextId){
                    nextId += 1;
                    maxId = 0x7FFFFFF0;
                } else {
                    if(minId > maxBound - minBound){
                        nextId = 0;
                        maxId = minId;
                    } else {
                        nextId = minBound;
                        maxId = maxBound;
                    }
                }
            }

            if(recreateIndex == -1){
                if((maxId - nextId) > recMin){
                    recStartId = nextId;
                    nextId += recMin;
                } else {
                    LogManager.getLogger().error("Cant found free ranges for entity ID. Please restart server");
                    realEntityList.addAll(toRecreateId);
                    break l1;
                }
            } else {
                recStartId = arr[recreateIndex].getEntityId()+1;
            }

            int id = recStartId;
            for (Entity entity:toRecreateId){
                entity.world = null;
                entity.setEntityId(id++);
                realEntityList.addF(entity);
                entity.world = this;
            }

            nextEntityID = nextId;
            maxEntityId  = maxId;
        }
        return nextEntityID++;
    }
//    private static class IdRecreator{
//        private final Entity[] all;
//        private int lockIndex, nextIndex, curId, curMax;
//
//        private IdRecreator(Entity[] all, int lock) {
//            this.all = all;
//            this.lockIndex = lock;
//        }
//        private int nextId(){
//            if(curId == curMax){
//                if(++nextIndex == lockIndex){
//                    curId = all[nextIndex].getEntityId()+1;
//                    ++nextIndex;
//                }
//
//                if(nextIndex >= all.length){
//                    //todo
//                } else {
//                    curId = all[nextIndex].getEntityId()+1;
//                    curMax = all[nextIndex].getEntityId();
//                }
//
//            }
//            return curId++;
//        }
//    }
//
//
//    public Object getLocal(int id){
//        return locals[id];
//    }


    protected ChunkSelector initSelector(){
        return null;
    }


    public final IBlockEAccess access(){
        return effectiveAcces;
    }


    public World init() {
        return this;
    }


    @Override
    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }


    public Biome getBiome(final BlockPos pos) {
        return this.provider.getBiomeForCoords(pos);
    }


    public Biome getBiomeForCoordsBody(final BlockPos pos) {
        if (this.isBlockLoaded(pos)) {
            Chunk chunk = this.getChunk(pos);

            try {
                return chunk.getBiome(pos, this.provider.getBiomeProvider());
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting biome");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Coordinates of biome request");
                crashreportcategory.addDetail("Location", new ICrashReportDetail<String>() {
                    public String call() throws Exception {
                        return CrashReportCategory.getCoordinateInfo(pos);
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            return this.provider.getBiomeProvider().getBiome(pos, Biomes.PLAINS);
        }
    }


    public BiomeProvider getBiomeProvider() {
        return this.provider.getBiomeProvider();
    }


    protected abstract IChunkProvider createChunkProvider();


    public void initialize(WorldSettings settings) {
        this.worldInfo.setServerInitialized(true);
    }


    @Nullable
    public MinecraftServer getMinecraftServer() {
        return null;
    }


    @SideOnly(Side.CLIENT)
    public void setInitialSpawnLocation() {
        this.setSpawnPoint(new BlockPos(8, 64, 8));
    }


    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        BlockPos blockpos;
        for (blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ()); !this.isAirBlock(blockpos.up()); blockpos = blockpos.up()) {
            ;
        }
        return this.getBlockState(blockpos);
    }


    public boolean isValid(BlockPos pos) {
        return !this.isOutsideBuildHeight(pos) && pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
    }


    public boolean isOutsideBuildHeight(BlockPos pos) {
        return pos.getY() < 0 || pos.getY() >= 256;
    }


    public boolean isBlockLoaded(BlockPos pos) {
        return this.isBlockLoaded(pos, true);
    }


    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, allowEmpty);
    }


    public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
        return this.isAreaLoaded(center.getX() - radius, center.getY() - radius, center.getZ() - radius, center.getX() + radius, center.getY() + radius, center.getZ() + radius, allowEmpty);
    }


    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return this.isAreaLoaded(from, to, true);
    }


    public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
        return this.isAreaLoaded(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), allowEmpty);
    }


    public boolean isAreaLoaded(StructureBoundingBox box) {
        return this.isAreaLoaded(box, true);
    }


    public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
        return this.isAreaLoaded(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, allowEmpty);
    }


    public boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
        if (yEnd >= 0 && yStart < 256) {
            xStart = xStart >> 4;
            zStart = zStart >> 4;
            xEnd = xEnd >> 4;
            zEnd = zEnd >> 4;

            for (int i = xStart; i <= xEnd; ++i) {
                for (int j = zStart; j <= zEnd; ++j) {
                    if (!this.isChunkLoaded(i, j, allowEmpty)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);


    public Chunk getChunk(BlockPos pos) {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }


    public Chunk getChunk(int chunkX, int chunkZ) {
        return this.chunkProvider.provideChunk(chunkX, chunkZ);
    }


    public boolean isChunkGeneratedAt(int x, int z) {
        return this.isChunkLoaded(x, z, false) ? true : this.chunkProvider.isChunkGeneratedAt(x, z);
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (this.isOutsideBuildHeight(pos)) {
            return false;
        } else if ((!this.isRemote || SERVER_COMPILE) && isDebug) {
            return false;
        } else {
            Chunk chunk = this.getChunk(pos);

            pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks
            net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
            if (this.captureBlockSnapshots && (!this.isRemote || SERVER_COMPILE)) {
                blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(this, pos, flags);
                this.capturedBlockSnapshots.add(blockSnapshot);
            }
            IBlockState oldState = getBlockState(pos);
            int oldLight = oldState.getLightValue(this, pos);
            int oldOpacity = oldState.getLightOpacity(this, pos);

            IBlockState iblockstate = chunk.setBlockState(pos, newState);

            if (iblockstate == null) {
                if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
                return false;
            } else {
                if (newState.getLightOpacity(this, pos) != oldOpacity || newState.getLightValue(this, pos) != oldLight) {
                    this.checkLight(pos);
                }
                // Don't notify clients or update physics while capturing blockstates
                if (blockSnapshot == null)
                    this.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
                return true;
            }
        }
    }


    // Split off from original setBlockState(BlockPos, IBlockState, int)
    // method in order to directly send client and physic updates
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        Block block = newState.getBlock();
        if ((flags & 2) != 0 && ((!this.isRemote || SERVER_COMPILE) || (flags & 4) == 0) && (chunk == null || chunk.isPopulated()))
            this.notifyBlockUpdate(pos, iblockstate, newState, flags);
        if ((!this.isRemote || SERVER_COMPILE) && (flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, iblockstate.getBlock(), true);

            if (newState.hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, block);
            }
        } else if ((!this.isRemote || SERVER_COMPILE) && (flags & 16) == 0)
            this.updateObservingBlocksAt(pos, block);
    }


    public boolean setBlockToAir(BlockPos pos) {
        return this.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
    }


    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.isAir(iblockstate, this, pos)) {
            return false;
        } else {
            this.playEvent(2001, pos, Block.getStateId(iblockstate));

            if (dropBlock) {
                block.dropBlockAsItem(this, pos, iblockstate, 0);
            }
            return this.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
        }
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return this.setBlockState(pos, state, 3);
    }


    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        for (int i = 0; i < this.eventListeners.size(); ++i)
            this.eventListeners.get(i).notifyBlockUpdate(this, pos, oldState, newState, flags);
    }


    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean updateObservers) {
        if (!isDebug)
            this.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
    }


    public void markBlocksDirtyVertical(int x, int z, int y1, int y2) {
        if (y1 > y2) {
            int i = y2;
            y2 = y1;
            y1 = i;
        }
        if (this.provider.hasSkyLight()) {
            final LightingEngine eng = lightingEngine;
            for (int y = y1; y <= y2; ++y)
                eng.scheduleLightUpdate(EnumSkyBlock.SKY, x, y, z);
        }
        this.markBlockRangeForRenderUpdate(x, y1, z, x, y2, z);
    }


    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        this.markBlockRangeForRenderUpdate(rangeMin.getX(), rangeMin.getY(), rangeMin.getZ(), rangeMax.getX(), rangeMax.getY(), rangeMax.getZ());
    }


    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
        }
    }


    //todo not optimal
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
        if(ElcaCV.ENABLE_NEIGHBOR_NOTIFY_EVENT) {
            if (onNeighborNotify(pos, this.getBlockState(pos), java.util.EnumSet.allOf(EnumFacing.class), updateObservers).isCanceled())
                return;
        }

        this.neighborChanged(pos.west(),  blockType, pos);
        this.neighborChanged(pos.east(),  blockType, pos);
        this.neighborChanged(pos.down(),  blockType, pos);
        this.neighborChanged(pos.up(),    blockType, pos);
        this.neighborChanged(pos.north(), blockType, pos);
        this.neighborChanged(pos.south(), blockType, pos);

        if (updateObservers) {
            this.updateObservingBlocksAt(pos, blockType);
        }
    }


    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        if(ElcaCV.ENABLE_NEIGHBOR_NOTIFY_EVENT) {
            java.util.EnumSet<EnumFacing> directions = java.util.EnumSet.allOf(EnumFacing.class);
            directions.remove(skipSide);
            if (onNeighborNotify(pos, this.getBlockState(pos), directions, false).isCanceled())
                return;
        }
        BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
        if (skipSide != EnumFacing.WEST) {
            this.neighborChanged(
                    temp.setPos(pos.getX()-1,pos.getY(),pos.getZ()), blockType, pos
            );
        }

        if (skipSide != EnumFacing.EAST) {
            this.neighborChanged(
                    temp.setPos(pos.getX()+1,pos.getY(),pos.getZ()), blockType, pos
            );
        }

        if (skipSide != EnumFacing.DOWN) {
            this.neighborChanged(
                    temp.setPos(pos.getX(),pos.getY()-1,pos.getZ()), blockType, pos
            );
        }

        if (skipSide != EnumFacing.UP) {
            this.neighborChanged(
                    temp.setPos(pos.getX(),pos.getY()+1,pos.getZ()), blockType, pos
            );
        }

        if (skipSide != EnumFacing.NORTH) {
            this.neighborChanged(
                    temp.setPos(pos.getX(),pos.getY(),pos.getZ()-1), blockType, pos
            );
        }

        if (skipSide != EnumFacing.SOUTH) {
            this.neighborChanged(
                    temp.setPos(pos.getX(),pos.getY(),pos.getZ()+1), blockType, pos
            );
        }
    }


    public void observedNeighborChanged(BlockPos pos, final Block changedBlock, BlockPos changedBlockPos) {
        if (!this.isRemote || SERVER_COMPILE) {
            IBlockState iblockstate = this.getBlockState(pos);
            if (true) {
                try {
                    iblockstate.getBlock().observedNeighborChange(iblockstate, this, pos, changedBlock, changedBlockPos);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
                    crashreportcategory.addDetail("Source block type", new ICrashReportDetail<String>() {
                        public String call() throws Exception {
                            try {
                                return String.format("ID #%d (%s // %s // %s)", Block.getIdFromBlock(changedBlock), changedBlock.getTranslationKey(), changedBlock.getClass().getName(), changedBlock.getRegistryName());
                            } catch (Throwable var2) {
                                return "ID #" + Block.getIdFromBlock(changedBlock);
                            }
                        }
                    });
                    CrashReportCategory.addBlockInfo(crashreportcategory, pos, iblockstate);
                    throw new ReportedException(crashreport);
                }
            }
        }
    }


    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        return false;
    }


    public boolean canSeeSky(BlockPos pos) {
        return this.getChunk(pos).canSeeSky(pos);
    }


    public final boolean canSeeSky(Entity entity){
        return canSeeSky(
                MathHelper.floor(entity.posX),
                MathHelper.floor(entity.posY + entity.getEyeHeight()),
                MathHelper.floor(entity.posZ)
        );
    }


    public final boolean canSeeSky(int x, int y, int z) {
        return this.getChunk(x >> 4, z >> 4).canSeeSky(x, y, z);
    }


    public boolean canBlockSeeSky(BlockPos pos) {
        if (pos.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(pos.getX(), pos.getY(), pos.getZ());
        } else {
            if (!this.canSeeSky(pos.getX(), this.getSeaLevel(), pos.getZ())) {
                return false;
            } else {
                BlockPos blockpos = new BlockPos(pos.getX(), this.getSeaLevel()-1, pos.getZ());

                for (BlockPos blockpos1 = blockpos.down(); blockpos1.getY() > pos.getY();
                     BlockPos.unsafeEdit(blockpos1, blockpos1.getX(), blockpos1.getY() - 1, blockpos1.getZ())) {

                    IBlockState iblockstate = this.getBlockState(blockpos1);

                    if (iblockstate.getBlock().getLightOpacity(iblockstate, this, blockpos) > 0 && !iblockstate.getMaterial().isLiquid()) {
                        return false;
                    }
                }
                return true;
            }
        }
    }


    public int getLight(BlockPos pos) {
        if (pos.getY() < 0) {
            return 0;
        } else {
            if (pos.getY() >= 256) {
                pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }
            return this.getChunk(pos).getLightSubtracted(pos, 0);
        }
    }


    public int getLightFromNeighbors(BlockPos pos) {
        return this.getLight(pos, true);
    }


    public BlockPos getHeight(BlockPos pos) {
        return new BlockPos(pos.getX(), this.getHeight(pos.getX(), pos.getZ()), pos.getZ());
    }


    public int getHeight(int x, int z) {
        int i;
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            if (this.isChunkLoaded(x >> 4, z >> 4, true)) {
                i = this.getChunk(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
            } else {
                i = 0;
            }
        } else {
            i = this.getSeaLevel() + 1;
        }
        return i;
    }


    @Deprecated
    public int getChunksLowestHorizon(int x, int z) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            if (!this.isChunkLoaded(x >> 4, z >> 4, true)) {
                return 0;
            } else {
                Chunk chunk = this.getChunk(x >> 4, z >> 4);
                return chunk.getLowestHeight();
            }
        } else {
            return this.getSeaLevel() + 1;
        }
    }


    @SideOnly(Side.CLIENT)
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        if (!this.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
            return 0;
        } else {
            if (pos.getY() < 0) {
                pos = new BlockPos(pos.getX(), 0, pos.getZ());
            }
            if (!this.isValid(pos)) {
                return type.defaultLightValue;

            } else if (!this.isBlockLoaded(pos)) {
                return type.defaultLightValue;
            }

            else if (this.getBlockState(pos).useNeighborBrightness()) {
                int i1 = this.getLightFor(type, pos.up());
                int i = this.getLightFor(type, pos.east());
                int j = this.getLightFor(type, pos.west());
                int k = this.getLightFor(type, pos.south());
                int l = this.getLightFor(type, pos.north());

                if (i > i1) i1 = i;
                if (j > i1) i1 = j;
                if (k > i1) i1 = k;
                if (l > i1) i1 = l;

                return i1;
            } else {
                return this.getChunk(pos).getLightFor(type, pos);
            }
        }
    }


    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }
        if (!this.isValid(pos)) {
            return type.defaultLightValue;
        }
        else if (!this.isBlockLoaded(pos)) {
            return type.defaultLightValue;
        } else {
            Chunk chunk = this.getChunk(pos);
            return chunk.getLightFor(type, pos);
        }
    }


    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        if (this.isValid(pos)) {
            if (this.isBlockLoaded(pos)) {
                Chunk chunk = this.getChunk(pos);
                chunk.setLightFor(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }


    public final void notifyLightSet(BlockPos pos) {
        List<IWorldEventListener> listeners = this.eventListeners;
        for (int i = 0, s = listeners.size(); i < s; ++i)
            listeners.get(i).notifyLightSet(pos);
    }


    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
        int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
        if (j < lightValue) {
            j = lightValue;
        }
        return i << 20 | j << 4;
    }


    public float getLightBrightness(BlockPos pos) {
        return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(pos)];
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            Chunk chunk = this.getChunk(pos);
            return chunk.getBlockState(pos);
        }
    }


    public boolean isDaytime() {
        return this.provider.isDaytime();
    }


    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return this.rayTraceBlocks(start, end, false, false, false);
    }


    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
        return this.rayTraceBlocks(start, end, stopOnLiquid, false, false);
    }


    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int sX   = MathHelper.floor(vec32.x);
                int sY   = MathHelper.floor(vec32.y);
                int sZ   = MathHelper.floor(vec32.z);
                int xPos = MathHelper.floor(vec31.x);
                int yPos = MathHelper.floor(vec31.y);
                int zPos = MathHelper.floor(vec31.z);

                final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(xPos, yPos, zPos);
                Chunk chunk = getChunk(xPos>>4, zPos>>4);
                IBlockState state = chunk.getBlockState(blockPos);

                if ((!ignoreBlockWithoutBoundingBox || state.isFullBlock() || state.getCollisionBoundingBox(this, blockPos) != Block.NULL_AABB) && state.getBlock().canCollideCheck(state, stopOnLiquid)) {
                    RayTraceResult raytraceresult = state.collisionRayTrace(this, blockPos, vec31, vec32);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                EnumFacing enumfacing;
                RayTraceResult rtxResult2 = null;
                IBlockEAccess selector = isRemote?this:new ChunkSelector(chunk);

                int k1 = 200;

                double num1 = 0, num2 = 0, num3 = 0, num4, num5, num6, num7, num8, num9;
                double vecX = vec31.x, vecY = vec31.y, vecZ = vec31.z;

                while (k1-- >= 0) {
                    if (Double.isNaN(vecX) || Double.isNaN(vecY) || Double.isNaN(vecZ)) return null;
                    if (xPos == sX && yPos == sY && zPos == sZ) {
                        return returnLastUncollidableBlock ? rtxResult2 : null;
                    }

                    num7 = vec32.x - vecX;
                    num8 = vec32.y - vecY;
                    num9 = vec32.z - vecZ;

                    if (sX > xPos) {
                        num1 = xPos + 1d;
                        num4 = (num1 - vecX) / num7;
                    } else if (sX < xPos) {
                        num1 = xPos;
                        num4 = (num1 - vecX) / num7;
                    } else {
                        num4 = 999d;
                    }

                    if (sY > yPos) {
                        num2 = yPos + 1;
                        num5 = (num2 - vecY) / num8;
                    } else if (sY < yPos) {
                        num2 = yPos;
                        num5 = (num2 - vecY) / num8;
                    } else {
                        num5 = 999d;
                    }

                    if (sZ > zPos) {
                        num3 = zPos + 1;
                        num6 = (num3 - vecZ) / num9;
                    } else if (sZ < zPos) {
                        num3 = zPos;
                        num6 = (num3 - vecZ) / num9;
                    } else {
                        num6 = 999d;
                    }

                    if (num4 == -0.0D) num4 = -1.0E-4D;
                    if (num5 == -0.0D) num5 = -1.0E-4D;
                    if (num6 == -0.0D) num6 = -1.0E-4D;

                    if (num4 < num5 && num4 < num6) {
                        enumfacing = sX > xPos ? EnumFacing.WEST : EnumFacing.EAST;
                        vecX = num1; vecY = vecY + num8 * num4; vecZ = vecZ + num9 * num4;
                    } else if (num5 < num6) {
                        enumfacing = sY > yPos ? EnumFacing.DOWN : EnumFacing.UP;
                        vecX = vecX + num7 * num5; vecY = num2; vecZ = vecZ + num9 * num5;
                    } else {
                        enumfacing = sZ > zPos ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vecX = vecX + num7 * num6; vecY = vecY + num8 * num6; vecZ = num3;
                    }

                    state = selector.getBlockState(
                            xPos = MathHelper.floor(vecX) - (enumfacing == EnumFacing.EAST ? 1 : 0),
                            yPos = MathHelper.floor(vecY) - (enumfacing == EnumFacing.UP ? 1 : 0),
                            zPos = MathHelper.floor(vecZ) - (enumfacing == EnumFacing.SOUTH ? 1 : 0)
                    );

                    if (!ignoreBlockWithoutBoundingBox || state != BlockIDIndexes.AIR_STATE && (state.isFullBlock() || state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(this, blockPos) != Block.NULL_AABB)) {
                        blockPos.setPosB(xPos, yPos, zPos);
                        if (state.getBlock().canCollideCheck(state, stopOnLiquid)) {
                            RayTraceResult rtxResult = state.collisionRayTrace(this, blockPos, new Vec3d(vecX, vecY, vecZ), vec32);

                            if (rtxResult != null) {
                                return rtxResult;
                            }
                        } else {
                            rtxResult2 = new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(vecX, vecY, vecZ), enumfacing, blockPos);
                        }
                    }
                }
                return returnLastUncollidableBlock ? rtxResult2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private final BlockPos.MutableBlockPos checkSeePos = new BlockPos.MutableBlockPos();


    public boolean canSee(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid) {
        return canSee(isRemote?this:localSelector, vec31, vec32, stopOnLiquid);
    }


    public boolean canSee(IBlockEAccess selector, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int sX   = MathHelper.floor(vec32.x);
                int sY   = MathHelper.floor(vec32.y);
                int sZ   = MathHelper.floor(vec32.z);
                int xPos = MathHelper.floor(vec31.x);
                int yPos = MathHelper.floor(vec31.y);
                int zPos = MathHelper.floor(vec31.z);

                Chunk chunk = getChunk(xPos>>4, zPos>>4);
                IBlockState state = chunk.getBlockState(xPos, yPos, zPos);

                EnumFacing enumfacing;

                int k1 = 200;

                double num1 = 0, num2 = 0, num3 = 0, num4, num5, num6, num7, num8, num9;
                double vecX = vec31.x, vecY = vec31.y, vecZ = vec31.z;

                BlockPos.MutableBlockPos blockPos = checkSeePos;

                while (k1-- >= 0) {
                    if (Double.isNaN(vecX) || Double.isNaN(vecY) || Double.isNaN(vecZ)) return false;
                    if (xPos == sX && yPos == sY && zPos == sZ) return true;

                    num7 = vec32.x - vecX;
                    num8 = vec32.y - vecY;
                    num9 = vec32.z - vecZ;

                    if (sX > xPos) {
                        num1 = xPos + 1d;
                        num4 = (num1 - vecX) / num7;
                    } else if (sX < xPos) {
                        num1 = xPos;
                        num4 = (num1 - vecX) / num7;
                    } else {
                        num4 = 999d;
                    }

                    if (sY > yPos) {
                        num2 = yPos + 1;
                        num5 = (num2 - vecY) / num8;
                    } else if (sY < yPos) {
                        num2 = yPos;
                        num5 = (num2 - vecY) / num8;
                    } else {
                        num5 = 999d;
                    }

                    if (sZ > zPos) {
                        num3 = zPos + 1;
                        num6 = (num3 - vecZ) / num9;
                    } else if (sZ < zPos) {
                        num3 = zPos;
                        num6 = (num3 - vecZ) / num9;
                    } else {
                        num6 = 999d;
                    }

                    if (num4 == -0.0D) num4 = -1.0E-4D;
                    if (num5 == -0.0D) num5 = -1.0E-4D;
                    if (num6 == -0.0D) num6 = -1.0E-4D;

                    if (num4 < num5 && num4 < num6) {
                        enumfacing = sX > xPos ? EnumFacing.WEST : EnumFacing.EAST;
                        vecX = num1; vecY = vecY + num8 * num4; vecZ = vecZ + num9 * num4;
                    } else if (num5 < num6) {
                        enumfacing = sY > yPos ? EnumFacing.DOWN : EnumFacing.UP;
                        vecX = vecX + num7 * num5; vecY = num2; vecZ = vecZ + num9 * num5;
                    } else {
                        enumfacing = sZ > zPos ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vecX = vecX + num7 * num6; vecY = vecY + num8 * num6; vecZ = num3;
                    }

                    state = selector.getBlockState(
                            xPos = MathHelper.floor(vecX) - (enumfacing == EnumFacing.EAST ? 1 : 0),
                            yPos = MathHelper.floor(vecY) - (enumfacing == EnumFacing.UP ? 1 : 0),
                            zPos = MathHelper.floor(vecZ) - (enumfacing == EnumFacing.SOUTH ? 1 : 0)
                    );

                    if (state != BlockIDIndexes.AIR_STATE && (state.isFullBlock() || state.getMaterial() == Material.PORTAL)) {
                        Block block;
                        if ((block = state.getBlock()).canCollideCheck(state, stopOnLiquid)) {
                            blockPos.setPosB(xPos, yPos, zPos);
                            boolean rtxResult = block.hasCollisionRayTrace(state, this, blockPos, new Vec3d(vecX, vecY, vecZ), vec32);
                            if (rtxResult) return false;
                        }
                    } else {
                        blockPos.setPosB(xPos, yPos, zPos);
                        if(state.getCollisionBoundingBox(this, blockPos) != Block.NULL_AABB){
                            Block block;
                            if ((block = state.getBlock()).canCollideCheck(state, stopOnLiquid)) {
                                boolean rtxResult = block.hasCollisionRayTrace(state, this, blockPos, new Vec3d(vecX, vecY, vecZ), vec32);
                                if (rtxResult) return false;
                            }
                        }
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    @Nullable
    public RayTraceResult rayTraceBlocks(IBlockEAccess selector, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int sX   = MathHelper.floor(vec32.x);
                int sY   = MathHelper.floor(vec32.y);
                int sZ   = MathHelper.floor(vec32.z);
                int xPos = MathHelper.floor(vec31.x);
                int yPos = MathHelper.floor(vec31.y);
                int zPos = MathHelper.floor(vec31.z);

                final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(xPos, yPos, zPos);

                IBlockState state = selector.getBlockState(blockPos);

                if ((!ignoreBlockWithoutBoundingBox || state != BlockIDIndexes.AIR_STATE && (state.getCollisionBoundingBox(this, blockPos) != Block.NULL_AABB) && state.getBlock().canCollideCheck(state, stopOnLiquid))) {
                    RayTraceResult raytraceresult = state.collisionRayTrace(this, blockPos, vec31, vec32);
                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                EnumFacing enumfacing;
                RayTraceResult rtxResult2 = null;

                int k1 = 200;

                double num1 = 0, num2 = 0, num3 = 0, num4, num5, num6, num7, num8, num9;
                double vecX = vec31.x, vecY = vec31.y, vecZ = vec31.z;

                while (k1-- >= 0) {
                    if (Double.isNaN(vecX) || Double.isNaN(vecY) || Double.isNaN(vecZ)) {
                        return null;
                    }
                    if (xPos == sX && yPos == sY && zPos == sZ) {
                        return returnLastUncollidableBlock ? rtxResult2 : null;
                    }

                    num7 = vec32.x - vecX;
                    num8 = vec32.y - vecY;
                    num9 = vec32.z - vecZ;

                    if (sX > xPos) {
                        num1 = xPos + 1d;
                        num4 = (num1 - vecX) / num7;
                    } else if (sX < xPos) {
                        num1 = xPos;
                        num4 = (num1 - vecX) / num7;
                    } else {
                        num4 = 999d;
                    }

                    if (sY > yPos) {
                        num2 = yPos + 1;
                        num5 = (num2 - vecY) / num8;
                    } else if (sY < yPos) {
                        num2 = yPos;
                        num5 = (num2 - vecY) / num8;
                    } else {
                        num5 = 999d;
                    }

                    if (sZ > zPos) {
                        num3 = zPos + 1;
                        num6 = (num3 - vecZ) / num9;
                    } else if (sZ < zPos) {
                        num3 = zPos;
                        num6 = (num3 - vecZ) / num9;
                    } else {
                        num6 = 999d;
                    }

                    if (num4 == -0.0D) num4 = -1.0E-4D;
                    if (num5 == -0.0D) num5 = -1.0E-4D;
                    if (num6 == -0.0D) num6 = -1.0E-4D;

                    if (num4 < num5 && num4 < num6) {
                        enumfacing = sX > xPos ? EnumFacing.WEST : EnumFacing.EAST;
                        vecX = num1; vecY = vecY + num8 * num4; vecZ = vecZ + num9 * num4;
                    } else if (num5 < num6) {
                        enumfacing = sY > yPos ? EnumFacing.DOWN : EnumFacing.UP;
                        vecX = vecX + num7 * num5; vecY = num2; vecZ = vecZ + num9 * num5;
                    } else {
                        enumfacing = sZ > zPos ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vecX = vecX + num7 * num6; vecY = vecY + num8 * num6; vecZ = num3;
                    }

                    state = selector.getBlockState(
                            xPos = MathHelper.floor(vecX) - (enumfacing == EnumFacing.EAST ? 1 : 0),
                            yPos = MathHelper.floor(vecY) - (enumfacing == EnumFacing.UP ? 1 : 0),
                            zPos = MathHelper.floor(vecZ) - (enumfacing == EnumFacing.SOUTH ? 1 : 0)
                    );

                    if (!ignoreBlockWithoutBoundingBox || state != BlockIDIndexes.AIR_STATE && (state.isFullCube() || state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(this, blockPos) != Block.NULL_AABB)) {
                        blockPos.setPosB(xPos, yPos, zPos);
                        if (state.getBlock().canCollideCheck(state, stopOnLiquid)) {
                            RayTraceResult rtxResult = state.collisionRayTrace(this, blockPos, new Vec3d(vecX, vecY, vecZ), vec32);

                            if (rtxResult != null) {
                                return rtxResult;
                            }
                        } else {
                            rtxResult2 = new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(vecX, vecY, vecZ), enumfacing, blockPos);
                        }
                    }
                }
                return returnLastUncollidableBlock ? rtxResult2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }



    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        this.playSound(player, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch);
    }


    //todo use pooled event
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        PlaySoundAtEntityEvent event;
        (event = playSoundEvent).initPlaySound(player, soundIn, category, volume, pitch);
        MinecraftForge.EVENT_BUS.postF(event);
        if (event.isCanceled() || event.getSound() == null) return;
        soundIn = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        pitch = event.getPitch();
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
        }
    }


    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {}


    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).playRecord(soundEventIn, blockPositionIn);
        }
    }


    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }


    public void spawnAlwaysVisibleParticle(int id, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).spawnParticle(id, false, true, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
        }
    }


    @SideOnly(Side.CLIENT)
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange() || ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }


    private void spawnParticle(int particleID, boolean ignoreRange, double xCood, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).spawnParticle(particleID, ignoreRange, xCood, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
        }
    }


    public boolean addWeatherEffect(Entity entityIn) {
        if(MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entityIn, this)))
            return false;
        this.weatherEffects.add(entityIn);
        return true;
    }


    public boolean spawnEntity(Entity entityIn) {
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.isRemote && (entityIn == null || (entityIn instanceof net.minecraft.entity.item.EntityItem && this.restoringBlockSnapshots))) return false;

        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer) {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, false)) {
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                this.playerEntities.add(entityplayer);
                this.playersModified = true;
                this.updateAllPlayersSleepingFlag();
            }

            if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entityIn, this)) && !flag) return false;
            this.getChunk(i, j).addEntity(entityIn);
            this.realEntityList.addF(entityIn);
            this.onEntityAdded(entityIn);
            return true;
        }
    }


    public void onEntityAdded(Entity entityIn) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).onEntityAdded(entityIn);
        }
        entityIn.onAddedToWorld();
    }


    public void onEntityRemoved(Entity entityIn) {
        for (int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).onEntityRemoved(entityIn);
        }
        entityIn.onRemovedFromWorld();
    }


    public void removeEntity(Entity entityIn) {
        if (entityIn.isBeingRidden()) {
            entityIn.removePassengers();
        }
        if (entityIn.isRiding()) {
            entityIn.dismountRidingEntity();
        }
        entityIn.setDead();
        if (entityIn instanceof EntityPlayer) {
            this.playerEntities.remove(entityIn);
            this.playersModified = true;
            this.updateAllPlayersSleepingFlag();
            this.onEntityRemoved(entityIn);
        }
    }


    public void removeEntityDangerously(Entity entityIn) {
        entityIn.setDropItemsWhenDead(false);
        entityIn.setDead();

        if (entityIn instanceof EntityPlayer) {
            this.playerEntities.remove(entityIn);
            this.playersModified = true;
            this.updateAllPlayersSleepingFlag();
        }

        int i = entityIn.chunkCoordX;
        int j = entityIn.chunkCoordZ;

        if (entityIn.addedToChunk && this.isChunkLoaded(i, j, true)) {
            this.getChunk(i, j).removeEntity(entityIn);
        }

        this.realEntityList.removeRefSwap(entityIn);
        this.onEntityRemoved(entityIn);
    }


    public void addEventListener(IWorldEventListener listener) {
        this.eventListeners.add(listener);
    }


    public void removeEventListener(IWorldEventListener listener) {
        this.eventListeners.remove(listener);
    }


    public final boolean isInWorldBorder(AxisAlignedBB bb){
        return bb.maxX > worldBMinx && bb.minX < worldBMaxx && bb.maxZ > worldBMinz && bb.minZ < worldBMaxz;
    }


    public final boolean isInWorldBorder(ChunkPos range) {
        return (double)range.getXEnd() > worldBMinx && (double)range.getXStart() < worldBMaxx &&
                (double)range.getZEnd() > worldBMinz && (double)range.getZStart() < worldBMaxz;
    }


    public final boolean isInWorldBorderChunk(int chunkX, int chunkZ) {
        return  (chunkX<<4)+15 > worldBMinx && (chunkX<<4) < worldBMaxx &&
                (chunkZ<<4)+15 > worldBMinz && (chunkZ<<4) < worldBMaxz;
    }


    public final boolean isInWorldBorder(int blockX, int blockZ) {
        return blockX > worldBMinx && blockX < worldBMaxx && blockZ > worldBMinz && blockZ < worldBMaxz;
    }


    public boolean isInsideWorldBorder(Entity entityToCheck) {
        double d0, d1, d2, d3;
        if (entityToCheck.isOutsideBorder()) {
            d0 = worldBMinx +1;
            d1 = worldBMinz +1;
            d2 = worldBMinx -1;
            d3 = worldBMinx -1;
        } else {
            d0 = worldBMinx -1;
            d1 = worldBMinx -1;
            d2 = worldBMaxx +1;
            d3 = worldBMaxz +1;
        }
        return entityToCheck.posX > d0 && entityToCheck.posX < d2 && entityToCheck.posZ > d1 && entityToCheck.posZ < d3;
    }


    public int calculateSkylightSubtracted(float partialTicks) {
        float f = provider.getSunBrightnessFactor(partialTicks);
        f = 1 - f;
        return (int)(f * 11);
    }


    /**
     * The current sun brightness factor for this dimension.
     * 0.0f means no light at all, and 1.0f means maximum sunlight.
     * Highly recommended for sunlight detection like solar panel.
     *
     * @return The current brightness factor
     * */
    public float getSunBrightnessFactor(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
        f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
        return f1;
    }


    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float partialTicks) {
        return this.provider.getSunBrightness(partialTicks);
    }


    @SideOnly(Side.CLIENT)
    public float getSunBrightnessBody(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
        f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
        return f1 * 0.8F + 0.2F;
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
        return this.provider.getSkyColor(entityIn, partialTicks);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        int i = MathHelper.floor(entityIn.posX);
        int j = MathHelper.floor(entityIn.posY);
        int k = MathHelper.floor(entityIn.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        int l = net.minecraftforge.client.ForgeHooksClient.getSkyBlendColour(this, blockpos);
        float f3 = (float)(l >> 16 & 255) / 255.0F;
        float f4 = (float)(l >> 8 & 255) / 255.0F;
        float f5 = (float)(l & 255) / 255.0F;
        f3 = f3 * f1;
        f4 = f4 * f1;
        f5 = f5 * f1;
        float f6 = this.getRainStrength(partialTicks);

        if (f6 > 0.0F)
        {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f8 = 1.0F - f6 * 0.75F;
            f3 = f3 * f8 + f7 * (1.0F - f8);
            f4 = f4 * f8 + f7 * (1.0F - f8);
            f5 = f5 * f8 + f7 * (1.0F - f8);
        }

        float f10 = this.getThunderStrength(partialTicks);

        if (f10 > 0.0F)
        {
            float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f9 = 1.0F - f10 * 0.75F;
            f3 = f3 * f9 + f11 * (1.0F - f9);
            f4 = f4 * f9 + f11 * (1.0F - f9);
            f5 = f5 * f9 + f11 * (1.0F - f9);
        }

        if (this.lastLightningBolt > 0)
        {
            float f12 = (float)this.lastLightningBolt - partialTicks;

            if (f12 > 1.0F)
            {
                f12 = 1.0F;
            }

            f12 = f12 * 0.45F;
            f3 = f3 * (1.0F - f12) + 0.8F * f12;
            f4 = f4 * (1.0F - f12) + 0.8F * f12;
            f5 = f5 * (1.0F - f12) + 1.0F * f12;
        }

        return new Vec3d((double)f3, (double)f4, (double)f5);
    }


    public float getCelestialAngle(float partialTicks) {
        return this.provider.calculateCelestialAngle(this.getWorldTime(), partialTicks);
    }


    @SideOnly(Side.CLIENT)
    public int getMoonPhase() {
        return this.provider.getMoonPhase(this.getWorldTime());
    }


    public float getCurrentMoonPhaseFactor() {
        return provider.getCurrentMoonPhaseFactor();
    }


    public float getCurrentMoonPhaseFactorBody() {
        return WorldProvider.MOON_PHASE_FACTORS[this.provider.getMoonPhase(this.getWorldTime())];
    }


    public float getCelestialAngleRadians(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        return f * ((float)Math.PI * 2F);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColour(float partialTicks) {
        return this.provider.getCloudColor(partialTicks);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColorBody(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        float f2 = 1.0F;
        float f3 = 1.0F;
        float f4 = 1.0F;
        float f5 = this.getRainStrength(partialTicks);

        if (f5 > 0.0F) {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f7 = 1.0F - f5 * 0.95F;
            f2 = f2 * f7 + f6 * (1.0F - f7);
            f3 = f3 * f7 + f6 * (1.0F - f7);
            f4 = f4 * f7 + f6 * (1.0F - f7);
        }

        f2 = f2 * (f1 * 0.9F + 0.1F);
        f3 = f3 * (f1 * 0.9F + 0.1F);
        f4 = f4 * (f1 * 0.85F + 0.15F);
        float f9 = this.getThunderStrength(partialTicks);

        if (f9 > 0.0F) {
            float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f8 = 1.0F - f9 * 0.95F;
            f2 = f2 * f8 + f10 * (1.0F - f8);
            f3 = f3 * f8 + f10 * (1.0F - f8);
            f4 = f4 * f8 + f10 * (1.0F - f8);
        }
        return new Vec3d((double)f2, (double)f3, (double)f4);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        return this.provider.getFogColor(f, partialTicks);
    }


    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return this.getChunk(pos).getPrecipitationHeight(pos);
    }


    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        Chunk chunk = this.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.down();
            IBlockState state = chunk.getBlockState(blockpos1);

            if (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, this, blockpos1) && !state.getBlock().isFoliage(this, blockpos1)) {
                break;
            }
        }
        return blockpos;
    }


    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
        return this.provider.getStarBrightness(partialTicks);
    }


    @SideOnly(Side.CLIENT)
    public float getStarBrightnessBody(float partialTicks) {
        float f = this.getCelestialAngle(partialTicks);
        float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }


    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        return true;
    }


    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {}


    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {}


    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {}


    public void tickLoadedEntities(){
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            Entity[] raw = list.raw();
            for (int cursor = 0; cursor < list.sizeC(); ++cursor) {
                Entity entity = raw[cursor];
                Entity ridding = entity.getRidingEntity();

                if (ridding != null) {
                    if (!ridding.isDead && ridding.isPassenger(entity))
                        continue;
                    entity.dismountRidingEntity();
                }

                if (!entity.isDead && !(entity instanceof EntityPlayerMP))
                    this.updateEntity(entity);

                if (entity.isDead) {
                    int l1 = entity.chunkCoordX;
                    int i2 = entity.chunkCoordZ;

                    if (entity.addedToChunk && this.isChunkLoaded(l1, i2, true)) {
                        this.getChunk(l1, i2).removeEntity(entity);
                    }
                    list.removeSwap(cursor--);
                    this.onEntityRemoved(entity);
                }
            }
        }
    }


    protected ReportedException onEntityError(Entity entity, Throwable exception){
        CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking entity");
        CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

        if (entity == null) {
            crashreportcategory.addCrashSection("Entity", "~~NULL~~");
        } else {
            entity.addEntityCrashInfo(crashreportcategory);
        }
        if (ElcaConfig.REMOVE_ERROR_ENTITIES) {
            //FMLLog.log.fatal("{}", crashreport.getCompleteReport());
            removeEntity(entity);
        }
        return new ReportedException(crashreport);
    }


    protected void unloadEntites(){
        final List<Entity> toUnload;
        this.realEntityList.removeRefSwapAll(toUnload = this.unloadedEntityList);

        for (int k = 0; k < toUnload.size(); ++k) {
            Entity entity = toUnload.get(k);
            int cX = entity.chunkCoordX;
            int cZ = entity.chunkCoordZ;

            if (entity.addedToChunk && this.isChunkLoaded(cX, cZ, true)) {
                this.getChunk(cX, cZ).removeEntity(entity);
            }
            onEntityRemoved(entity);
        }
        toUnload.clear();
    }


    protected boolean checkCanUpdate;
    public void updateEntities() {
        final List<Entity> effects = this.weatherEffects;
        for (int i = 0; i < effects.size(); ++i) {
            Entity entity = effects.get(i);
            try {
                if(entity.updateBlocked) continue;
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable throwable2) {
                throw onEntityError(entity, throwable2);
            }
            if (entity.isDead) effects.remove(i--);
        }

        unloadEntites();
        checkCanUpdate = false;
        this.tickPlayers();

        //entities
        checkCanUpdate = true;
        tickLoadedEntities();

        this.processingLoadedTiles = true; //FML Move above remove to prevent CMEs
        List<TileEntity> loadedTileEntityList = this.loadedTileEntityList;

        //tile entities
        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            ReferenceOpenHashSet<TileEntity> remove = new ReferenceOpenHashSet<>();
            for (TileEntity tile : tileEntitiesToBeRemoved) {
                if(remove.add(tile)) tile.onChunkUnload();
            }

            this.tickableTileEntities.removeAll(remove);
            loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        Iterator<TileEntity> iterator = this.tickableTileEntities.remIterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (this.isBlockLoaded(blockpos, false) && this.worldBorder.contains(blockpos))
                    //Forge: Fix TE's getting an extra tick on the client side....
                {
                    try {
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                        ((ITickable)tileentity).update();
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
                    } catch (Throwable throwable) {
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory2);
                        if (net.minecraftforge.common.ForgeModContainer.removeErroringTileEntities) {
                            net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport2.getCompleteReport());
                            tileentity.invalidate();
                            this.removeTileEntity(tileentity.getPos());
                        } else throw new ReportedException(crashreport2);
                    }
                }
            }

            if (tileentity.isInvalid()) {
                iterator.remove();
                loadedTileEntityList.remove(tileentity);

                if (this.isBlockLoaded(tileentity.getPos())) {
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                    Chunk chunk = this.getChunk(tileentity.getPos());
                    if (chunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity)
                        chunk.removeTileEntity(tileentity.getPos());
                }
            }
        }

        this.processingLoadedTiles = false;

        List<TileEntity> addedTileEntityList = this.addedTileEntityList;
        if (!addedTileEntityList.isEmpty()) {
            for (int j1 = 0; j1 < addedTileEntityList.size(); ++j1) {
                TileEntity tileentity1 = addedTileEntityList.get(j1);

                if (!tileentity1.isInvalid()) {
                    if (!loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos())) {
                        Chunk chunk = this.getChunk(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }
            addedTileEntityList.clear();
        }
    }


    protected void tickPlayers() {}


    public boolean addTileEntity(TileEntity tile) {
        // Forge - set the world early as vanilla doesn't set it until next tick
        if (tile.getWorld() != this) tile.setWorld(this);
        // Forge: wait to add new TE if we're currently processing existing ones
        if (processingLoadedTiles) return addedTileEntityList.add(tile);

        boolean flag = this.loadedTileEntityList.add(tile);

        if (flag && tile instanceof ITickable) {
            this.tickableTileEntities.addF(tile);
        }
        tile.onLoad();

        if (this.isRemote) {
            BlockPos blockpos1 = tile.getPos();
            IBlockState iblockstate1 = this.getBlockState(blockpos1);
            this.notifyBlockUpdate(blockpos1, iblockstate1, iblockstate1, 2);
        }
        return flag;
    }


    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        if (this.processingLoadedTiles) {
            for (TileEntity te : tileEntityCollection) {
                if (te.getWorld() != this) // Forge - set the world early as vanilla doesn't set it until next tick
                    te.setWorld(this);
            }
            this.addedTileEntityList.addAll(tileEntityCollection);
        } else {
            for (TileEntity tileentity2 : tileEntityCollection) {
                this.addTileEntity(tileentity2);
            }
        }
    }


    public void updateEntity(Entity ent) {
        this.updateEntityWithOptionalForce(ent, true);
    }


    public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate) {
        if (checkCanUpdate) {
            boolean canUpdate = isChunkLoaded(entity.chunkCoordX, entity.chunkCoordZ, true);

            if (!canUpdate) canUpdate = canUpdateEvent.post(entity);
            if (!canUpdate) return;
        }

        entity.lastTickPosX      = entity.posX;
        entity.lastTickPosY      = entity.posY;
        entity.lastTickPosZ      = entity.posZ;
        entity.prevRotationYaw   = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;

        if (forceUpdate && entity.addedToChunk) {
            ++entity.ticksExisted;

            if (entity.isRiding()) {
                entity.updateRidden();
            } else {
                if(!entity.updateBlocked) entity.onUpdate();
            }
        }

        if (MathUtls.isInvalidDouble(entity.posX))          entity.posX          = entity.lastTickPosX     ;
        if (MathUtls.isInvalidDouble(entity.posY))          entity.posY          = entity.lastTickPosY     ;
        if (MathUtls.isInvalidDouble(entity.posZ))          entity.posZ          = entity.lastTickPosZ     ;
        if (MathUtls.isInvalidDouble(entity.rotationPitch)) entity.rotationPitch = entity.prevRotationPitch;
        if (MathUtls.isInvalidDouble(entity.rotationYaw))   entity.rotationYaw   = entity.prevRotationYaw  ;

        updateEntityChunkSection(entity, forceUpdate);
    }


    protected void updateEntityChunkSection(Entity entityIn, boolean forceUpdate){
        int i3 = MathHelper.floor(entityIn.posX / 16.0D);
        int j3 = MathHelper.floor(entityIn.posY / 16.0D);
        int k3 = MathHelper.floor(entityIn.posZ / 16.0D);

        if (!entityIn.addedToChunk || entityIn.chunkCoordX != i3 || entityIn.chunkCoordY != j3 || entityIn.chunkCoordZ != k3) {
            if (entityIn.addedToChunk && this.isChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
                this.getChunk(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }

            if (!entityIn.setPositionNonDirty() && !this.isChunkLoaded(i3, k3, true)) {
                entityIn.addedToChunk = false;
            } else {
                this.getChunk(i3, k3).addEntity(entityIn);
            }
        }

        if (forceUpdate && entityIn.addedToChunk) {
            for (Entity entity4 : entityIn.getPassengers()) {
                if (!entity4.isDead && entity4.getRidingEntity() == entityIn) {
                    this.updateEntity(entity4);
                } else {
                    entity4.dismountRidingEntity();
                }
            }
        }
    }


    public boolean checkNoEntityCollision(AxisAlignedBB bb) {
        return this.checkNoEntityCollision(bb, null);
    }


    public boolean checkNoEntityCollision(AxisAlignedBB bb, @Nullable Entity entityIn) {
        List<Entity> list = this.getEntitiesWithinAABBExcludingEntity(null, bb);
        for (int j2 = 0; j2 < list.size(); ++j2) {
            Entity entity4 = list.get(j2);

            // Forge: fix MC-103516
            if (!entity4.isDead && entity4.preventEntitySpawning && entity4 != entityIn
                    && (entityIn == null || !entity4.isRidingSameEntity(entityIn)))
                return false;
        }
        return true;
    }


    public boolean checkBlockCollision(AxisAlignedBB bb) {
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        if(minY < 0) minY = 0;
        int maxY = MathHelper.ceil(bb.maxY);
        if(maxY > 255)maxY = 255;
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);
        ChunkSelector access = localSelector;

        for (int dx = minX; dx < maxX; ++dx) {
            for (int dz = minZ; dz < maxZ; ++dz) {
                Chunk chunk = access.getChunkE(dx>>4, dz>>4);
                for (int dy = minY; dy < maxY; ++dy) {
                    if (chunk.getBlockStateUnchecked(dx, dy, dz).getMaterial() != Material.AIR)
                        return true;
                }
            }
        }
        return false;
    }


    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        if(minY < 0) minY = 0;
        int maxY = MathHelper.ceil(bb.maxY);
        if(maxY > 255)maxY = 255;
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        ChunkSelector access = localSelector;
        BlockPos.MutableBlockPos pooledPos = tempPooledLocalPos;

        for (int dx = minX; dx < maxX; ++dx) {
            for (int dz = minZ; dz < maxZ; ++dz) {
                Chunk chunk = access.getChunkE(dx>>4, dz>>4);
                for (int dy = minY; dy < maxY; ++dy) {
                    IBlockState state = chunk.getBlockStateUnchecked(dx, dy, dz);

                    Boolean result = state.getBlock().isAABBInsideLiquid(this, pooledPos.setPos(dx, dy, dz), bb);
                    if (result != null) {
                        if (!result) continue;
                        return true;
                    }
                    if (state.getMaterial().isLiquid()) return true;
                }
            }
        }
        return false;
    }


    public Explosion createExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean damagesTerrain) {
        return this.newExplosion(entityIn, x, y, z, strength, false, damagesTerrain);
    }


    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean causesFire, boolean damagesTerrain) {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, causesFire, damagesTerrain);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return explosion;
    }


    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        IBlockEAccess selector = isRemote?this:new ChunkSelector(this);
        return getBlockDensity(selector, vec, bb);
    }


    public float getBlockDensity(IBlockEAccess selector, Vec3d vec,
                                 AxisAlignedBB bb) {
        final double bbMaxX = bb.maxX, bbMaxY = bb.maxY, bbMaxZ = bb.maxZ,
                bbMinX = bb.minX, bbMinY = bb.minY, bbMinZ = bb.minZ;

        double d0 = 1.0D / ((bbMaxX - bbMinX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((bbMaxY - bbMinY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((bbMaxZ - bbMinZ) * 2.0D + 1.0D);

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            if (!Double.isNaN(vec.x) && !Double.isNaN(vec.y) && !Double.isNaN(vec.z))return 0;

            double sX = (1.0D - Math.floor(1.0D / d0) * d0) * 0.5D;
            double sZ = (1.0D - Math.floor(1.0D / d2) * d2) * 0.5D;

            int j2 = 0, k2 = 0;

            final BlockPos.MutableBlockPos blockPos = checkSeePos;

            for (float fX = 0.0F; fX <= 1.0F; fX = (float)((double)fX + d0)) {
                for (float fY = 0.0F; fY <= 1.0F; fY = (float)((double)fY + d1)) {
                    for (float fZ = 0.0F; fZ <= 1.0F; fZ = (float)((double)fZ + d2)) {
                        double vX, vY, vZ;
                        if(
                                Double.isNaN(vX = (bbMinX + (bbMaxX - bbMinX) * (double)fX) + sX) ||
                                Double.isNaN(vY = (bbMinY + (bbMaxY - bbMinY) * (double)fY)     ) ||
                                Double.isNaN(vZ = (bbMinZ + (bbMaxZ - bbMinZ) * (double)fZ) + sZ)
                        ) {
                            ++j2;
                        } else {
                            if (rtxForExplosion(blockPos, selector, new Vec3d(vX, vY, vZ), vec))
                                ++j2;
                        }
                        ++k2;
                    }
                }
            }
            return (float)j2 / (float)k2;
        } else {
            return 0.0F;
        }
    }


    //return true if null
    private static boolean rtxForExplosion(BlockPos.MutableBlockPos blockPos, IBlockEAccess selector, Vec3d st, Vec3d end) {
        int xPos, yPos, zPos;

        blockPos.setPosB(
                xPos = MathHelper.floor(st.x),
                yPos = MathHelper.floor(st.y),
                zPos = MathHelper.floor(st.z)
        );

        IBlockState state;

        if ((state = selector.getBlockState(blockPos)).getBlock()
                .hasCollisionRayTrace(state, selector, blockPos, st, end)) {
            return false;
        }

        double num1 = 0, num2 = 0, num3 = 0, num4, num5, num6, num7, num8, num9;
        double vecX = st.x, vecY = st.y, vecZ = st.z;

        final int sX = MathHelper.floor(end.x);
        final int sY = MathHelper.floor(end.y);
        final int sZ = MathHelper.floor(end.z);

        int k1 = 200;
        while (k1-- >= 0) {
            if (
                    Double.isNaN(vecX) || Double.isNaN(vecY) || Double.isNaN(vecZ)
                            || (xPos == sX && yPos == sY && zPos == sZ)
            ) return true;

            num7 = end.x - vecX;
            num8 = end.y - vecY;
            num9 = end.z - vecZ;

            if (sX > xPos) {
                num1 = xPos + 1d;
                num4 = (num1 - vecX) / num7;
            } else if (sX < xPos) {
                num1 = xPos;
                num4 = (num1 - vecX) / num7;
            } else {
                num4 = 999d;
            }

            if (sY > yPos) {
                num2 = yPos + 1;
                num5 = (num2 - vecY) / num8;
            } else if (sY < yPos) {
                num2 = yPos;
                num5 = (num2 - vecY) / num8;
            } else {
                num5 = 999d;
            }

            if (sZ > zPos) {
                num3 = zPos + 1;
                num6 = (num3 - vecZ) / num9;
            } else if (sZ < zPos) {
                num3 = zPos;
                num6 = (num3 - vecZ) / num9;
            } else {
                num6 = 999d;
            }

            if (num4 == -0.0D) num4 = -1.0E-4D;
            if (num5 == -0.0D) num5 = -1.0E-4D;
            if (num6 == -0.0D) num6 = -1.0E-4D;

            EnumFacing enumfacing;

            if (num4 < num5 && num4 < num6) {
                enumfacing = sX > xPos ? EnumFacing.WEST : EnumFacing.EAST;
                vecX = num1;
                vecY = vecY + num8 * num4;
                vecZ = vecZ + num9 * num4;
            } else if (num5 < num6) {
                enumfacing = sY > yPos ? EnumFacing.DOWN : EnumFacing.UP;
                vecX = vecX + num7 * num5;
                vecY = num2;
                vecZ = vecZ + num9 * num5;
            } else {
                enumfacing = sZ > zPos ? EnumFacing.NORTH : EnumFacing.SOUTH;
                vecX = vecX + num7 * num6;
                vecY = vecY + num8 * num6;
                vecZ = num3;
            }

            state = selector.getBlockState(
                    xPos = MathHelper.floor(vecX) - (enumfacing == EnumFacing.EAST ? 1 : 0),
                    yPos = MathHelper.floor(vecY) - (enumfacing == EnumFacing.UP ? 1 : 0),
                    zPos = MathHelper.floor(vecZ) - (enumfacing == EnumFacing.SOUTH ? 1 : 0)
            );

            Block block;
            if ((block = state.getBlock()).canCollideCheck(state, false)) {
                blockPos.setPosB(xPos, yPos, zPos);

                if (block.hasCollisionRayTrace(state, selector, blockPos, new Vec3d(vecX, vecY, vecZ), end))
                    return false;
            }
        }
        return true;
    }


    public boolean extinguishFire(@Nullable EntityPlayer player, BlockPos pos, EnumFacing side) {
        pos = pos.offset(side);
        if (this.getBlockState(pos).getBlock() == Blocks.FIRE) {
            this.playEvent(player, 1009, pos, 0);
            this.setBlockToAir(pos);
            return true;
        } else return false;
    }


    @SideOnly(Side.CLIENT)
    public String getDebugLoadedEntities() {
        return "All: " + this.realEntityList.size();
    }


    @SideOnly(Side.CLIENT)
    public String getProviderName() {
        return this.chunkProvider.makeString();
    }


    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return null;
        } else {
            TileEntity tileEntity = null;

            if (this.processingLoadedTiles) tileEntity = this.getPendingTileEntityAt(pos);

            if (tileEntity == null)
                tileEntity = this.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);

            return tileEntity == null?this.getPendingTileEntityAt(pos):tileEntity;
        }
    }


    public TileEntity getTileForAcces(ChunkSelector selector, BlockPos pos) {
        if(this.isOutsideBuildHeight(pos))return null;
        TileEntity tileEntity = null;
        if (this.processingLoadedTiles) tileEntity = this.getPendingTileEntityAt(pos);

        if (tileEntity == null)
            tileEntity = selector.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);

        return tileEntity == null ? this.getPendingTileEntityAt(pos) : tileEntity;
    }


    @Nullable
    private TileEntity getPendingTileEntityAt(BlockPos pos) {
        for (int j2 = 0; j2 < this.addedTileEntityList.size(); ++j2) {
            TileEntity tileentity2 = this.addedTileEntityList.get(j2);

            if (!tileentity2.isInvalid() && tileentity2.getPos().equals(pos))
                return tileentity2;
        }
        return null;
    }


    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks
        if (!this.isOutsideBuildHeight(pos)) {
            if (tileEntityIn != null && !tileEntityIn.isInvalid()) {
                if (this.processingLoadedTiles) {
                    tileEntityIn.setPos(pos);
                    if (tileEntityIn.getWorld() != this)
                        tileEntityIn.setWorld(this); // Forge - set the world early as vanilla doesn't set it until next tick
                    Iterator<TileEntity> iterator1 = this.addedTileEntityList.iterator();
                    List<TileEntity> toInvalidate = Lists.newArrayList();

                    while (iterator1.hasNext()) {
                        TileEntity tileentity2 = iterator1.next();

                        if (tileentity2.getPos().equals(pos)) {
                            toInvalidate.add(tileentity2); // Forge - don't call invalidate while iterating
                            iterator1.remove();
                        }
                    }
                    toInvalidate.forEach(TileEntity::invalidate);
                    this.addedTileEntityList.add(tileEntityIn);
                } else {
                    Chunk chunk = this.getChunk(pos);
                    if (chunk != null) chunk.addTileEntity(pos, tileEntityIn);
                    this.addTileEntity(tileEntityIn);
                }
            }
        }
    }


    public void removeTileEntity(BlockPos pos) {
        TileEntity tileentity2 = this.getTileEntity(pos);

        if (tileentity2 != null && this.processingLoadedTiles) {
            tileentity2.invalidate();
            this.addedTileEntityList.remove(tileentity2);
            if (!(tileentity2 instanceof ITickable)) //Forge: If they are not tickable they wont be removed in the update loop.
                this.loadedTileEntityList.remove(tileentity2);
        } else {
            if (tileentity2 != null) {
                this.addedTileEntityList.remove(tileentity2);
                this.loadedTileEntityList.remove(tileentity2);
                this.tickableTileEntities.remove(tileentity2);
            }
            this.getChunk(pos).removeTileEntity(pos);
        }
        this.updateComparatorOutputLevel(pos, getBlockState(pos).getBlock()); //Notify neighbors of changes
    }


    public void markTileEntityForRemoval(TileEntity tileEntityIn) {
        this.tileEntitiesToBeRemoved.add(tileEntityIn);
    }


    public void calculateInitialSkylight() {
        int j2 = this.calculateSkylightSubtracted(1.0F);
        if (j2 != this.skylightSubtracted) {
            this.skylightSubtracted = j2;
        }
    }


    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        this.spawnHostileMobs = hostile;
        this.spawnPeacefulMobs = peaceful;
        this.provider.setAllowedSpawnTypes(hostile, peaceful);
    }


    private double worldBMinx, worldBMinz, worldBMaxx, worldBMaxz;
    public void tick() {
        if(playersModified){
            curPlayerEntities = Lists.newArrayList(playerEntities);
        }

        this.updateWeather();
        if(ElcaCV.ENABLE_COLLISION_EVENTS) collisionEvent.reset();
        if(ElcaCV.MORE_MOBS_OPTIMIZER){
            eeCache.tick();
            bbCache.tick();
        }

        worldBMinx = this.worldBorder.minX();
        worldBMinz = this.worldBorder.minZ();
        worldBMaxx = this.worldBorder.maxX();
        worldBMaxz = this.worldBorder.maxZ();

        clearEvents();
    }


    protected void calculateInitialWeather() {
        this.provider.calculateInitialWeather();
    }


    public void calculateInitialWeatherBody() {
        if (this.worldInfo.isRaining()) {
            this.rainingStrength = 1.0F;
            if (this.worldInfo.isThundering()) {
                this.thunderingStrength = 1.0F;
            }
        }
    }


    protected void updateWeather() {
        this.provider.updateWeather();
    }


    public void updateWeatherBody() {
        if (this.provider.hasSkyLight()) {
            if (!this.isRemote || SERVER_COMPILE) {
                if (this.getGameRules().doWeatherCycle.getBoolean()) {
                    int j2 = this.worldInfo.getCleanWeatherTime();

                    if (j2 > 0) {
                        --j2;
                        this.worldInfo.setCleanWeatherTime(j2);
                        this.worldInfo.setThunderTime(this.worldInfo.isThundering() ? 1 : 2);
                        this.worldInfo.setRainTime(this.worldInfo.isRaining() ? 1 : 2);
                    }

                    int k2 = this.worldInfo.getThunderTime();

                    if (k2 <= 0) {
                        if (this.worldInfo.isThundering()) {
                            this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
                        } else {
                            this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
                        }
                    } else {
                        --k2;
                        this.worldInfo.setThunderTime(k2);

                        if (k2 <= 0) {
                            this.worldInfo.setThundering(!this.worldInfo.isThundering());
                        }
                    }
                    int l2 = this.worldInfo.getRainTime();

                    if (l2 <= 0) {
                        if (this.worldInfo.isRaining()) {
                            this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
                        } else {
                            this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
                        }
                    } else {
                        --l2;
                        this.worldInfo.setRainTime(l2);
                        if (l2 <= 0)
                            this.worldInfo.setRaining(!this.worldInfo.isRaining());
                    }
                }

                this.prevThunderingStrength = this.thunderingStrength;

                if (this.worldInfo.isThundering()) {
                    this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
                } else {
                    this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
                }

                this.thunderingStrength = MathHelper.clamp(this.thunderingStrength, 0.0F, 1.0F);
                this.prevRainingStrength = this.rainingStrength;

                if (this.worldInfo.isRaining()) {
                    this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
                } else {
                    this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
                }
                this.rainingStrength = MathHelper.clamp(this.rainingStrength, 0.0F, 1.0F);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    protected void playMoodSoundAndCheckLight(int x, int z, Chunk chunkIn) {
        chunkIn.enqueueRelightChecks();
    }


    protected void updateBlocks() {}


    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        this.scheduledUpdatesAreImmediate = ElcaConfig.IMMEDIATE_UPDATE_LIMIT;
        state.getBlock().updateTick(this, pos, state, random);
        this.scheduledUpdatesAreImmediate = 0;
    }


    public boolean canBlockFreezeWater(BlockPos pos) {
        return this.canBlockFreeze(pos, false);
    }


    public boolean canBlockFreezeNoWater(BlockPos pos) {
        return this.canBlockFreeze(pos, true);
    }


    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
        return this.provider.canBlockFreeze(pos, noWaterAdj);
    }


    public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
        Biome biome = this.getBiome(pos);
        float f = biome.getTemperature(pos);

        if (f >= 0.15F) {
            return false;
        } else {
            if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState iblockstate1 = this.getBlockState(pos);
                Block block = iblockstate1.getBlock();

                if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && ((Integer)iblockstate1.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                    if (!noWaterAdj) {
                        return true;
                    }

                    boolean flag = this.isWater(pos.west()) && this.isWater(pos.east()) && this.isWater(pos.north()) && this.isWater(pos.south());

                    if (!flag) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    private boolean isWater(BlockPos pos) {
        return this.getBlockState(pos).getMaterial() == Material.WATER;
    }


    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return this.provider.canSnowAt(pos, checkLight);
    }


    public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
        Biome biome = this.getBiome(pos);
        float f = biome.getTemperature(pos);
        if (f >= 0.15F) return false;
        else if (!checkLight) {
            return true;
        } else {
            if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState state = this.getBlockState(pos);
                if (state.getBlock().isAir(state, this, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(this, pos)) {
                    return true;
                }
            }
            return false;
        }
    }


    public boolean checkLight(BlockPos pos) {
        boolean flag = false;
        if (this.provider.hasSkyLight())
            flag |= this.checkLightFor(EnumSkyBlock.SKY, pos);
        flag = flag | this.checkLightFor(EnumSkyBlock.BLOCK, pos);
        return flag;
    }


    private int getRawLight(BlockPos pos, EnumSkyBlock type) {
        if (type == EnumSkyBlock.SKY && this.canSeeSky(pos)) {
            return 15;
        } else {
            IBlockState state = this.getBlockState(pos);
            int j2 = type == EnumSkyBlock.SKY ? 0 : state.getBlock().getLightValue(state, this, pos);
            int k2 = state.getBlock().getLightOpacity(state, this, pos);

            if (k2 < 1) {
                k2 = 1;
            }
            if (k2 >= 15) {
                return j2; // Forge: fix MC-119932
            }
            else if (j2 >= 14) {
                return j2;
            }
            else {
                BlockPos.PooledMutableBlockPos pooled = BlockPos.PooledMutableBlockPos.retain();

                try {
                    for (EnumFacing enumfacing : EnumFacing.VALUES) {
                        pooled.setPos(pos).move(enumfacing);
                        int l2 = this.getLightFor(type, pooled) - k2;

                        if (l2 > j2) {
                            j2 = l2;
                        }

                        if (j2 >= 14) {
                            int i3 = j2;
                            return i3;
                        }
                    }
                    return j2;
                } finally {
                    pooled.release();
                }
            }
        }
    }


    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos){
        this.lightingEngine.scheduleLightUpdate(lightType, pos);
        return true;
    }


    public void checkLightFor(EnumSkyBlock lightType, int x, int y, int z){
        this.lightingEngine.scheduleLightUpdate(lightType, x, y, z);
    }


    public WorldChunkSlice slice(int x, int z){
        return new WorldChunkSlice(chunkProviderServer, x, z);
    }


    public void reuse(WorldChunkSlice slice, int x, int z){
        slice.tryLoad5x5Full(chunkProviderServer, x, z);
    }


    public WorldChunkSlice poledSlice = null;


    public boolean tickUpdates(boolean runAllPending) {
        return false;
    }


    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
        return null;
    }


    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
        return null;
    }


    @Keep
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        return this.getEntitiesInAABBexcluding(entityIn, bb, EntitySelectors.NOT_SPECTATING);
    }


    @Keep
    public <T extends Entity> List<T> getEntities(Class <? extends T > entityType, Predicate <? super T > filter) {
        List<T> result = Lists.<T>newArrayList();
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                Entity entity = list.getC(cursor);
                if (entityType.isAssignableFrom(entity.getClass()) && filter.apply((T) entity))
                    result.add((T) entity);
            }
        }
        return result;
    }


    @Keep
    public <T extends Entity> List<T> getPlayers(Class <? extends T > playerType, Predicate <? super T > filter) {
        List<T> list = Lists.<T>newArrayList();

        for (Entity entity4 : this.playerEntities) {
            if (playerType.isAssignableFrom(entity4.getClass()) && filter.apply((T)entity4))
                list.add((T)entity4);
        }
        return list;
    }


    @Keep
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class <? extends T > classEntity, AxisAlignedBB bb) {
        return this.<T>getEntitiesWithinAABB(classEntity, bb, EntitySelectors.NOT_SPECTATING);
    }


    @Keep
    @Nullable
    public <T extends Entity> T findNearestEntityWithinAABB(Class <? extends T > entityType, AxisAlignedBB aabb, T closestTo) {
        List<T> list = this.<T>getEntitiesWithinAABB(entityType, aabb);
        T t = null;
        double d0 = Double.MAX_VALUE;
        for (int j2 = 0; j2 < list.size(); ++j2) {
            T t1 = list.get(j2);
            if (t1 != closestTo && EntitySelectors.NOT_SPECTATING.apply(t1)) {
                double d1 = closestTo.getDistanceSq(t1);

                if (d1 <= d0) {
                    t = t1;
                    d0 = d1;
                }
            }
        }
        return t;
    }


    @Nullable
    public Entity getEntityByID(int id) {
        return this.entitiesById.lookup(id);
    }


    @SideOnly(Side.CLIENT)
    public List<Entity> getLoadedEntityList() {
        return this.loadedEntityList;
    }


    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        if (this.isBlockLoaded(pos)) {
            this.getChunk(pos).markDirty();
        }
    }


    public int countEntities(Class<?> entityType) {
        int count = 0;
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            for (int cursor = 0; cursor < list.sizeC(); ++cursor) {
                Entity entity = list.getC(cursor);
                if ((!(entity instanceof EntityLiving) || !((EntityLiving) entity).isNoDespawnRequired()) && entityType.isAssignableFrom(entity.getClass())) {
                    ++count;
                }
            }
        }
        return count;
    }


    public void loadEntities(Collection<Entity> entityCollection) {
        for (Entity entity4 : entityCollection) {
            if (!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity4, this))) {
                realEntityList.addF(entity4);
                this.onEntityAdded(entity4);
            }
        }
    }


    public void unloadEntities(Collection<Entity> entityCollection) {
        this.unloadedEntityList.addAll(entityCollection);
    }


    public boolean mayPlace(Block block, BlockPos pos, boolean skipCollisionCheck, EnumFacing side, @Nullable Entity placer) {
        IBlockState iblockstate1 = this.access().getBlockState(pos);
        AxisAlignedBB aabb = skipCollisionCheck ? null : block.getDefaultState().getCollisionBoundingBox(this, pos);

        if (!((placer instanceof EntityPlayer) || !onBlockPlace(placer, new BlockSnapshot(this, pos, block.getDefaultState()), side).isCanceled())) return false;
        if (aabb != Block.NULL_AABB && !this.checkNoEntityCollision(
                new AxisAlignedBB(
                        aabb.minX+pos.getX()+0.0626,
                        aabb.minY+pos.getY()+0.0724,
                        aabb.maxX+pos.getZ()+0.0624,
                        aabb.maxX+pos.getX()-0.0626,
                        aabb.maxY+pos.getY()-0.0726,
                        aabb.maxZ+pos.getZ()-0.0626
                )
        ))
            // Forge: Remove second parameter, we patch placer to be non-null, passing it here skips collision checks for the placer
        {
            return false;
        }
        else if (iblockstate1.getMaterial() == Material.CIRCUITS && block == Blocks.ANVIL) {
            return true;
        } else {
            return iblockstate1.getBlock().isReplaceable(this, pos) && block.canPlaceBlockOnSide(this, pos, side);
        }
    }


    public int getSeaLevel() {
        return this.seaLevel;
    }


    public void setSeaLevel(int seaLevelIn) {
        this.seaLevel = seaLevelIn;
    }


    @Override
    public WorldType getWorldType() {
        return this.worldInfo.getTerrainType();
    }


    public int getStrongPower(final BlockPos pos) {
        return getStrongPowerDelta(new BlockPosDelta(pos));
    }


    public boolean isSidePowered(BlockPos pos, EnumFacing side) {
        return this.getRedstonePower(pos, side) > 0;
    }


    public boolean isBlockPowered(BlockPos pos) {
        if (this.getRedstonePower(pos.down(), EnumFacing.DOWN) > 0) {
            return true;
        } else if (this.getRedstonePower(pos.up(), EnumFacing.UP) > 0) {
            return true;
        } else if (this.getRedstonePower(pos.north(), EnumFacing.NORTH) > 0) {
            return true;
        } else if (this.getRedstonePower(pos.south(), EnumFacing.SOUTH) > 0) {
            return true;
        } else if (this.getRedstonePower(pos.west(), EnumFacing.WEST) > 0) {
            return true;
        } else {
            return this.getRedstonePower(pos.east(), EnumFacing.EAST) > 0;
        }
    }


    public int getRedstonePowerFromNeighbors(BlockPos pos) {
        int k2, j2 = 0;
        for (EnumFacing enumfacing : EnumFacing.VALUES) {
            if ((k2 = this.getRedstonePower(pos.offset(enumfacing), enumfacing)) >= 15) return 15;
            if (k2 > j2) j2 = k2;
        }
        return j2;
    }


    @Nullable
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, false);
    }


    @Nullable
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, true);
    }


    @Nullable
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        return this.getClosestPlayer(
                posX, posY, posZ, distance,
                spectator ? EntitySelectors.CAN_AI_TARGET : EntitySelectors.NOT_SPECTATING
        );
    }


    @Nullable
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        if(distance > 0)distance = distance * distance;
        else distance = Double.MAX_VALUE;
        double d0 = -1.0D;
        EntityPlayer result = null;
        for (int i = 0, s = playerEntities.size(); i < s; ++i) {
            EntityPlayer player;
            if (predicate.apply(player = playerEntities.get(i))) {
                double d1;
                if (((d1 = player.getDistanceSq(x, y, z)) < distance) && (d0 == -1.0D || d1 < d0)) {
                    d0 = d1;
                    result = player;
                }
            }
        }
        return result;
    }


    public boolean isAnyPlayerWithinRangeAt(final double x, final double y, final double z, final double range) {
        final double rangeSq = range * range;
        final List<EntityPlayer> players = this.playerEntities;
        for (int i = 0, s = players.size(); i < s; ++i) {
            EntityPlayer player = players.get(i);
            if (!player.isSpectator()) {
                if (range < 0.0D || player.getDistanceSq(x, y, z) < rangeSq) {
                    return true;
                }
            }
        }
        return false;
    }


    @Nullable
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return this.getNearestAttackablePlayer(entityIn.posX, entityIn.posY, entityIn.posZ, maxXZDistance, maxYDistance, (Function)null, (Predicate)null);
    }


    @Nullable
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return this.getNearestAttackablePlayer((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), maxXZDistance, maxYDistance, (Function)null, (Predicate)null);
    }


    @Nullable
    public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> predicate) {
        double d0 = -1.0D;
        EntityPlayer nearest = null;
        final List<EntityPlayer> players = this.playerEntities;

        for (int i = 0, s = players.size(); i < s; ++i) {
            EntityPlayer player = players.get(i);

            if (!player.capabilities.disableDamage && player.isEntityAlive() && !player.isSpectator() && (predicate == null || predicate.apply(player))) {
                double d1 = player.getDistanceSq(posX, player.posY, posZ);
                double d2 = maxXZDistance;

                if (player.isSneaking()) {
                    d2 = maxXZDistance * 0.800000011920929D;
                }

                if (player.isInvisible()) {
                    float f = player.getArmorVisibility();

                    if (f < 0.1F) {
                        f = 0.1F;
                    }
                    d2 *= (double)(0.7F * f);
                }

                if (playerToDouble != null) {
                    d2 *= ((Double)MoreObjects.firstNonNull(playerToDouble.apply(player), Double.valueOf(1.0D))).doubleValue();
                }

                d2 = net.minecraftforge.common.ForgeHooks.getPlayerVisibilityDistance(player, d2, maxYDistance);

                if ((maxYDistance < 0.0D || Math.abs(player.posY - posY) < maxYDistance * maxYDistance) && (maxXZDistance < 0.0D || d1 < d2 * d2) && (d0 == -1.0D || d1 < d0)) {
                    d0 = d1;
                    nearest = player;
                }
            }
        }
        return nearest;
    }


    @Nullable
    public EntityPlayer getPlayerEntityByName(String name) {
        for (int j2 = 0; j2 < this.playerEntities.size(); ++j2) {
            EntityPlayer entityplayer = this.playerEntities.get(j2);
            if (name.equals(entityplayer.getName())) {
                return entityplayer;
            }
        }
        return null;
    }


    @Nullable
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        for (int j2 = 0; j2 < this.playerEntities.size(); ++j2) {
            EntityPlayer entityplayer = this.playerEntities.get(j2);

            if (uuid.equals(entityplayer.getUniqueID())) {
                return entityplayer;
            }
        }
        return null;
    }


    @SideOnly(Side.CLIENT)
    public void sendQuittingDisconnectingPacket() {}


    public void checkSessionLock() throws MinecraftException {
        this.saveHandler.checkSessionLock();
    }


    @SideOnly(Side.CLIENT)
    public void setTotalWorldTime(long worldTime) {
        this.worldInfo.setWorldTotalTime(worldTime);
    }


    public long getSeed() {
        return this.provider.getSeed();
    }


    public long getTotalWorldTime() {
        return this.worldInfo.getWorldTotalTime();
    }


    public long getWorldTime() {
        return this.provider.getWorldTime();
    }


    public void setWorldTime(long time) {
        this.provider.setWorldTime(time);
    }


    public BlockPos getSpawnPoint() {
        BlockPos point = this.provider.getSpawnPoint();
        if (!isInWorldBorder(point.getX(), point.getY())) {
            point = this.getHeight(new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
        }
        return point;
    }


    public void setSpawnPoint(BlockPos pos) {
        this.provider.setSpawnPoint(pos);
    }


    //TODO
    @SideOnly(Side.CLIENT)
    public void joinEntityInSurroundings(Entity entityIn) {
        int j2 = MathHelper.floor(entityIn.posX / 16.0D);
        int k2 = MathHelper.floor(entityIn.posZ / 16.0D);
        int l2 = 2;

        for (int i3 = -2; i3 <= 2; ++i3) {
            for (int j3 = -2; j3 <= 2; ++j3)
                this.getChunk(j2 + i3, k2 + j3);
        }

        if (!this.realEntityList.containsRef(entityIn)) {
            if (!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entityIn, this)))
                this.realEntityList.addF(entityIn);
        }
    }


    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return this.provider.canMineBlock(player, pos);
    }


    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return true;
    }


    public void setEntityState(Entity entityIn, byte state) {
    }


    public IChunkProvider getChunkProvider() {
        return this.chunkProvider;
    }


    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        this.getBlockState(pos).onBlockEventReceived(this, pos, eventID, eventParam);
    }


    public ISaveHandler getSaveHandler() {
        return this.saveHandler;
    }


    public WorldInfo getWorldInfo() {
        return this.worldInfo;
    }


    public GameRules getGameRules() {
        return this.worldInfo.getGameRulesInstance();
    }


    public void updateAllPlayersSleepingFlag() {}


    public float getThunderStrength(float delta) {
        return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * delta) * this.getRainStrength(delta);
    }


    @SideOnly(Side.CLIENT)
    public void setThunderStrength(float strength) {
        this.prevThunderingStrength = strength;
        this.thunderingStrength = strength;
    }


    public float getRainStrength(float delta) {
        return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * delta;
    }


    @SideOnly(Side.CLIENT)
    public void setRainStrength(float strength) {
        this.prevRainingStrength = strength;
        this.rainingStrength = strength;
    }


    public boolean isThundering() {
        return (double)this.getThunderStrength(1.0F) > 0.9D;
    }


    public boolean isRaining() {
        return (double)this.getRainStrength(1.0F) > 0.2D;
    }


    public boolean isRainingAt(BlockPos position) {
        if (!this.isRaining()) {
            return false;
        } else if (!this.canSeeSky(position)) {
            return false;
        } else if (this.getPrecipitationHeight(position).getY() > position.getY()) {
            return false;
        } else {
            Biome biome = this.getBiome(position);
            if (biome.getEnableSnow()) {
                return false;
            } else {
                return this.canSnowAt(position, false) ? false : biome.canRain();
            }
        }
    }


    public boolean isBlockinHighHumidity(BlockPos pos) {
        return this.provider.isBlockHighHumidity(pos);
    }


    @Nullable
    public MapStorage getMapStorage() {
        return this.mapStorage;
    }


    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        this.mapStorage.setData(dataID, worldSavedDataIn);
    }


    @Nullable
    public WorldSavedData loadData(Class <? extends WorldSavedData > clazz, String dataID) {
        return this.mapStorage.getOrLoadData(clazz, dataID);
    }


    public int getUniqueDataId(String key) {
        return this.mapStorage.getUniqueDataId(key);
    }


    public void playBroadcastSound(int id, BlockPos pos, int data) {
        for (int j2 = 0; j2 < this.eventListeners.size(); ++j2) {
            ((IWorldEventListener)this.eventListeners.get(j2)).broadcastSound(id, pos, data);
        }
    }


    public void playEvent(int type, BlockPos pos, int data) {
        this.playEvent((EntityPlayer)null, type, pos, data);
    }


    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {
        try {
            for (int j2 = 0; j2 < this.eventListeners.size(); ++j2) {
                ((IWorldEventListener)this.eventListeners.get(j2)).playEvent(player, type, pos, data);
            }
        } catch (Throwable throwable3) {
            CrashReport crashreport3 = CrashReport.makeCrashReport(throwable3, "Playing level event");
            CrashReportCategory crashreportcategory3 = crashreport3.makeCategory("Level event being played");
            crashreportcategory3.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(pos));
            crashreportcategory3.addCrashSection("Event source", player);
            crashreportcategory3.addCrashSection("Event type", Integer.valueOf(type));
            crashreportcategory3.addCrashSection("Event data", Integer.valueOf(data));
            throw new ReportedException(crashreport3);
        }
    }


    public int getHeight() {
        return this.provider.getHeight();
    }


    public int getActualHeight() {
        return this.provider.getActualHeight();
    }


    public Random setRandomSeed(int seedX, int seedY, int seedZ) {
        long j2 = (long)seedX * 341873128712L + (long)seedY * 132897987541L + this.getWorldInfo().getSeed() + (long)seedZ;
        this.rand.setSeed(j2);
        return this.rand;
    }


    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        CrashReportCategory crashreportcategory3 = report.makeCategoryDepth("Affected level", 1);
        crashreportcategory3.addCrashSection("Level name", this.worldInfo == null ? "????" : this.worldInfo.getWorldName());
        crashreportcategory3.addDetail("All players", new ICrashReportDetail<String>()
        {
            public String call()
            {
                return World.this.playerEntities.size() + " total; " + World.this.playerEntities;
            }
        });
        crashreportcategory3.addDetail("Chunk stats", new ICrashReportDetail<String>()
        {
            public String call()
            {
                return World.this.chunkProvider.makeString();
            }
        });

        try {
            this.worldInfo.addToCrashReport(crashreportcategory3);
        }
        catch (Throwable throwable3) {
            crashreportcategory3.addCrashSectionThrowable("Level Data Unobtainable", throwable3);
        }
        return crashreportcategory3;
    }


    @SideOnly(Side.CLIENT)
    public double getHorizon() {
        return provider.getHorizon();
    }


    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        for (int j2 = 0; j2 < this.eventListeners.size(); ++j2) {
            IWorldEventListener listener = this.eventListeners.get(j2);
            listener.sendBlockBreakProgress(breakerId, pos, progress);
        }
    }


    public Calendar getCurrentDate() {
        if (this.getTotalWorldTime() % 600L == 0L) {
            this.calendar.setTimeInMillis(MinecraftServer.getCurrentTimeMillis());
        }
        return this.calendar;
    }


    @SideOnly(Side.CLIENT)
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {}


    public Scoreboard getScoreboard() {
        return this.worldScoreboard;
    }


    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        for (EnumFacing enumfacing : EnumFacing.VALUES) {
            BlockPos blockpos1 = pos.offset(enumfacing);
            if (this.isBlockLoaded(blockpos1)) {
                IBlockState iblockstate1 = this.getBlockState(blockpos1);

                iblockstate1.getBlock().onNeighborChange(this, blockpos1, pos);
                if (iblockstate1.getBlock().isNormalCube(iblockstate1, this, blockpos1)) {
                    blockpos1 = blockpos1.offset(enumfacing);
                    iblockstate1 = this.getBlockState(blockpos1);

                    if (iblockstate1.getBlock().getWeakChanges(this, blockpos1)) {
                        iblockstate1.getBlock().onNeighborChange(this, blockpos1, pos);
                    }
                }
            }
        }
    }


    public DifficultyInstance getDifficultyForLocation(BlockPos pos){
        return getDifficultyForLocation(pos.getX(), pos.getZ());
    }


    public DifficultyInstance getDifficultyForLocation(int blockX, int blockZ) {
        long j2 = 0L;
        float f = 0.0F;

        if (this.isChunkLoaded(blockX>>4, blockZ>>4, true)) {
            f = this.getCurrentMoonPhaseFactor();
            j2 = this.getChunk(blockX>>4, blockZ>>4).getInhabitedTime();
        }
        return new DifficultyInstance(this.getDifficulty(), this.getWorldTime(), j2, f);
    }


    public EnumDifficulty getDifficulty() {
        return this.getWorldInfo().getDifficulty();
    }


    public int getSkylightSubtracted() {
        return this.skylightSubtracted;
    }


    public void setSkylightSubtracted(int newSkylightSubtracted) {
        this.skylightSubtracted = newSkylightSubtracted;
    }


    @SideOnly(Side.CLIENT)
    public int getLastLightningBolt() {
        return this.lastLightningBolt;
    }


    public void setLastLightningBolt(int lastLightningBoltIn) {
        this.lastLightningBolt = lastLightningBoltIn;
    }


    public VillageCollection getVillageCollection() {
        return this.villageCollection;
    }


    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }


    public boolean isSpawnChunk(int x, int z) {
        BlockPos blockpos1 = this.getSpawnPoint();
        int j2 = x * 16 + 8 - blockpos1.getX();
        int k2 = z * 16 + 8 - blockpos1.getZ();
        int l2 = 128;
        return j2 >= -128 && j2 <= 128 && k2 >= -128 && k2 <= 128;
    }

    /* ======================================== FORGE START =====================================*/
    /**
     * Determine if the given block is considered solid on the
     * specified side.  Used by placement logic.
     *
     * @param pos Block Position
     * @param side The Side in question
     * @return True if the side is solid
     */
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return isSideSolid(pos, side, false);
    }


    /**
     * Determine if the given block is considered solid on the
     * specified side.  Used by placement logic.
     *
     * @param pos Block Position
     * @param side The Side in question
     * @param _default The default to return if the block doesn't exist.
     * @return True if the side is solid
     */
    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (!this.isValid(pos)) return _default;

        Chunk chunk = getChunk(pos);
        if (chunk == null || chunk.isEmpty()) return _default;
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    /**
     * Get the persistent chunks for this world
     */
    public com.google.common.collect.ImmutableSetMultimap<net.minecraft.util.math.ChunkPos, net.minecraftforge.common.ForgeChunkManager.Ticket> getPersistentChunks() {
        return net.minecraftforge.common.ForgeChunkManager.getPersistentChunksFor(this);
    }


    @Deprecated
    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        return chunkIterator;
    }
    /**
     * Readded as it was removed, very useful helper function
     *
     * @param pos Block position
     * @return The blocks light opacity
     */
    public int getBlockLightOpacity(BlockPos pos) {
        if (!this.isValid(pos)) return 0;
        return getChunk(pos).getBlockLightOpacity(pos);
    }


    /**
     * Returns a count of entities that classify themselves as the specified creature type.
     */
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        int count = 0;
        for (EntityRemSwapList.E15<Entity> list: realEntityList.getRaw()) {
            for (int cursor = 0, s = list.sizeC(); cursor < s; ++cursor) {
                if(list.getC(cursor).isCreatureType(type, forSpawnCount))
                    ++count;
            }
        }
        return count;
    }


    @Deprecated // remove in 1.13
    public void markTileEntitiesInChunkForRemoval(Chunk chunk) {
        for (TileEntity tileentity : chunk.getTileEntityMap().values()) {
            markTileEntityForRemoval(tileentity);
        }
    }


    protected void initCapabilities() {
        net.minecraftforge.common.capabilities.ICapabilityProvider parent = provider.initCapabilities();
        capabilities = net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(this, parent);
        net.minecraftforge.common.util.WorldCapabilityData data = (net.minecraftforge.common.util.WorldCapabilityData)perWorldStorage.getOrLoadData(net.minecraftforge.common.util.WorldCapabilityData.class, net.minecraftforge.common.util.WorldCapabilityData.ID);
        if (data == null) {
            capabilityData = new net.minecraftforge.common.util.WorldCapabilityData(capabilities);
            perWorldStorage.setData(capabilityData.mapName, capabilityData);
        }
        else {
            capabilityData = data;
            capabilityData.setCapabilities(provider, capabilities);
        }
    }


    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilities == null ? false : capabilities.hasCapability(capability, facing);
    }


    @Override
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable EnumFacing facing) {
        return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }

    protected MapStorage perWorldStorage;
    //Moved to a getter to simulate final without being final so we can load in subclasses.


    public MapStorage getPerWorldStorage() {
        return perWorldStorage;
    }


    public void sendPacketToServer(Packet<?> packetIn) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }


    public LootTableManager getLootTableManager() {
        return this.lootTable;
    }


    @Nullable
    public BlockPos findNearestStructure(String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }


    @Override
    public World getWorld() {
        return this;
    }


    public void onUnload(){
        loaded = false;
    }


    /**
     * @author Te4hno
     */
    private final AABBList.Hole hole = new AABBList.Hole();
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return !getCollisionBoxesTrue(null, bbox, hole.free());
    }


    /**
     * @author Te4hno
     */
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb) {
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            SyncListCache.AABB bbCache;
            AABKey key = (bbCache = this.bbCache).key(aabb);

            AABBList res = bbCache.getC(key);
            if (res != null) return res;
            res = bbCache.allocArr();
            getCollisionBoxes0(res, entityIn, aabb);
            bbCache.add(key, res);
            return res;
        } else {
            AABBList res = new AABBList();
            getCollisionBoxes0(res, entityIn, aabb);
            return res;
        }
    }


    /**
     * @author Te4hno
     */
    public List<AxisAlignedBB> getCollisionBoxesLimited(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb, int lim) {
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            final AABKey key;
            AABBList res;
            SyncListCache.AABB bbCache;
            if ((res = (bbCache = this.bbCache).getC(key = bbCache.key(aabb))) != null) return res;
            getCollisionBoxesNoCacheLimited(res = bbCache.allocArr(), entityIn, aabb, lim);
            bbCache.add(key, res);
            return res;
        } else {
            final AABBList list;
            getCollisionBoxesNoCacheLimited(list = new AABBList(), entityIn, aabb, lim);
            return list;
        }
    }


    public final AABBList getCollisionBoxesLimitedC(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb, int lim) {
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            final SyncListCache.AABB bbCache;
            final AABKey key;
            AABBList res;
            if ((res = (bbCache = this.bbCache).getC(key = bbCache.key(aabb))) != null) return res;
            getCollisionBoxesNoCacheLimited(res = bbCache.allocArr(), entityIn, aabb, lim);
            bbCache.add(key, res);
            return res;
        } else {
            final AABBList list;
            getCollisionBoxesNoCacheLimited(list = new AABBList(), entityIn, aabb, lim);
            return list;
        }
    }


    public final AABBList getCollisionBoxesRecachedLimitedC(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb, int lim) {
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            final AABKey key;
            AABBList res;
            SyncListCache.AABB bbCache;
            if ((res = (bbCache = this.bbCache).getC(key = bbCache.key(aabb))) != null){
                res.fclear();
                getCollisionBoxesNoCacheLimited(res, entityIn, aabb, lim);
            } else {
                getCollisionBoxesNoCacheLimited(res = bbCache.allocArr(), entityIn, aabb, lim);
            }
            return res;
        } else {
            final AABBList list;
            getCollisionBoxesNoCacheLimited(list = new AABBList(), entityIn, aabb, lim);
            return list;
        }
    }


    /**
     * @author Te4hno
     */
    @NotNull
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb,
                                                   @Nullable Predicate<? super Entity > predicate){
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            SyncListCache.EList eeCache;
            AABKey key = (eeCache = this.eeCache).key(aabb);

            final EntityList<Entity> entities = eeCache.getC(key);
            if (entities != null) {
                if (predicate != null && !entities.isEmptyC()) {
                    for (final Entity entity : entities) {
                        if (!predicate.apply(entity)) {
                            EntityList<Entity> res = eeCache.allocArr();
                            getEntitiesInAABBexcludingNoCache(res, entityIn, aabb, predicate);
                            eeCache.add(key, res);
                            return res;
                        }
                    }
                }
                return entities;
            } else {
                EntityList<Entity> res = eeCache.allocArr();
                getEntitiesInAABBexcludingNoCache(res, entityIn, aabb, predicate);
                eeCache.add(key, res);
                return res;
            }
        } else {
            EntityList<Entity> res = new EntityList<>();
            getEntitiesInAABBexcludingNoCache(res, entityIn, aabb, predicate);
            return res;
        }
    }


    /**
     * @author Te4hno
     */
    @NotNull
    public <T extends Entity> List<T> getEntitiesWithinAABB(@NotNull Class <? extends T > clazz, @NotNull AxisAlignedBB aabb,
                                                            @Nullable Predicate<? super T > filter){
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            SyncListCache.EList eeCache;
            AABKey key = (eeCache = this.eeCache).key(aabb);
            //AABKey key = new AABKey(aabb);
            final EntityList<Entity> entities = eeCache.getC(key);
            if (entities != null) {
                if (entities.isEmptyC()) return (List<T>) entities;
                for (final Entity entity : entities) {
                    if (!clazz.isInstance(entity) || filter != null && !filter.apply((T) entity)) {
                        EntityList<T> res = eeCache.allocArr();

                        getEntitiesWithinAABBNoCache(res, clazz, aabb, filter);
                        eeCache.add(key, (EntityList<Entity>)res);
                        return res;
                    }
                }
                return (List<T>) entities;
            } else {
                EntityList<T> res = eeCache.allocArr();
                getEntitiesWithinAABBNoCache(res, clazz, aabb, filter);
                eeCache.add(key, (EntityList<Entity>) res);
                return res;
            }
        } else {
            EntityList<T> res = new EntityList<>();
            getEntitiesWithinAABBNoCache(res, clazz, aabb, filter);
            return res;
        }
    }


    public <T extends Entity> List<T> getEntitiesWithinAABBNoCache(@NotNull Class <? extends T > clazz, @NotNull AxisAlignedBB aabb,
                                                            @Nullable Predicate<? super T > filter){
        EntityList<T> res = new EntityList<>();
        getEntitiesWithinAABBNoCache(res, clazz, aabb, filter);
        return res;
    }


    protected final GetCollisionBoxesEvent collisionEvent =
            ElcaCV.ENABLE_COLLISION_EVENTS?
            new GetCollisionBoxesEvent(this):null;


    private final AxisAlignedBB localTempAABB = new AxisAlignedBB(0,0,0,0,0,0);

    private void getCollisionBoxes0(AABBList list, @Nullable Entity entityIn, AxisAlignedBB aabb) {
        this.getCollisionBoxesFalse(entityIn, aabb, list);
        if (entityIn != null) {
            AxisAlignedBB temp;
            aabb.growTo(0.25D,0.25D,0.25D, temp = localTempAABB);
            List<Entity> list1 = getEntitiesInAABBexcluding(entityIn, temp, EntitySelectors.NOT_SPECTATING);
            addIfNeed(list1, list, entityIn, aabb);
        }
        if(ElcaCV.ENABLE_COLLISION_EVENTS)
            MinecraftForge.EVENT_BUS.postF(collisionEvent.init(entityIn, aabb, list));
    }


    public final void getCollisionBoxesNoCacheLimited(AABBList list, @Nullable Entity entityIn, AxisAlignedBB aabb, int lim) {
        this.getCollisionBoxesFalse(entityIn, aabb, list);
        if (entityIn != null && list.sizeC() < lim) {
            final AxisAlignedBB temp;
            aabb.growTo(0.25D,0.25D,0.25D, temp = localTempAABB);
            addIfNeed(
                    getEntitiesInAABBexcludingLimited(entityIn, temp, EntitySelectors.NOT_SPECTATING, lim),
                    list, entityIn, aabb
            );
        }
        if(ElcaCV.ENABLE_COLLISION_EVENTS)
            MinecraftForge.EVENT_BUS.postF(collisionEvent.init(entityIn, aabb, list));
        //return list;
    }


    public final AABBList allocAABBList(){
        return bbCache.allocArr();
    }


    public final void freeAABBList(AABBList list){
        bbCache.freeArr(list);
    }


    public final boolean noCollisionsBoxesNoCacheIn(AABBList list, @Nullable Entity entityIn, AxisAlignedBB aabb){
        list.fclear();
        getCollisionBoxesNoCacheLimited(list, entityIn, aabb, 1);
        return list.isEmptyC();
    }


    @NotNull
    public EntityList<Entity> getEntitiesInAABBexcludingLimited(@Nullable Entity entityIn, @NotNull AxisAlignedBB aabb,
                                                          @Nullable Predicate<? super Entity > predicate, int lim){
        if(ElcaCV.MORE_MOBS_OPTIMIZER) {
            SyncListCache.EList eeCache;
            AABKey key = (eeCache = this.eeCache).key(aabb);

            final EntityList<Entity> entities = eeCache.getC(key);
            if (entities != null) {
                int size;
                if (predicate != null && (size = entities.sizeC()) != 0) {
                    for (int i = 0; i < size; ++i) {
                        if (!predicate.apply(entities.getC(i))) {
                            EntityList<Entity> res = eeCache.allocArr();

                            getEntitiesInAABBexcludingNoCacheLimited(res, entityIn, aabb, predicate, lim);

                            eeCache.add(key, res);
                            return res;
                        }
                    }
                }
                return entities;
            } else {
                final EntityList<Entity> res;
                getEntitiesInAABBexcludingNoCacheLimited(res = eeCache.allocArr(), entityIn, aabb, predicate, lim);

                eeCache.add(key, res);
                return res;
            }
        } else {
            EntityList<Entity> res = new EntityList<>();
            getEntitiesInAABBexcludingNoCacheLimited(res, entityIn, aabb, predicate, lim);
            return res;
        }
    }


    private void addIfNeed(List<Entity> list1, List<AxisAlignedBB> list, Entity entityIn, @NotNull AxisAlignedBB aabb){
        for (int i = 0; i < list1.size(); ++i) {
            Entity entity = list1.get(i);
            if (!entityIn.isRidingSameEntity(entity)) {
                AxisAlignedBB axisalignedbb = entity.getCollisionBoundingBox();
                if (axisalignedbb != null && axisalignedbb.intersects(aabb))
                    list.add(axisalignedbb);
                axisalignedbb = entityIn.getCollisionBox(entity);
                if (axisalignedbb != null && axisalignedbb.intersects(aabb))
                    list.add(axisalignedbb);
            }
        }
    }


    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        if (y < 0 || y > 255) return BlockIDIndexes.AIR_STATE;
        else {
            Chunk chunk = this.getChunk(x >>4, z>>4);
            return chunk.getBlockState(x, y, z);
        }
    }


    //pooledEvents
    private final LivingSpawnEvent.CheckSpawn checkSpawnEvemt = new LivingSpawnEvent.CheckSpawn();
    private final BlockEvent.EntityPlaceEvent blockPlaceEvent = new BlockEvent.EntityPlaceEvent();
    private final EntityMobGriefingEvent     mobGriefingEvent = new EntityMobGriefingEvent(null);
    private final LivingEvent.LivingJumpEvent   jumpEvent     = new LivingEvent.LivingJumpEvent(null);
    private final LivingEvent.LivingUpdateEvent updateEvent   = new LivingEvent.LivingUpdateEvent().forBus(MinecraftForge.EVENT_BUS);
    private final LivingAttackEvent             attackEvent   = new LivingAttackEvent(null, null, 0);
    private final LivingHurtEvent               hurtEvent     = new LivingHurtEvent(null,null,0);
    private final LivingDamageEvent             damageEvent   = new LivingDamageEvent(null, null, 0);
    private final LivingSpawnEvent.SpecialSpawn specialSpawn  = new LivingSpawnEvent.SpecialSpawn(null,this,0,0,0,null);
    private final LivingSpawnEvent.AllowDespawn allowDespawn  = new LivingSpawnEvent.AllowDespawn();
    private final BlockEvent.CropGrowEvent.Pre  cropGrowPre   = new BlockEvent.CropGrowEvent.Pre();
    private final BlockEvent.CropGrowEvent.Post croptGrowPost = new BlockEvent.CropGrowEvent.Post();
    private final PlaySoundAtEntityEvent       playSoundEvent = new PlaySoundAtEntityEvent(null,null,null,0,0);

    public  final EntityEvent.EnteringChunk     enteringChunk = new EntityEvent.EnteringChunk(null,0,0,0,0);
    public  final ChunkWatchEvent             chunkWatchEvent = new ChunkWatchEvent.Watch().forBus(MinecraftForge.EVENT_BUS);
    public  final ChunkWatchEvent           chunkUnWatchEvent = new ChunkWatchEvent.UnWatch().forBus(MinecraftForge.EVENT_BUS);
    public  final EntityEvent.CanUpdate       canUpdateEvent  = new EntityEvent.CanUpdate(null).forBus(MinecraftForge.EVENT_BUS);
    public  final Random              commonWorldEntityRandom = new FastRandom();
    public  final AxisAlignedBB         commonWorldEntityAABB = new AxisAlignedBB(0,0,0,0,0,0);
    public  final AxisAlignedBB        commonWorldEntityAABB2 = new AxisAlignedBB(0,0,0,0,0,0);
    public  final CollisionPredicate                 entityCP = new CollisionPredicate();

    public  final Vec3dMut                 commonEntityVec3d1 = new Vec3dMut(0,0,0);
    public  final Vec3dMut                 commonEntityVec3d2 = new Vec3dMut(0,0,0);


    protected void clearEvents(){
        jumpEvent.setEntity(null);
        updateEvent.setEntity(null);
        attackEvent.setEntity(null);
        hurtEvent.setEntity(null);
        damageEvent.setEntity(null);
        enteringChunk.setEntity(null);
        mobGriefingEvent.setEntity(null);
        entityCP.reset();
        if(poledSlice != null) poledSlice.reset();
        bigLocalCache.cleanAll();
    }


    public boolean onCropsGrowPre(BlockPos pos, IBlockState state, boolean def) {
        BlockEvent ev = cropGrowPre.initCropPre(this, pos, state);
        MinecraftForge.EVENT_BUS.postF(ev);
        return (ev.getResult() == Event.Result.ALLOW || (ev.getResult() == Event.Result.DEFAULT && def));
    }


    public void onCropsGrowPost(BlockPos pos, IBlockState state, IBlockState other) {
        MinecraftForge.EVENT_BUS.postF(
                croptGrowPost.initCropPre(this, pos, state, this.access().getBlockState(pos))
        );
    }


    public Event.Result canEntityDespawn(EntityLiving entity) {
        LivingSpawnEvent.AllowDespawn event;
        (event = allowDespawn).initAllowDespawn(entity);
        MinecraftForge.EVENT_BUS.postF(event);
        return event.getResult();
    }


    public boolean doSpecialSpawn(EntityLiving entity, float x, float y, float z) {
        LivingSpawnEvent.SpecialSpawn ev;
        MinecraftForge.EVENT_BUS.postF(ev = specialSpawn.initSpecialSpawn(entity, this, x, y, z));
        return ev.isCanceled() && !entity.isDead;
    }


    public Event.Result canEntitySpawn(EntityLiving entity, float x, float y, float z) {
        if (entity == null) return Event.Result.DEFAULT;
        checkSpawnEvemt.initCheckSpawn(entity, this, x, y, z, null);
        MinecraftForge.EVENT_BUS.postF(checkSpawnEvemt);
        return checkSpawnEvemt.getResult();
    }


    public BlockEvent.EntityPlaceEvent onBlockPlace(Entity entity, BlockSnapshot snapshot, EnumFacing direction) {
        IBlockState placedAgainst = snapshot.getWorld().getBlockState(snapshot.getPos().offset(direction.getOpposite()));
        blockPlaceEvent.initEntityPlaceEvent(snapshot, placedAgainst, entity);
        MinecraftForge.EVENT_BUS.postF(blockPlaceEvent);
        return blockPlaceEvent;
    }


    private final BlockEvent.NeighborNotifyEvent nNotifyEvent
            = ElcaCV.ENABLE_NEIGHBOR_NOTIFY_EVENT?new BlockEvent.NeighborNotifyEvent():null;

    public BlockEvent.NeighborNotifyEvent onNeighborNotify(BlockPos pos, IBlockState state,
                                                                  EnumSet<EnumFacing> notifiedSides,
                                                                  boolean forceRedstoneUpdate) {

        nNotifyEvent.initNeighborNotifyEvent(this, pos, state, forceRedstoneUpdate, notifiedSides);
        MinecraftForge.EVENT_BUS.postF(nNotifyEvent);
        return nNotifyEvent;
    }


    public BlockEvent.NeighborNotifyEvent onNeighborNotify(BlockPos pos, IBlockState state,
                                                           boolean forceRedstoneUpdate, EnumFacing... faces) {

        nNotifyEvent.initNeighborNotifyEvent(this, pos, state, forceRedstoneUpdate, faces);
        MinecraftForge.EVENT_BUS.postF(nNotifyEvent);
        return nNotifyEvent;
    }


    public boolean getMobGrifering(Entity entity){
        mobGriefingEvent.setEntity(entity);
        mobGriefingEvent.setResult(Event.Result.DEFAULT);
        MinecraftForge.EVENT_BUS.postF(mobGriefingEvent);
        Event.Result result = mobGriefingEvent.getResult();
        return result == Event.Result.DEFAULT ? getGameRules().mobGriefing.getBoolean() : result == Event.Result.ALLOW;
    }


    public void onLivingJump(EntityLivingBase entity){
        MinecraftForge.EVENT_BUS.postF(jumpEvent.initLiving(entity));
    }


    public boolean onLivingUpdate(EntityLivingBase entity){
        return updateEvent.post(entity);
    }


    public boolean onLivingAttack(EntityLivingBase entity, DamageSource src, float amount) {
        return entity instanceof EntityPlayer || !MinecraftForge.EVENT_BUS.post(attackEvent.initAttack(entity, src, amount));
    }


    public float onLivingHurt(EntityLivingBase entity, DamageSource src, float amount) {
        return (MinecraftForge.EVENT_BUS.post(hurtEvent.initHurt(entity, src, amount)) ? 0 : hurtEvent.getAmount());
    }


    public float onLivingDamage(EntityLivingBase entity, DamageSource src, float amount) {
        return (MinecraftForge.EVENT_BUS.post(damageEvent.initDamage(entity, src, amount)) ? 0 : damageEvent.getAmount());
    }


    //player events
    public  final PlayerEvent.StartTracking startTrackingEvent = new PlayerEvent.StartTracking().forBus(MinecraftForge.EVENT_BUS);
    public  final PlayerEvent.StopTracking  stopTrackingEvent  = new PlayerEvent.StopTracking().forBus(MinecraftForge.EVENT_BUS);

    private final PlayerEvent.BreakSpeed breakSpeedEvent = new PlayerEvent.BreakSpeed().forBus(MinecraftForge.EVENT_BUS);
    private final PlayerEvent.HarvestCheck harvestCheck = new PlayerEvent.HarvestCheck().forBus(MinecraftForge.EVENT_BUS);


    public float getBreakSpeed(EntityPlayer player, IBlockState state, float original, BlockPos pos) {
        return breakSpeedEvent.post(player, state, original, pos) ? -1 : breakSpeedEvent.getNewSpeed();
    }


    public boolean doPlayerHarvestCheck(EntityPlayer player, IBlockState state, boolean success) {
        return harvestCheck.post(player, state, success);
    }


    //to merge ==============================================================================================================
    protected final BlockPos.MutableBlockPos tempPooledLocalPos = new BlockPos.MutableBlockPos();

    //common
    public boolean isBlockFullCube(BlockPos pos) {
        if(pos.getY() < 0 || pos.getY() > 255)return false;
        AxisAlignedBB axisalignedbb = localSelector.getStateUnchecked(pos.getX(), pos.getY(), pos.getZ()).getCollisionBoundingBox(this, pos);
        return axisalignedbb != Block.NULL_AABB && axisalignedbb.getAverageEdgeLength() >= 1.0D;
    }


    @Override
    public boolean isAirBlock(@NotNull BlockPos pos) {
        if(isOutsideBuildHeight(pos))return true;
        IBlockState state = getChunk(pos).getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }


    public boolean isBlockNormalCube(@NotNull BlockPos pos, boolean def) {
        if (pos.getY() < 0|| pos.getY() > 255) return false;
        else {
            Chunk chunk1 = localSelector.getLoadedE(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk1 != null && !chunk1.isEmpty()) {
                IBlockState state = chunk1.getBlockStateUnchecked(pos.getX(), pos.getY(), pos.getZ());
                return state.getBlock().isNormalCube(state, localSelector, pos);
            }
            return def;
        }
    }


    //redstone
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        IBlockState state = localSelector.getState(pos);
        return state.getBlock().shouldCheckWeakPower(state, this, pos, facing) ?
                getStrongPowerDelta(new BlockPosDelta(pos)) : state.getWeakPower(localSelector, pos, facing);
    }


    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return localSelector.getState(pos).getStrongPower(localSelector, pos, direction);
    }


    private int getStrongPowerDelta(BlockPosDelta pos) {
        int max = localSelector.getState(pos.dY(-1)).getStrongPower(localSelector, pos, EnumFacing.DOWN);

        if (max >= 15) {
            return max;
        } else {
            int cur = localSelector.getState(pos.dY(1)).getStrongPower(localSelector, pos, EnumFacing.UP);

            if (cur >= 15) {
                return cur;
            } else {
                if(cur>max)max = cur;
                cur = localSelector.getState(pos.dZ(-1)).getStrongPower(localSelector, pos, EnumFacing.NORTH);

                if (cur >= 15) {
                    return cur;
                } else {
                    if(cur>max)max = cur;
                    cur = localSelector.getState(pos.dZ(1)).getStrongPower(localSelector, pos, EnumFacing.SOUTH);

                    if (cur >= 15) {
                        return cur;
                    } else {
                        if(cur>max)max = cur;
                        cur = localSelector.getState(pos.dX(-1)).getStrongPower(localSelector, pos, EnumFacing.WEST);

                        if (cur >= 15) {
                            return cur;
                        } else {
                            cur = localSelector.getState(pos.dX(1)).getStrongPower(localSelector, pos, EnumFacing.EAST);
                            return max>cur?max:cur;
                        }
                    }
                }
            }
        }
    }


    public void neighborChanged(BlockPos pos, final Block blockIn, BlockPos fromPos) {
        localSelector.getState(pos).neighborChanged(this, pos, blockIn, fromPos);
    }


    //area
    public boolean isAreaLoaded(BlockPos center, int radius) {
        if (center.getY() - radius < 255) {
            int     xStart = (center.getX() - radius) >> 4,
                    xEnd   = (center.getX() + radius) >> 4,
                    zStart = (center.getZ() - radius) >> 4,
                    zEnd   = (center.getZ() + radius) >> 4;

            for (;xStart <= xEnd; ++xStart) {
                for (;zStart <= zEnd; ++zStart)
                    if (!chunkProviderServer.chunkExists(xStart, zStart)) return false;
            }
            return true;
        } else return false;
    }


    private boolean isAreaLoaded0(int xStart, int zStart, int xEnd, int zEnd) {
        zStart = zStart >> 4;
        xEnd = xEnd >> 4;
        zEnd = zEnd >> 4;
        for (int i = xStart >> 4; i <= xEnd; ++i)
            for (int j = zStart; j <= zEnd; ++j)
                if (!this.isChunkLoaded(i, j, true)) return false;
        return true;
    }


    public int getLight(int x, int y, int z, boolean checkNeighbors) {
        if (x >= -30000000 && y >= -30000000 && z < 30000000 && x < 30000000)
            return getLight0(x, y, z, checkNeighbors);
        else return 15;
    }


    //ligh
    public int getLight(BlockPos pos, boolean checkNeighbors){
        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000)
            return getLight0(pos.getX(), pos.getY(), pos.getZ(), checkNeighbors);
        else return 15;
    }


    private final BlockPos.MutableBlockPos lightTemp = new BlockPos.MutableBlockPos();
    private int getLight0(int posX, int posY, int posZ, boolean checkNeighbors) {
        if (checkNeighbors && localSelector.getState(posX, posY, posZ).useNeighborBrightness()) {
            int i1 = this.getLight0U(     posX  , posY+1,     posZ  );
            int i =  this.getLight0U(posX+1,      posY  ,     posZ  );
            int j =  this.getLight0U(posX-1,      posY  ,     posZ  );
            int k =  this.getLight0U(     posX  ,      posY  ,posZ+1);
            int l =  this.getLight0U(     posX  ,      posY  ,posZ-1);

            if (i > i1) i1 = i;
            if (j > i1) i1 = j;
            if (k > i1) i1 = k;
            if (l > i1) i1 = l;

            return i1;
        } else if (posY < 0) {
            return 0;
        } else {
            if (posY > 255) posY = 255;
            return localSelector.getChunk(posX, posZ).getLightSubtracted(posX, posY, posZ, getSkylightSubtracted());
        }
    }


    private int getLight0U(int posX, int posY, int posZ) {
        if (posY < 0) return 0;
        if (posY > 255) posY = 255;
        return localSelector.getChunk(posX, posZ).getLightSubtracted(posX, posY, posZ, getSkylightSubtracted());
    }


    //chunk-iter functions
    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        if(this.isRemote)return;
        int bX = pos.getX(), bY = pos.getY(), bZ = pos.getZ();
        if(bY>0) {
            BlockPos.MutableBlockPos mut = tempPooledLocalPos;
            int nX, nZ, buf;
            Chunk chunk = localSelector.getChunk(nX = (bX+1) >>4, nZ = bZ>>4);
            IBlockState iblockstate;

            iblockstate = chunk.getBlockState(mut.setPos(bX + 1, bY, bZ));
            iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);

            if((buf=(bX-1)>>4)!=nX){
                chunk = getChunk(nX = buf, nZ);
            }

            iblockstate = chunk.getBlockState(mut.setPos(bX - 1, bY, bZ));
            iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);

            if((buf=(bX)>>4)!=nX || (bZ+1)>>4 != nZ){
                chunk = getChunk(nX = buf, nZ = (bZ+1)>>4);
            }

            iblockstate = chunk.getBlockState(mut.setPos(bX, bY, bZ + 1));
            iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);

            if((buf=(bZ-1)>>4)!=nZ){
                chunk = getChunk(nX, nZ = buf);
            }

            iblockstate = chunk.getBlockState(mut.setPos(bX, bY, bZ - 1));
            iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);

            if((buf=(bZ)>>4)!=nZ){
                chunk = getChunk(nX, buf);
            }

            if(bY>1) {
                iblockstate = chunk.getBlockState(mut.setPos(bX, bY - 1, bZ));
                iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);

                if(bY<256) {
                    iblockstate = chunk.getBlockState(mut.setPos(bX, bY + 1, bZ));
                    iblockstate.getBlock().observedNeighborChange(iblockstate, this, mut, blockType, pos);
                }
            }
        }
    }


    @Keep
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        int minY = MathHelper.floor(bb.minY);
        int maxY = MathHelper.ceil(bb.maxY);
        if(minY<0 || maxY > 255)return false;

        int maxX = MathHelper.ceil(bb.maxX);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        BlockPos.MutableBlockPos pos = tempPooledLocalPos;
        ChunkSelector selector = localSelector;
        IBlockState state;
        for (int xPos = MathHelper.floor(bb.minX); xPos < maxX; ++xPos)
            for (int yPos = minY; yPos < maxY; ++yPos) {
                for (int zPos = minZ; zPos < maxZ; ++zPos) {
                    state = selector.getStateUnchecked(xPos, yPos, zPos);
                    pos.setPosB(xPos, yPos, zPos);
                    Boolean result = state.getBlock().isAABBInsideMaterial(this, pos, bb, materialIn);
                    if (result != null) {
                        if (!result) continue;
                        return true;
                    }
                    if (state.getMaterial() == materialIn) return true;
                }
            }
        return false;
    }


    @Keep
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        int yPos, maxY;
        if((yPos = MathHelper.floor(bb.minY)) < 0 || (maxY = MathHelper.ceil(bb.maxY)) > 255)return false;
        int xPos, maxX, zPos, maxZ;

        if (this.isAreaLoaded0((xPos = MathHelper.floor(bb.minX)), (zPos = MathHelper.floor(bb.minZ)), (maxX = MathHelper.ceil(bb.maxX)),
                (maxZ = MathHelper.ceil(bb.maxZ)))) {
            final ChunkSelector access = localSelector;
            final BlockPos.MutableBlockPos mut = tempPooledLocalPos;
            for (Block block; xPos < maxX; ++xPos)
                for (; zPos < maxZ; ++zPos)
                    for (; yPos < maxY; ++yPos) {
                        block = access.getStateUnchecked(xPos, yPos, zPos).getBlock();
                        if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA)
                            return true;
                        else if (block.isBurning(this, mut.setPos(xPos, yPos, zPos)))
                            return true;
                    }
        }
        return false;
    }


    @Keep
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        int yPos, maxY;
        if((yPos = MathHelper.floor(bb.minY)) < 0 || (maxY = MathHelper.ceil(bb.maxY)) > 255)return false;

        int xPos, maxX, zPos, maxZ;

        if (!this.isAreaLoaded0(xPos = MathHelper.floor(bb.minX), zPos = MathHelper.floor(bb.minZ), maxX = MathHelper.ceil(bb.maxX), maxZ = MathHelper.ceil(bb.maxZ))) {
            return false;
        } else {
            boolean flag = false;
            Vec3d vec3d = Vec3d.ZERO;
            IBlockState state;
            Block block;
            Boolean result;
            ChunkSelector access = localSelector;

            for (final BlockPos.MutableBlockPos pos = tempPooledLocalPos; xPos < maxX; ++xPos) {
                for (; zPos < maxZ; ++zPos) {
                    Chunk chunk = access.getChunkE(xPos>>4,zPos>>4);
                    for (; yPos < maxY; ++yPos) {
                        block = (state = chunk.getBlockStateUnchecked(xPos, yPos, zPos)).getBlock();

                        result = block.isEntityInsideMaterial(access, pos.setPos(xPos, yPos, zPos), state, entityIn, maxY, materialIn, false);
                        if (result != null && result) {
                            // Forge: When requested call blocks modifyAcceleration method,
                            // and more importantly cause this method to return true,
                            // which results in an entity being "inWater"
                            flag = true;
                            vec3d = block.modifyAcceleration(this, pos, entityIn, vec3d);
                            continue;
                        } else if (result != null && !result) continue;

                        if (state.getMaterial() == materialIn) {
                            double d0 = (float)(yPos + 1) - BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));

                            if ((double)maxY >= d0) {
                                flag = true;
                                vec3d = block.modifyAcceleration(this, pos, entityIn, vec3d);
                            }
                        }
                    }
                }
            }
            //accelUsed = false;
            if (vec3d.length() > 0.0D && entityIn.isPushedByWater()) {
                vec3d = vec3d.normalize();
                entityIn.motionX += vec3d.x * 0.014D;
                entityIn.motionY += vec3d.y * 0.014D;
                entityIn.motionZ += vec3d.z * 0.014D;
            }
            return flag;
        }
    }


    @Keep
    public void getCollisionBoxesFalse(@Nullable Entity entityIn, AxisAlignedBB aabb, AABBList outList) {
        int minX = MathHelper.floor(aabb.minX) - 1;
        int maxX = MathHelper.ceil(aabb.maxX) + 1;
        int minY = MathHelper.floor(aabb.minY) - 1;
        if (minY < 0)minY = 0;
        int maxY = MathHelper.ceil(aabb.maxY) + 1;
        if (maxY > 255)maxY = 255;
        int minZ = MathHelper.floor(aabb.minZ) - 1;
        int maxZ = MathHelper.ceil(aabb.maxZ) + 1;

        boolean flag1 = entityIn == null || isInsideWorldBorder(entityIn);
        ChunkSelector selector = localSelector;
        Chunk chunk;
        BlockPos.MutableBlockPos mut = tempPooledLocalPos;

        for (int xPos = minX; xPos < maxX; ++xPos) {
            final boolean flag2 = xPos == minX || xPos == maxX - 1;
            for (int zPos = minZ; zPos < maxZ; ++zPos) {
                final boolean flag3 = zPos == minZ || zPos == maxZ - 1;

                if ((!flag2 || !flag3) && (chunk = selector.getLoadedE(xPos >> 4, zPos >> 4)) != null)
                    for (int yPos = minY; yPos < maxY; ++yPos)
                        if (!flag2 && !flag3 || yPos != maxY - 1) {

                            if (flag1) {
                                chunk.getBlockStateUnchecked(xPos, yPos, zPos)
                                        .addCollisionBoxToListF(selector, mut.setPos(xPos, yPos, zPos), aabb, outList);
                            } else {
                                if(isInWorldBorder(xPos, zPos)){
                                    chunk.getBlockStateUnchecked(xPos, yPos, zPos)
                                            .addCollisionBoxToListF(selector, mut.setPos(xPos, yPos, zPos), aabb, outList);
                                } else {
                                    BlockIDIndexes.STONE_STATE
                                            .addCollisionBoxToListF(selector, mut.setPos(xPos, yPos, zPos), aabb, outList);
                                }
                            }
                        }
            }
        }
    }


    public void getAndApplyCollision(@Nullable Entity entityIn, AxisAlignedBB aabb, AABBList outList) {
        int minX = MathHelper.floor(aabb.minX) - 1;
        int maxX = MathHelper.ceil(aabb.maxX) + 1;
        int minY = MathHelper.floor(aabb.minY) - 1;
        if (minY < 0)minY = 0;
        int maxY = MathHelper.ceil(aabb.maxY) + 1;
        if (maxY > 255)maxY = 255;
        int minZ = MathHelper.floor(aabb.minZ) - 1;
        int maxZ = MathHelper.ceil(aabb.maxZ) + 1;

        boolean flag  = entityIn != null && entityIn.isOutsideBorder(),
                flag1 = entityIn != null && isInsideWorldBorder(entityIn);
        ChunkSelector selector = localSelector;
        Chunk chunk;

        for (int xPos = minX; xPos < maxX; ++xPos) {
            final boolean flag2 = xPos == minX || xPos == maxX - 1;
            for (int zPos = minZ; zPos < maxZ; ++zPos) {
                final boolean flag3 = zPos == minZ || zPos == maxZ - 1;

                if ((!flag2 || !flag3) && (chunk = selector.getLoadedE(xPos >> 4, zPos >> 4)) != null)
                    for (int yPos = minY; yPos < maxY; ++yPos)
                        if (!flag2 && !flag3 || yPos != maxY - 1) {
                            if (entityIn != null && flag == flag1) entityIn.setOutsideBorder(!flag1);

                            if (flag1 && !isInWorldBorder(xPos, zPos)) {
                                BlockIDIndexes.STONE_STATE
                                        .addCollisionBoxToListF(selector, tempPooledLocalPos.setPos(xPos, yPos, zPos), aabb, outList);
                            } else {
                                chunk.getBlockStateUnchecked(xPos, yPos, zPos)
                                        .addCollisionBoxToListF(selector, tempPooledLocalPos.setPos(xPos, yPos, zPos), aabb, outList);
                            }
                        }
            }
        }
    }


    @Keep
    protected boolean getCollisionBoxesTrue(@Nullable Entity entityIn, AxisAlignedBB aabb, AABBList outList) {
        if(ElcaCV.ENABLE_COLLISION_EVENTS)
            MinecraftForge.EVENT_BUS.post(collisionEvent.init(entityIn, aabb, outList));
        if(!outList.isEmptyC())return true;

        int minX = MathHelper.floor(aabb.minX) - 1;
        int maxX = MathHelper.ceil(aabb.maxX) + 1;
        int minY = MathHelper.floor(aabb.minY) - 1;
        if (minY < 0)minY = 0;
        int maxY = MathHelper.ceil(aabb.maxY) + 1;
        if (maxY > 255)maxY = 255;
        int minZ = MathHelper.floor(aabb.minZ) - 1;
        int maxZ = MathHelper.ceil(aabb.maxZ) + 1;

        boolean flag = entityIn != null && entityIn.isOutsideBorder(),
                flag1 = entityIn != null && this.isInsideWorldBorder(entityIn),
                flag2, flag3;
        ChunkSelector selector = localSelector;
        Chunk chunk;

        for (int xPos = minX; xPos < maxX; ++xPos)
            for (int zPos = minZ; zPos < maxZ; ++zPos) {
                flag2 = xPos == minX || xPos == maxX - 1;
                flag3 = zPos == minZ || zPos == maxZ - 1;

                if ((!flag2 || !flag3) && (chunk = selector.getLoadedE(xPos>>4, zPos>>4))!=null)
                    for (int yPos = minY; yPos < maxY; ++yPos)
                        if (!flag2 && !flag3 || yPos != maxY - 1) {
                            if (true) {
                                if (xPos < -30000000 || xPos >= 30000000 || zPos < -30000000 || zPos >= 30000000) return true;

                            } else if (entityIn != null && flag == flag1) entityIn.setOutsideBorder(!flag1);

                            ((!true && flag1 && !isInWorldBorder(xPos, zPos))?
                                    BlockIDIndexes.STONE_STATE : chunk.getBlockStateUnchecked(xPos, yPos, zPos))

                                    .addCollisionBoxToListF(selector, tempPooledLocalPos.setPos(xPos, yPos, zPos), aabb, outList);


                            if(ElcaCV.ENABLE_COLLISION_EVENTS){
                                MinecraftForge.EVENT_BUS.postF(collisionEvent.init(entityIn, aabb, outList));
                                if(!outList.isEmptyC())return true;
                            }else return true;
                        }
            }
        return !outList.isEmptyC();
    }


    @Keep
    public <T extends Entity> void getEntitiesWithinAABBNoCache(List<T> list, Class <? extends T > clazz,
                                                                AxisAlignedBB aabb, @Nullable Predicate<? super T > filter) {
        int maxX = MathHelper.ceil(aabb.maxX + MAX_ENTITY_RADIUS)>>4;
        int minZ = MathHelper.floor(aabb.minZ - MAX_ENTITY_RADIUS)>>4;
        int maxZ = MathHelper.ceil(aabb.maxZ + MAX_ENTITY_RADIUS)>>4;

        Chunk chunk;

        for (int xPos = MathHelper.floor(aabb.minX - MAX_ENTITY_RADIUS)>>4; xPos < maxX; ++xPos)
            for (int zPos = minZ; zPos < maxZ; ++zPos)
                if ((chunk = chunkProviderServer.getLoadedChunk(xPos, zPos)) != null)
                    chunk.getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
    }


    //collisions, no cache
    protected void getEntitiesInAABBexcludingNoCacheLimited(EntityList<Entity> list, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate <? super Entity > predicate, int lim) {
        if(lim==0)return;
        int k2 = MathHelper.floor(boundingBox.maxX + MAX_ENTITY_RADIUS)>>4,
                l2 = MathHelper.floor(boundingBox.minZ - MAX_ENTITY_RADIUS)>>4,
                i3 = MathHelper.floor(boundingBox.maxZ + MAX_ENTITY_RADIUS)>>4;

        Chunk chunk;

        for (int xPos = MathHelper.floor(boundingBox.minX - MAX_ENTITY_RADIUS)>>4; xPos <= k2; ++xPos)
            for (int zPos = l2; zPos <= i3; ++zPos)
                if ((chunk = localSelector.getLoadedE(xPos, zPos)) != null)
                    chunk.getEntitiesWithinAABBForEntityLimitedC(entityIn, boundingBox, list, predicate, lim);
    }


    public void getEntitiesInAABBexcludingNoCache(EntityList<Entity> list, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate <? super Entity > predicate) {
        int k2 = MathHelper.floor(boundingBox.maxX + MAX_ENTITY_RADIUS)>>4;
        int l2 = MathHelper.floor(boundingBox.minZ - MAX_ENTITY_RADIUS)>>4;
        int i3 = MathHelper.floor(boundingBox.maxZ + MAX_ENTITY_RADIUS)>>4;

        Chunk chunk;

        for (int xPos = MathHelper.floor(boundingBox.minX - MAX_ENTITY_RADIUS)>>4; xPos <= k2; ++xPos)
            for (int zPos = l2; zPos <= i3; ++zPos)
                if ((chunk = localSelector.getLoadedE(xPos, zPos)) != null)
                    chunk.getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
    }
}