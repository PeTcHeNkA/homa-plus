package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataCallableSave;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.WorldSpecificSaveHandler;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.api.MeanTicker;
import plus.fserver.api.ElcaWorldServer;
import plus.fserver.ElcaCV;
import plus.fserver.server.EntityCollider;
import plus.fserver.server.chunk.io.TChunkTask;
import plus.fserver.server.chunk.io.TQueuedChunk;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ChunkIterator;
import plus.fserver.utls.collections.EntityRemSwapList;
import plus.fserver.utls.math.MathUtls;
import plus.fserver.world.CommonWorldEntitySpawner;
import plus.fserver.world.NavigationCache;
import plus.tson.utl.Te4HashSet;


public class WorldServer extends World implements IThreadListener, ElcaWorldServer, MeanTicker {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MinecraftServer server;
    private EntityTracker entityTracker;
    private final PlayerChunkMap playerChunkMap;
    private final Set<NextTickListEntry> pendingTickListEntriesHashSet = new Te4HashSet<>();
    private final TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet = new TreeSet<>(
            new ConcurrentSkipListSet<>()
    );

    private final Map<UUID, Entity> entitiesByUuid = new HashMap<>();
    public boolean disableLevelSaving;
    private boolean allPlayersSleeping;
    private int updateEntityTick;
    private final Teleporter worldTeleporter;
    @Keep
    private final WorldEntitySpawner entitySpawner;
    protected final VillageSiege villageSiege = new VillageSiege(this);
    private final WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
    private int blockEventCacheIndex;
    private final List<NextTickListEntry> pendingTickListEntriesThisTick = new ArrayList<>();

    /** Stores the recently processed (lighting) chunks */
    @Keep
    public List<Teleporter> customTeleporters = new ArrayList<>();

    private static final long spawnOnSetTickRate = 400L;

    private final ConcurrentHashMap<TQueuedChunk, TChunkTask> _chunkTasks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Runnable> _syncTasks = new ConcurrentLinkedQueue<>();

    public WorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, info, DimensionManager.createProviderFor(dimensionId), profilerIn, false);
        if(ElcaCV.DEBUG_NO_SERVER){
            throw new RuntimeException("DEBUG NO SERVER IS ENABLED!");
        }
        this.server = server;
        this.entityTracker = new EntityTracker(this).init();
        this.playerChunkMap = new PlayerChunkMap(this);

        perWorldStorage = new MapStorage(new WorldSpecificSaveHandler(this, saveHandlerIn));
        this.worldTeleporter = new Teleporter(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.getWorldBorder().setSize(server.getMaxWorldSize());

        DimensionManager.setWorld(dimensionId, this, server);

        effectiveAcces = localSelector;
        entitySpawner = makeWorldSpawner();
    }


    @Keep
    protected WorldEntitySpawner makeWorldSpawner(){
        return new CommonWorldEntitySpawner();
    }


    protected ChunkSelector initSelector(){
        int providerDim = this.provider.getDimension();
        this.provider.setWorld(this);
        this.provider.setDimension(providerDim);

        this.chunkProvider = this.createChunkProvider();
        this.chunkProviderServer = (ChunkProviderServer) chunkProvider;
        return new ChunkSelector(this);
    }


    @Override
    public World init() {
        this.mapStorage = new MapStorage(this.saveHandler);
        String s = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, s);

        if (villagecollection == null) {
            this.villageCollection = new VillageCollection(this);
            this.perWorldStorage.setData(s, this.villageCollection);
        } else {
            this.villageCollection = villagecollection;
            this.villageCollection.setWorldsForAll(this);
        }

        this.worldScoreboard = new ServerScoreboard(this.server);
        ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardsavedata == null) {
            scoreboardsavedata = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", scoreboardsavedata);
        }

        scoreboardsavedata.setScoreboard(this.worldScoreboard);
        ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(scoreboardsavedata));
        this.lootTable = new LootTableManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "loot_tables"));
        this.advancementManager = new AdvancementManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "advancements"));
        this.functionManager = new FunctionManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "functions"), this.server);
        this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
        this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
        this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
        this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
        this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());

        if (this.worldInfo.getBorderLerpTime() > 0L) {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
        } else {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
        }

        this.initCapabilities();
        return this;
    }


    @Nullable
    public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos) {
        List<Biome.SpawnListEntry> list = getSpawnList(creatureType, pos);
        return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(this.rand, list) : null;
    }


    @Nullable
    public final List<Biome.SpawnListEntry> getSpawnList(EnumCreatureType type, BlockPos pos){
        List<Biome.SpawnListEntry> list = chunkProviderServer.getPossibleCreatures(type, pos);
        return ForgeEventFactory.getPotentialSpawns(this, type, pos, list);
    }


    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, Biome.SpawnListEntry entry, BlockPos pos) {
        List<Biome.SpawnListEntry> list = getSpawnList(creatureType, pos);
        return list != null && !list.isEmpty() && list.contains(entry);
    }


    @Override
    public void updateAllPlayersSleepingFlag() {
        this.allPlayersSleeping = false;
        if (!this.playerEntities.isEmpty()) {
            int i = 0;
            int j = 0;
            for (EntityPlayer entityplayer : this.playerEntities) {
                if (entityplayer.isSpectator()) {
                    ++i;
                } else if (entityplayer.isPlayerSleeping()) {
                    ++j;
                }
            }
            this.allPlayersSleeping = j > 0 && j >= this.playerEntities.size() - i;
        }
    }


    protected void wakeAllPlayers() {
        this.allPlayersSleeping = false;
        for (EntityPlayer entityplayer : this.playerEntities.stream().filter(EntityPlayer::isPlayerSleeping).collect(Collectors.toList())) {
            entityplayer.wakeUpPlayer(false, false, true);
        }
        if (this.getGameRules().doWeatherCycle.getBoolean()) {
            this.resetRainAndThunder();
        }
    }


    private void resetRainAndThunder() {
        this.provider.resetRainAndThunder();
    }


    public boolean areAllPlayersAsleep() {
        if (this.allPlayersSleeping) {
            for (EntityPlayer entityplayer : this.playerEntities) {
                if (!entityplayer.isSpectator() && !entityplayer.isPlayerFullyAsleep()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void setInitialSpawnLocation() {
        if (this.worldInfo.getSpawnY() <= 0) {
            this.worldInfo.setSpawnY(this.getSeaLevel() + 1);
        }
        int i = this.worldInfo.getSpawnX();
        int j = this.worldInfo.getSpawnZ();
        int k = 0;

        while (this.getGroundAboveSeaLevel(new BlockPos(i, 0, j)).getMaterial() == Material.AIR) {
            i += this.rand.nextInt(8) - this.rand.nextInt(8);
            j += this.rand.nextInt(8) - this.rand.nextInt(8);
            ++k;

            if (k == 10000) {
                break;
            }
        }
        this.worldInfo.setSpawnX(i);
        this.worldInfo.setSpawnZ(j);
    }


    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return chunkProviderServer.chunkExists(x, z);
    }


    protected void playerCheckLight() {
        if (!this.playerEntities.isEmpty()) {
            int i = this.rand.nextInt(this.playerEntities.size());
            EntityPlayer entityplayer = this.playerEntities.get(i);
            int j = MathHelper.floor(entityplayer.posX) + this.rand.nextInt(11) - 5;
            int k = MathHelper.floor(entityplayer.posY) + this.rand.nextInt(11) - 5;
            int l = MathHelper.floor(entityplayer.posZ) + this.rand.nextInt(11) - 5;
            this.checkLight(new BlockPos(j, k, l));
        }
    }


    private final Predicate<EntityLivingBase> ajastNear = entity -> entity != null && entity.isEntityAlive() && WorldServer.this.canSeeSky(entity);


    protected BlockPos adjustPosToNearbyEntity(BlockPos pos) {
        BlockPos blockpos = this.getPrecipitationHeight(pos);
        AxisAlignedBB aabb = commonWorldEntityAABB;

        aabb.init(
                blockpos.getX()-3, blockpos.getY()-3, blockpos.getZ()-3,
                blockpos.getX()+3, this.getHeight()+3, blockpos.getZ()+3
        );

        List<EntityLivingBase> list = this.getEntitiesWithinAABB(EntityLivingBase.class, aabb, ajastNear);

        if (!list.isEmpty())
            return list.get(this.rand.nextInt(list.size())).getPosition();
        else {
            if (blockpos.getY() == -1) {
                BlockPos.unsafeEdit(blockpos, blockpos.getX(), 1, blockpos.getZ());
            }
            return blockpos;
        }
    }


    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockType);
        return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
    }


    @Override
    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blk);
        return this.pendingTickListEntriesHashSet.contains(nextticklistentry);
    }


    @Override
    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        this.updateBlockTick(pos, blockIn, delay, 0);
    }


    @Override
    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        Material material = blockIn.getDefaultState().getMaterial();

        if (this.scheduledUpdatesAreImmediate > 0 && material != Material.AIR) {
            --this.scheduledUpdatesAreImmediate;
            if (blockIn.requiresUpdates()) {
                //Keeping here as a note for future when it may be restored.
                //boolean isForced = false;// getPersistentChunks().containsKey(new ChunkPos(pos));
                int range = 8;
                if (this.isAreaLoaded(pos.add(-range, -range, -range), pos.add(range, range, range))) {
                    IBlockState iblockstate = this.getBlockState(pos);

                    if (iblockstate.getMaterial() != Material.AIR && iblockstate.getBlock() == blockIn) {
                        iblockstate.getBlock().updateTick(this, pos, iblockstate, this.rand);
                    }
                }
                return;
            }
            delay = 1;
        }
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);

        if (this.isBlockLoaded(pos)) {
            if (material != Material.AIR) {
                nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
                nextticklistentry.setPriority(priority);
            }
            if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
                this.pendingTickListEntriesHashSet.add(nextticklistentry);
                this.pendingTickListEntriesTreeSet.add(nextticklistentry);
            }
        }
    }


    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        if (blockIn == null) return;
        //Forge: Prevent null blocks from ticking, can happen if blocks are removed in old worlds.
        // TODO: Fix real issue causing block to be null.
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
        nextticklistentry.setPriority(priority);
        Material material = blockIn.getDefaultState().getMaterial();

        if (material != Material.AIR) {
            nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
        }

        if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
            this.pendingTickListEntriesHashSet.add(nextticklistentry);
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
        }
    }


    private final EntityCollider collider = new EntityCollider(this);
    @Override
    public void updateEntities() {
        if (this.playerEntities.isEmpty()) {
            if (this.updateEntityTick++ >= 300) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }
        this.provider.onWorldUpdateEntities();
        super.updateEntities();
    }


    private void collideAll(){
        EntityCollider collider = this.collider;
        collider.begin(getGameRules().maxEntityCramming.getInt());
        for (Chunk chunk:this.playerChunkMap.getChunkIterator(ElcaConfig.CHUNK_TICK_RADIUS_1)){
            collider.collideAll(chunk);
        }
        collider.complete();
    }


    protected void unloadEntites(){
        final List<Entity> toUnload = this.unloadedEntityList;
        final int size = toUnload.size();
        if(size > 0) {
            realEntityList.removeRefSwapAll(toUnload);
            ChunkProviderServer cps = chunkProviderServer;

            for (int i = 0; i < size; ++i) {
                Entity entity = toUnload.get(i);

                if (entity.addedToChunk) {
                    Chunk chunk = cps.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
                    if(chunk != null) chunk.removeEntity(entity);
                }
                onEntityRemoved(entity);
            }
            toUnload.clear();
        }
    }


    private boolean spawnAnimals = true, spawnNpc = true;
    @Override
    public void tickLoadedEntities(){
        if(spawnAnimals != canSpawnAnimals() || spawnNpc != canSpawnNPCs()){
            recheckEntities();
        }
        ChunkProviderServer cps = chunkProviderServer;
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            for (int cursor = 0; cursor < list.sizeC(); ++cursor) {
                Entity entity = list.getC(cursor);
                Entity ridding = entity.getRidingEntity();

                if (ridding != null) {
                    if (!ridding.isDead && ridding.isPassenger(entity))
                        continue;
                    entity.dismountRidingEntity();
                }

                if (!entity.isDead && !(entity instanceof EntityPlayerMP))
                    this.updateEntity(entity);

                if (entity.isDead) {
                    if (entity.addedToChunk) {
                        Chunk chunk = cps.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
                        if(chunk != null) chunk.removeEntity(entity);
                    }
                    list.removeSwap(cursor--);
                    this.onEntityRemoved(entity);
                }
            }
        }
        collideTask.complete();
    }


    private void recheckEntities(){
        boolean spawnAnimals = this.spawnAnimals = canSpawnAnimals();
        boolean spawnNpc     = this.spawnNpc = canSpawnNPCs();
        for (EntityRemSwapList.E15<Entity> list:realEntityList.getRaw()) {
            for (int cursor = 0; cursor < list.sizeC(); ++cursor) {
                Entity entity = list.getC(cursor);
                if(!spawnAnimals){
                    if((entity instanceof EntityAnimal || entity instanceof EntityWaterMob)){
                        entity.setDead();
                    }
                }
                if(!spawnNpc){
                    if(entity instanceof INpc)entity.setDead();
                }
            }
        }
    }


    public void resetUpdateEntityTick() {
        this.updateEntityTick = 0;
    }


    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
        ChunkPos chunkpos = chunkIn.getPos();
        int i = (chunkpos.x << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkpos.z << 4) - 2;
        int l = k + 16 + 2;
        return this.getPendingBlockUpdates(new StructureBoundingBox(i, 0, k, j, 256, l), remove);
    }


    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
        List<NextTickListEntry> list = null;

        for (int i = 0; i < 2; ++i) {
            Iterator<NextTickListEntry> iterator;

            if (i == 0) {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            } else {
                iterator = this.pendingTickListEntriesThisTick.iterator();
            }
            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = iterator.next();
                BlockPos blockpos = nextticklistentry.position;

                if (blockpos.getX() >= structureBB.minX && blockpos.getX() < structureBB.maxX && blockpos.getZ() >= structureBB.minZ && blockpos.getZ() < structureBB.maxZ) {
                    if (remove) {
                        if (i == 0) {
                            this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                        }
                        iterator.remove();
                    }
                    if (list == null) {
                        list = Lists.newArrayList();
                    }
                    list.add(nextticklistentry);
                }
            }
        }
        return list;
    }


    @Override
    public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate) {
        if (forceUpdate && checkCanUpdate) {
            Chunk chunk = localSelector.getLoadedE(entity.chunkCoordX, entity.chunkCoordZ);
            //Chunk chunk = chunkProviderServer.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
            boolean canUpdate;
            if(chunk != null){
                //localSelector.setChunk(chunk);
                canUpdate = chunk.isNotUnloadQueued();
            } else
                canUpdate = canUpdateEvent.post(entity);
            if (!canUpdate) return;
        }

        entity.lastTickPosX      = entity.posX         ;
        entity.lastTickPosY      = entity.posY         ;
        entity.lastTickPosZ      = entity.posZ         ;
        entity.prevRotationYaw   = entity.rotationYaw  ;
        entity.prevRotationPitch = entity.rotationPitch;

        if (forceUpdate && entity.addedToChunk) try {
            ++entity.ticksExisted;

            if (entity.isRiding())         entity.updateRidden();
            else if(!entity.updateBlocked) entity.onUpdate();

        } catch (Throwable exception){
            server.ticker().onError(this, onEntityError(entity, exception));
        }

        if (MathUtls.isInvalidDouble(entity.posX))          entity.posX          = entity.lastTickPosX     ;
        if (MathUtls.isInvalidDouble(entity.posY))          entity.posY          = entity.lastTickPosY     ;
        if (MathUtls.isInvalidDouble(entity.posZ))          entity.posZ          = entity.lastTickPosZ     ;
        if (MathUtls.isInvalidDouble(entity.rotationPitch)) entity.rotationPitch = entity.prevRotationPitch;
        if (MathUtls.isInvalidDouble(entity.rotationYaw))   entity.rotationYaw   = entity.prevRotationYaw  ;

        updateEntityChunkSection(entity, forceUpdate);
    }


    @Override
    protected void updateEntityChunkSection(Entity entity, boolean forceUpdate){
        //  1 / 16 = 0,0625
        int cXpos = MathHelper.floor(entity.posX * 0.0625);
        int cYpos = MathHelper.floor(entity.posY * 0.0625);
        int cZpos = MathHelper.floor(entity.posZ * 0.0625);

        if (!entity.addedToChunk || entity.chunkCoordX != cXpos || entity.chunkCoordY != cYpos || entity.chunkCoordZ != cZpos) {
            ChunkProviderServer cps = chunkProviderServer;
            Chunk chunk;
            if (entity.addedToChunk) {
                chunk = cps.getLoadedChunk(entity.chunkCoordX, entity.chunkCoordZ);
                if(chunk != null) chunk.removeEntityAtIndex(entity, entity.chunkCoordY);
            }
            chunk = cps.getLoadedChunk(cXpos, cZpos);

            if (!entity.setPositionNonDirty() && chunk == null) {
                entity.addedToChunk = false;
            } else {
                if(chunk == null)chunk = cps.provideChunk(cXpos, cZpos);
                chunk.addEntity(entity);
            }
        }

        if (forceUpdate && entity.addedToChunk) {
            for (Entity passenger : entity.getPassengers()) {
                if (!passenger.isDead && passenger.getRidingEntity() == entity) {
                    this.updateEntity(passenger);
                } else {
                    passenger.dismountRidingEntity();
                }
            }
        }
    }


    private boolean canSpawnNPCs() {
        return this.server.getCanSpawnNPCs();
    }


    private boolean canSpawnAnimals() {
        return this.server.getCanSpawnAnimals();
    }


    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos)
    {
        return super.isBlockModifiable(player, pos);
    }


    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return !this.server.isBlockProtected(this, pos, player) && this.getWorldBorder().contains(pos);
    }


    @Override
    public void initialize(WorldSettings settings) {
        if (!this.worldInfo.isInitialized()) {
            try {
                this.createSpawnPosition(settings);
                if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    isDebug = true;
                    this.setDebugWorldSettings();
                }
                super.initialize(settings);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception initializing level");
                try {
                    this.addWorldInfoToCrashReport(crashreport);
                } catch (Throwable ignored) {}

                throw new ReportedException(crashreport);
            }
            this.worldInfo.setServerInitialized(true);
        }
    }


    private void setDebugWorldSettings() {
        this.worldInfo.setMapFeaturesEnabled(false);
        this.worldInfo.setAllowCommands(true);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThundering(false);
        this.worldInfo.setCleanWeatherTime(1000000000);
        this.worldInfo.setWorldTime(6000L);
        this.worldInfo.setGameType(GameType.SPECTATOR);
        this.worldInfo.setHardcore(false);
        this.worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
        this.worldInfo.setDifficultyLocked(true);
        this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
    }


    private void createSpawnPosition(WorldSettings settings) {
        if (!this.provider.canRespawnHere()) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
        } else if (isDebug) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
        } else {
           ForgeEventFactory.onCreateWorldSpawn(this, settings);
        }
    }


    protected void createBonusChest() {
        WorldGeneratorBonusChest chestGen = new WorldGeneratorBonusChest();
        for (int i = 0; i < 10; ++i) {
            int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            BlockPos blockpos = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();

            if (chestGen.generate(this, this.rand, blockpos)) {
                break;
            }
        }
    }


    @Nullable
    public BlockPos getSpawnCoordinate()
    {
        return this.provider.getSpawnCoordinate();
    }


    public void flushToDisk() {
        ChunkProviderServer chunkproviderserver = this.getChunkProvider();
        if (chunkproviderserver.canSave()) {
            chunkproviderserver.flushToDisk();
        }
    }


    //todo java.util.ConcurrentModificationException
    //AsyncChecker checker = new AsyncChecker();
    protected void saveLevel() throws MinecraftException {
        this.checkSessionLock();
        for (WorldServer worldserver : this.server.worlds) {
            if (worldserver instanceof WorldServerMulti) {
                ((WorldServerMulti)worldserver).saveAdditionalData();
            }
        }
        this.worldInfo.setBorderSize(this.getWorldBorder().getDiameter());
        this.worldInfo.getBorderCenterX(this.getWorldBorder().getCenterX());
        this.worldInfo.getBorderCenterZ(this.getWorldBorder().getCenterZ());
        this.worldInfo.setBorderSafeZone(this.getWorldBorder().getDamageBuffer());
        this.worldInfo.setBorderDamagePerBlock(this.getWorldBorder().getDamageAmount());
        this.worldInfo.setBorderWarningDistance(this.getWorldBorder().getWarningDistance());
        this.worldInfo.setBorderWarningTime(this.getWorldBorder().getWarningTime());
        this.worldInfo.setBorderLerpTarget(this.getWorldBorder().getTargetSize());
        this.worldInfo.setBorderLerpTime(this.getWorldBorder().getTimeUntilTarget());
        this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.server.getPlayerList().getHostPlayerData());
        this.mapStorage.saveAllData(this);
        this.perWorldStorage.saveAllData(this);
    }


    @Override
    public boolean spawnEntity(Entity entityIn) {
        return this.canAddEntity(entityIn) ? super.spawnEntity(entityIn) : false;
    }


    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        for (Entity entity : Lists.newArrayList(entityCollection)) {
            if (this.canAddEntity(entity) && !MinecraftForge.EVENT_BUS.post(
                    new EntityJoinWorldEvent(entity, this))) {
                this.realEntityList.addF(entity);
                this.onEntityAdded(entity);
            }
        }
    }


    private boolean canAddEntity(Entity entity) {
        if (entity.isDead) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityList.getKey(entity));
            return false;
        } else {
            UUID uuid = entity.getUniqueID();
            if (this.entitiesByUuid.containsKey(uuid)) {
                Entity current = this.entitiesByUuid.get(uuid);
                if (this.unloadedEntityList.contains(current)) {
                    this.unloadedEntityList.remove(current);
                } else {
                    if (!(entity instanceof EntityPlayer)) {
                        LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityList.getKey(current), uuid.toString());
                        return false;
                    }
                    LOGGER.warn("Force-added player with duplicate UUID {}", (Object)uuid.toString());
                }
                this.removeEntityDangerously(current);
            }
            if(!spawnAnimals){
                if((entity instanceof EntityAnimal || entity instanceof EntityWaterMob)){
                    entity.setDead();
                }
            }
            if(!spawnNpc){
                if(entity instanceof INpc)entity.setDead();
            }
            return true;
        }
    }


    @Override
    public void onEntityAdded(Entity entityIn) {
        super.onEntityAdded(entityIn);
        this.entitiesById.addKey(entityIn.getEntityId(), entityIn);
        this.entitiesByUuid.put(entityIn.getUniqueID(), entityIn);

        Entity[] aentity = entityIn.getParts();

        if (aentity != null) for (Entity entity : aentity)
            this.entitiesById.addKey(entity.getEntityId(), entity);
    }


    @Override
    public void onEntityRemoved(Entity entityIn) {
        super.onEntityRemoved(entityIn);
        this.entitiesById.removeObject(entityIn.getEntityId());
        this.entitiesByUuid.remove(entityIn.getUniqueID());
        Entity[] aentity = entityIn.getParts();

        if (aentity != null) for (Entity entity : aentity)
            this.entitiesById.removeObject(entity.getEntityId());
    }


    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        if (super.addWeatherEffect(entityIn)) {
            sendToAllNearExcept(null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, new SPacketSpawnGlobalEntity(entityIn));
            return true;
        } else {
            return false;
        }
    }


    public void sendPacketToAllPlayers(Packet<?> packetIn) {
        List<EntityPlayer> players = playerEntities;
        if(players.isEmpty())return;

        EntityPlayerMP first = (EntityPlayerMP) players.get(0);
        first.connection.netManager.exec(() -> {
            first.connection.netManager.sendPacketSync(packetIn);
            for (int i = 1; i < players.size(); ++i) {
                ((EntityPlayerMP) players.get(i)).connection.netManager.sendPacketSync(packetIn);
            }
        });
    }


    public void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z, double radius, Packet<?> packetIn) {
        final double radius1 = radius*radius;
        double d0, d1, d2;
        PlayerChunkMapEntry center = this.playerChunkMap.getEntry(MathHelper.floor(x), MathHelper.floor(z));

        if(center == null) {
            List<EntityPlayer> players = playerEntities;
            for (int i = 0; i < players.size(); ++i) {
                EntityPlayer player = players.get(i);
                if (player != except) {
                    d0 = x - player.posX;
                    d1 = y - player.posY;
                    d2 = z - player.posZ;

                    if (d0 * d0 + d1 * d1 + d2 * d2 < radius1) {
                        EntityPlayerMP mp = (EntityPlayerMP) player;

                        final NetworkManager first = mp.connection.netManager;
                        final int finalI = i;
                        first.exec(() -> {
                            first.sendPacketSync(packetIn);
                            double d01, d11, d21;
                            for (int i1 = finalI; i1 < players.size(); ++i1) {
                                EntityPlayer player1 = players.get(i1);
                                if (player1 != except) {
                                    d01 = x - player1.posX;
                                    d11 = y - player1.posY;
                                    d21 = z - player1.posZ;

                                    if (d01 * d01 + d11 * d11 + d21 * d21 < radius1) {
                                        ((EntityPlayerMP) player1).connection.netManager.sendPacketSync(packetIn);
                                    }
                                }
                            }
                        });
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < center.size(); ++i) {
                EntityPlayerMP player = center.get(i);
                if (player != except) {
                    d0 = x - player.posX;
                    d1 = y - player.posY;
                    d2 = z - player.posZ;

                    if (d0 * d0 + d1 * d1 + d2 * d2 < radius1) {
                        final NetworkManager first = player.connection.netManager;
                        final int finalI = i;
                        first.exec(() -> {
                            first.sendPacketSync(packetIn);
                            double d01, d11, d21;
                            for (int i1 = finalI; i1 < center.size(); ++i1) {
                                EntityPlayer player1 = center.get(i1);
                                if (player1 != except) {
                                    d01 = x - player1.posX;
                                    d11 = y - player1.posY;
                                    d21 = z - player1.posZ;

                                    if (d01 * d01 + d11 * d11 + d21 * d21 < radius1) {
                                        ((EntityPlayerMP) player1).connection.netManager.sendPacketSync(packetIn);
                                    }
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }


    @Override
    public void setEntityState(Entity entityIn, byte state) {
        this.getEntityTracker().sendToTrackingAndSelf(entityIn, new SPacketEntityStatus(entityIn, state));
    }


    @Override
    public ChunkProviderServer getChunkProvider() {
        return chunkProviderServer;
    }


    @Override
    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean causesFire, boolean damagesTerrain) {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, causesFire, damagesTerrain);
        if (ElcaCV.ENABLE_EXPLOSION_EVENT &&
                ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!damagesTerrain) {
            explosion.clearAffectedBlockPositions();
        }

        SPacketExplosion packet = null;
        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                if(packet==null){
                    packet = new SPacketExplosion(x, y, z, strength,
                            explosion.getAffectedBlockPositions(),
                            explosion.getPlayerKnockbackMap().get(entityplayer)
                    );
                }
                ((EntityPlayerMP)entityplayer).connection.netManager.sendPacketE(packet);
            }
        }
        return explosion;
    }


    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);
        for (BlockEventData eventData : this.blockEventQueue[this.blockEventCacheIndex])
            if (eventData.equals(blockeventdata)) return;
        this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
    }


    //TODO send this only for self players
    private void sendQueuedBlockEvents() {
        while (!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
            int i = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;

            for (BlockEventData data : this.blockEventQueue[i]) {
                if (this.fireBlockEvent(data)) {
                    sendToAllNearExcept(null, data.getPosition().getX(), data.getPosition().getY(), data.getPosition().getZ(), 64.0D, new SPacketBlockAction(data.getPosition(), data.getBlock(), data.getEventID(), data.getEventParameter()));
                }
            }
            this.blockEventQueue[i].clear();
        }
    }


    private boolean fireBlockEvent(BlockEventData event) {
        IBlockState iblockstate = this.getBlockState(event.getPosition());
        return iblockstate.getBlock() == event.getBlock() ? iblockstate.onBlockEventReceived(this, event.getPosition(), event.getEventID(), event.getEventParameter()) : false;
    }


    public void flush() {
        this.saveHandler.flush();
    }


    @Override
    protected void updateWeather() {
        boolean flag = this.isRaining();
        super.updateWeather();

        if (this.prevRainingStrength != this.rainingStrength) {
            sendPacketToAllPlayers(new SPacketChangeGameState(7, this.rainingStrength));
        }

        if (this.prevThunderingStrength != this.thunderingStrength) {
            sendPacketToAllPlayers(new SPacketChangeGameState(8, this.thunderingStrength));
        }

        /* The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
         * rather than to all players on the server. This is what causes the client-side rain, as the
         * client believes that it has started raining locally, rather than in another dimension.
         */
        if (flag != this.isRaining()) {
            if (flag) {
                sendPacketToAllPlayers(
                        new SPacketChangeGameState(2, 0.0F)
                );
            } else {
                sendPacketToAllPlayers(
                        new SPacketChangeGameState(1, 0.0F)
                );
            }
            sendPacketToAllPlayers(
                    new SPacketChangeGameState(7, this.rainingStrength)
            );
            sendPacketToAllPlayers(
                    new SPacketChangeGameState(8, this.thunderingStrength)
            );
        }
    }


    @Nullable
    @Override
    public MinecraftServer getMinecraftServer() {
        return this.server;
    }


    public EntityTracker getEntityTracker() {
        return this.entityTracker;
    }


    public void setTracker(EntityTracker entityTracker){
        this.entityTracker = entityTracker;
    }


    public PlayerChunkMap getPlayerChunkMap() {
        return this.playerChunkMap;
    }


    public Teleporter getDefaultTeleporter() {
        return this.worldTeleporter;
    }


    public TemplateManager getStructureTemplateManager() {
        return this.saveHandler.getStructureTemplateManager();
    }


    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        this.spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
    }


    public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        SPacketParticles spacketparticles = new SPacketParticles(particleType, longDistance, (float)xCoord, (float)yCoord, (float)zCoord, (float)xOffset, (float)yOffset, (float)zOffset, (float)particleSpeed, numberOfParticles, particleArguments);

        for (int i = 0; i < this.playerEntities.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntities.get(i);
            this.sendPacketWithinDistance(entityplayermp, longDistance, xCoord, yCoord, zCoord, spacketparticles);
        }
    }


    public void spawnParticle(EntityPlayerMP player, EnumParticleTypes particle, boolean longDistance, double x, double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed, int... arguments) {
        Packet<?> packet = new SPacketParticles(particle, longDistance, (float)x, (float)y, (float)z, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, count, arguments);
        this.sendPacketWithinDistance(player, longDistance, x, y, z, packet);
    }


    private void sendPacketWithinDistance(EntityPlayerMP player, boolean longDistance, double x, double y, double z, Packet<?> packetIn) {
        double d0 = player.distanceSq(x, y, z);
        if (d0 <= 1024.0D || longDistance && d0 <= 262144.0D) player.connection.netManager.sendPacketE(packetIn);
    }


    @Nullable
    public Entity getEntityFromUuid(UUID uuid) {
        return this.entitiesByUuid.get(uuid);
    }


    @Override
    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        return this.server.addScheduledTask(runnableToSchedule);
    }


    @Override
    public boolean isCallingFromMinecraftThread() {
        return this.server.isCallingFromMinecraftThread();
    }


    @Nullable
    @Override
    public BlockPos findNearestStructure(String structureName, BlockPos position, boolean findUnexplored) {
        return this.getChunkProvider().getNearestStructurePos(this, structureName, position, findUnexplored);
    }


    public AdvancementManager getAdvancementManager() {
        return this.advancementManager;
    }


    public FunctionManager getFunctionManager() {
        return this.functionManager;
    }


    public java.io.File getChunkSaveLocation() {
        return ((net.minecraft.world.chunk.storage.AnvilChunkLoader)getChunkProvider().chunkLoader).chunkSaveLocation;
    }


    static class ServerBlockEventList extends ArrayList<BlockEventData> {
        private ServerBlockEventList() {}
    }


    @Override
    public void addAsyncTask(Runnable r) {
        server.ticker().runAsync(r);
    }


    @Override
    public void addSyncTask(Runnable task) {
        _syncTasks.add(task);
    }


    @Override
    public boolean canUnload() {
        return     playerEntities.isEmpty()
                && _syncTasks.isEmpty()
                && ((ChunkProviderServer)chunkProvider).isEmpty() &&
                !provider.getDimensionType().shouldLoadSpawn();
    }


    public final NavigationCache navCache = new NavigationCache();
    private int timerClean = 0;


    public void tickSyncTasks(){
        Runnable task = _syncTasks.poll();
        while(task != null){
            task.run();
            task = _syncTasks.poll();
        }
    }


    @Override
    public void tick() {
        localSelector.clear();
        super.tick();
        long prevWorldTotalTime = this.worldInfo.getWorldTotalTime();
        if (getGameRules().doMobSpawning.getBoolean()) {
            this.entitySpawner.findChunksForSpawning(this,
                    this.spawnHostileMobs, this.spawnPeacefulMobs,
                    prevWorldTotalTime % spawnOnSetTickRate == 0L);
        }
        if((timerClean++%ElcaConfig.NAVIGATE_CACHE_CLEAN_RATE)==0){
            navCache.clean(timerClean%ElcaConfig.NAVIGATE_RESET_RATE ==0);
        }

        this.chunkProvider.tick();
        int light = this.calculateSkylightSubtracted(1.0F);

        if (light != this.getSkylightSubtracted()) {
            this.setSkylightSubtracted(light);
        }

        this.worldInfo.setWorldTotalTime(prevWorldTotalTime + 1L);

        if (getGameRules().doWeatherCycle.getBoolean()) {
            this.setWorldTime(this.getWorldTime() + 1L);
        }
        //chunk tasks
        Iterator<TChunkTask> itr = _chunkTasks.values().iterator();
        while (itr.hasNext()) {
            TChunkTask task0 = itr.next();
            if (task0.isCompleted()) {
                if (task0.hasCallback())
                    task0.syncCallback();
                itr.remove();
            }
        }
        //end tasks

        this.tickUpdates(false);

        int balanceBlock;
        if (meanTickTime() > ElcaConfig.MEAN_BALANCE_MOB) {
            balanceBlock = ElcaConfig.BALANCE_BLOCK_CRITICAL;
        } else {
            balanceBlock = ElcaConfig.BALANCE_BLOCK_TICK;
        }
        if(balanceBlock == 0) {
            this.updateBlocks();
        } else l1:{
            if(prevWorldTotalTime % balanceBlock == 0) {
                if(meanTickTime() > ElcaConfig.MEAN_OVERLOAD){
                    if(blockUpdateDebt < 200) {
                        int am = balanceBlock >> 1 + 1;
                        if (am > balanceBlock) {
                            am = balanceBlock;
                        }
                        balanceBlock -= am;
                        blockUpdateDebt += am;
                    }
                } else {
                    if(blockUpdateDebt > 50){
                        balanceBlock    += 5;
                        blockUpdateDebt -= 5;
                    } else if(blockUpdateDebt > 0){
                        balanceBlock    += 1;
                        blockUpdateDebt -= 1;
                    }
                }
                if(balanceBlock>0) {
                    updateBlocks(balanceBlock);
                    break l1;
                }
            }
            collideAll();
        }
        if((prevWorldTotalTime & 127) == 0) poledSlice = null;

        playerChunkMap.tick();

        if(ElcaConfig.VILLAGE_TICK_RATE!=-1){
            if(prevWorldTotalTime % ElcaConfig.VILLAGE_TICK_RATE==0) {
                this.villageCollection.tick();
                this.villageSiege.tick();
            }
        }

        this.getDefaultTeleporter().removeStalePortalLocations(this.getTotalWorldTime());
        for (Teleporter tele : customTeleporters) {
            tele.removeStalePortalLocations(getTotalWorldTime());
        }

        this.sendQueuedBlockEvents();
        //mean tick time
//        if(++meanTTSkip == 20){
//            meanTTSkip = 0;
//            meanTTSum = average(server.worldTickTimes.get(provider.getDimension())) * 1.0E-6D;
//            //meanTTSum += average(FMLCommonHandler.instance().getMinecraftServerInstance().tickTimeArray) * 1.0E-6;
//            if (++meanTTCount >= 30) {
//                meanTTTime = meanTTSum / meanTTCount;
//                meanTTCount = 0;
//                meanTTSum = 1.0;
//            }
//            tryUpdateNavigationLimit = MathHelper.clamp((int)(meanTTTime*0.5),
//                    ElcaConfig.STD_UPDATE_NAVIGATION_RATE, ElcaConfig.MAX_UPDATE_NAVIGATION_RATE
//            );
//        }
    }


    private double meanTTTime = 0.0;
//    private int meanTTSkip = 0, meanTTCount = 0;
    //todo add multicore balance
    @Override
    public double meanTickTime() {
        return meanTTTime;
    }

    public void setMeanTT(double time){
        this.meanTTTime = time;
    }


//    private static long average(final long[] values) {
//        if (values == null || values.length == 0) return 0L;
//        long sum = 0L;
//        for (final long v : values) sum += v;
//        return sum / values.length;
//    }


    @Override
    public boolean tickUpdates(boolean runAllPending) {
        if (isDebug) {
            return false;
        } else {
            NextTickListEntry nextticklistentry = null;
            for (int j = 0; j < 65536 && !pendingTickListEntriesTreeSet.isEmpty(); ++j) {
                try {
                    nextticklistentry = this.pendingTickListEntriesTreeSet.first();
                } catch (NoSuchElementException e){
                    //todo maybe critical error
                    //todo legal error, ignored
                    //e.printStackTrace();
                    break;
                }

                if (!runAllPending && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                    break;
                }
                this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
                this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                this.pendingTickListEntriesThisTick.add(nextticklistentry);
            }

            Iterator<NextTickListEntry> iterator = this.pendingTickListEntriesThisTick.iterator();

            while (iterator.hasNext()) {
                NextTickListEntry entry = iterator.next();
                iterator.remove();

                if (this.isAreaLoaded(entry.position.add(0, 0, 0), entry.position.add(0, 0, 0))) {
                    IBlockState state = this.getBlockState(entry.position);

                    if (state.getMaterial() != Material.AIR && Block.isEqualTo(state.getBlock(), entry.getBlock())) {
                        try {
                            state.getBlock().updateTick(this, entry.position, state, this.rand);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                            CrashReportCategory.addBlockInfo(crashreportcategory, entry.position, state);
                            throw new ReportedException(crashreport);
                        }
                    }
                } else {
                    this.scheduleUpdate(entry.position, entry.getBlock(), 0);
                }
            }
            this.pendingTickListEntriesThisTick.clear();
            return !this.pendingTickListEntriesTreeSet.isEmpty();
        }
    }


    @Override
    public IChunkProvider createChunkProvider() {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        return chunkProviderServer = new ChunkProviderServer(this, ichunkloader, createChunkGenerator());
    }


    @Override
    public IChunkGenerator createChunkGenerator(){
        return this.provider.createChunkGenerator();
    }


    public ConcurrentHashMap<TQueuedChunk, TChunkTask> getChunkTasks() {
        return _chunkTasks;
    }


    @Override
    public void onUnload(){
        super.onUnload();
        Set<Map.Entry<TQueuedChunk, TChunkTask>> entries = _chunkTasks.entrySet();
        if(entries.isEmpty())return;
        Iterator<Map.Entry<TQueuedChunk, TChunkTask>> itr = entries.iterator();
        while (itr.hasNext()){
            Map.Entry<TQueuedChunk, TChunkTask> res = itr.next();
            res.getValue().await();
            itr.remove();
        }
    }


    @Override
    protected void tickPlayers() {
        super.tickPlayers();
        Entity entity, entity1;
        List<EntityPlayer> players = this.playerEntities;
        for (int i = 0; i < players.size(); ++i) {
            entity = players.get(i);
            if ((entity1 = entity.getRidingEntity()) != null) {
                if (!entity1.isDead && entity1.isPassenger(entity)) continue;

                entity.dismountRidingEntity();
            }

            if (!entity.isDead) {
                try {
                    this.updateEntity(entity);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
                    entity.addEntityCrashInfo(crashreportcategory);
                    server.ticker().onError(this, new ReportedException(crashreport));
                }
            }
            if (entity.isDead) {
                int j = entity.chunkCoordX, k = entity.chunkCoordZ;
                if (entity.addedToChunk){
                    Chunk chunk = chunkProviderServer.getLoadedChunk(j, k);
                    if(chunk != null)chunk.removeEntity(entity);
                }

                this.realEntityList.removeRefSwap(entity);
                this.onEntityRemoved(entity);
            }
        }
    }


    public void saveAllChunks(boolean all, @Nullable IProgressUpdate progressCallback) throws MinecraftException {
        ChunkProviderServer chunkproviderserver = this.getChunkProvider();
        if (chunkproviderserver.canSave()) {
            if (progressCallback != null) {
                progressCallback.displaySavingString("Saving level");
            }
            this.saveLevel();
            if (progressCallback != null) {
                progressCallback.displayLoadingString("Saving chunks");
            }
            chunkproviderserver.saveChunks(all);
            MinecraftForge.EVENT_BUS.postF(new WorldEvent.Save(this));
        }
    }


    private final BlockPos.MutableBlockPos localUpdateBlocks = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos localUpdateBlocks2 = new BlockPos.MutableBlockPos();
    private int blockUpdateFlags   = 0;
    private int blockUpdateDebt    = 0;

    @Override
    protected void updateBlocks() {
        updateBlocks(1);
    }


    protected void updateBlocks(final int factor) {
        this.playerCheckLight();
        if (isDebug) {
            for (Iterator<Chunk> iterator = this.playerChunkMap.getChunkIterator(); iterator.hasNext();){
                iterator.next().onTick(false);
            }
        } else {
            int updateLCG = this.updateLCG;
            final boolean raining = isRaining(), thundering = isThundering();
            final boolean skipRecheck = (blockUpdateFlags & 1) == 1;

            int radius;
            if((blockUpdateFlags & 2) == 2){
                radius = ElcaConfig.CHUNK_TICK_RADIUS_1;
            } else {
                radius = ElcaConfig.CHUNK_TICK_RADIUS_2;
            }
            ++blockUpdateFlags;

            ChunkSelector selector = localSelector;
            Chunk chunk;
            BlockPos.MutableBlockPos mut = localUpdateBlocks;
            BlockPos.MutableBlockPos mut2 = localUpdateBlocks2;
            final int rts = getGameRules().randomTickSpeed.getInt() * factor;
            final int lightChance = 100000 / factor;
            final int snowChance  = 16 / factor;
            final Random rand = this.rand;

            EntityCollider collider = this.collider;
            collider.begin(getGameRules().maxEntityCramming.getInt());

            for (ChunkIterator iter = this.playerChunkMap.getChunkIterator(radius); iter.hasNext();){
                selector.setChunk(chunk = iter.next());
                collider.collideAll(chunk);

                int startCX = chunk.x << 4;
                int startCZ = chunk.z << 4;
                chunk.enqueueRelightChecks();
                chunk.onTick(skipRecheck);

                if (raining && thundering && provider.canDoLightning(chunk) && rand.nextInt(lightChance) == 0) {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int updSeed = updateLCG >> 2;
                    BlockPos blockpos = adjustPosToNearbyEntity(
                            mut.setPos(startCX + (updSeed & 15), 0, startCZ + (updSeed >> 8 & 15))
                    );

                    if (isRainingAt(chunk, blockpos)) {
                        DifficultyInstance difficultyinstance = getDifficultyForLocation(blockpos);

                        if (getGameRules().doMobSpawning.getBoolean() && rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.01D) {
                            EntitySkeletonHorse horse = new EntitySkeletonHorse(WorldServer.this);
                            horse.setTrap(true);
                            horse.setGrowingAge(0);
                            horse.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                            spawnEntity(horse);
                            addWeatherEffect(new EntityLightningBolt(WorldServer.this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), true));
                        } else {
                            addWeatherEffect(new EntityLightningBolt(WorldServer.this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), false));
                        }
                    }
                }

                if (provider.canDoRainSnowIce(chunk) && rand.nextInt(snowChance) == 0) {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int rNum = updateLCG >> 2;
                    int posX = startCX + (rNum & 15);
                    int posZ = startCZ + (rNum >> 8 & 15);
                    int posY = chunk.getPrecipitationHeightY(posX, posZ);

                    mut2.setPosB(posX, posY-1, posX);

                    if (isAreaLoaded(mut2, 1) && canBlockFreezeNoWater(mut2))
                        setBlockState(mut2, Blocks.ICE.getDefaultState());

                    if (raining) {
                        if(canSnowAt(mut.setPos(posX, posY, posZ), true))
                            setBlockState(mut, Blocks.SNOW_LAYER.getDefaultState());

                        if (selector.getBiome(mut2).canRain())
                            selector.getState(mut2).getBlock().fillWithRain(WorldServer.this, mut2);
                    }
                }

                if (rts > 0) {
                    int dx, dz, dy;
                    Block block;
                    for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                        if (storage != Chunk.NULL_BLOCK_STORAGE && storage.needsRandomTick()) {
                            for (int lim = 0, yLoc = storage.getYLocation(); lim < rts; ++lim) {
                                updateLCG = updateLCG * 3 + 1013904223;
                                int rNum = updateLCG >> 2;

                                IBlockState state = storage.get(dx = rNum & 15, dy = rNum >> 16 & 15, dz = rNum >> 8 & 15);

                                if ((block = state.getBlock()).getTickRandomly()) {
                                    block.randomTick(
                                            this,
                                            mut2.setPos(dx + startCX, dy + yLoc, dz + startCZ),
                                            state, rand
                                    );
                                }
                            }
                        }
                    }
                }
            }
            this.updateLCG = updateLCG;

            collider.complete();
        }
    }


    private boolean isRainingAt(Chunk chunk, BlockPos position) {
        if (!this.isRaining()) {
            return false;
        } else if (!chunk.canSeeSky(position)) {
            return false;
        } else if (chunk.getPrecipitationHeight(position).getY() > position.getY()) {
            return false;
        } else {
            Biome biome = chunk.getBiome(position, provider.biomeProvider);
            if (biome.getEnableSnow()) {
                return false;
            } else {
                return !this.canSnowAt(position, false) && biome.canRain();
            }
        }
    }


    @Nullable
    public Chunk getLoadedChunk(int chunkX, int chunkZ){
        return chunkProviderServer.getLoadedChunk(chunkX, chunkZ);
    }


    @Override
    public void checkSessionLock() throws MinecraftException {
        if(ElcaConfig.DISABLE_SESSION_LOCK)return;
        super.checkSessionLock();
    }


    @Override
    public final boolean isSideSolid(BlockPos pos, @NotNull EnumFacing side, boolean def) {
        if (pos.getY() < 0 || pos.getY() >= 256) return def;
        Chunk chunk = localSelector.getChunk(pos);
        if (chunk.isEmpty()) return def;
        return chunk.getBlockStateUnchecked(pos.getX(), pos.getY(), pos.getZ()).isSideSolid(localSelector, pos, side);
    }


    public void cleanCache(){
        navCache.clean(true);
        chunkProviderServer.chunkGenerator.cleanupCache();
        provider.getBiomeProvider().cleanupCache();
        clearEvents();
    }

    public EMoveHelper.ComboCollideTask collideTask = new EMoveHelper.ComboCollideTask(this);

    private EMoveHelper.MoveTask[] allTasks = new EMoveHelper.MoveTask[128];
    private int realSize = 0;
    
    public EMoveHelper.MoveTask allocMoveTask(){
        if(realSize == 0)return new EMoveHelper.MoveTask();
        else return allTasks[--realSize];
    }
    
    public void freeMoveTask(EMoveHelper.MoveTask task){
        if(allTasks.length == realSize){
            EMoveHelper.MoveTask[] newTasks = new EMoveHelper.MoveTask[allTasks.length+128];
            System.arraycopy(allTasks, 0, newTasks, 0, allTasks.length);
            allTasks = newTasks;
        }
        allTasks[realSize++] = task;
    }

    private RecipeItemHelper recipeItemHelper = null;
    public RecipeItemHelper getRecipeItemHelper(){
        RecipeItemHelper helper;
        if((helper = this.recipeItemHelper) == null)return recipeItemHelper = new RecipeItemHelper();
        else {
            helper.clear();
            return helper;
        }
    }
}