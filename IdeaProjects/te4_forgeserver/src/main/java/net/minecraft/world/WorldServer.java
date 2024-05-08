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
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaConfig;
import plus.fserver.api.MeanTicker;
import plus.fserver.api.PWorldServer;
import plus.fserver.ElcaCV;
import plus.fserver.server.chunk.io.TChunkTask;
import plus.fserver.server.chunk.io.TQueuedChunk;
import plus.fserver.utls.ChunkSelector;
import plus.fserver.utls.math.MathUtls;
import plus.fserver.world.NavigationCache;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.math.BlockPosDelta;
import plus.tson.utl.Te4HashSet;


public class WorldServer extends World implements IThreadListener, PWorldServer, MeanTicker {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MinecraftServer server;
    private final EntityTracker entityTracker;
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
    private final WorldEntitySpawner entitySpawner = new WorldEntitySpawner();
    protected final VillageSiege villageSiege = new VillageSiege(this);
    private final WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
    private int blockEventCacheIndex;
    private final List<NextTickListEntry> pendingTickListEntriesThisTick = new ArrayList<>();

    /** Stores the recently processed (lighting) chunks */
    //protected Set<ChunkPos> doneChunks = new Te4HashSet<>();
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
        this.entityTracker = new EntityTracker(this);
        this.playerChunkMap = new PlayerChunkMap(this);

        perWorldStorage = new MapStorage(new WorldSpecificSaveHandler(this, saveHandlerIn));
        this.worldTeleporter = new Teleporter(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.getWorldBorder().setSize(server.getMaxWorldSize());

        DimensionManager.setWorld(dimensionId, this, server);

        effectiveAcces = localSelector;
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
        List<Biome.SpawnListEntry> list = chunkProviderServer.getPossibleCreatures(creatureType, pos);
        list = ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
        return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(this.rand, list) : null;
    }


    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, Biome.SpawnListEntry spawnListEntry, BlockPos pos) {
        List<Biome.SpawnListEntry> list = chunkProviderServer.getPossibleCreatures(creatureType, pos);
        list =ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
        return list != null && !list.isEmpty() && list.contains(spawnListEntry);
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
        if (this.allPlayersSleeping && !this.isRemote) {
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
        this.profiler.startSection("playerCheckLight");

        if (!this.playerEntities.isEmpty()) {
            int i = this.rand.nextInt(this.playerEntities.size());
            EntityPlayer entityplayer = this.playerEntities.get(i);
            int j = MathHelper.floor(entityplayer.posX) + this.rand.nextInt(11) - 5;
            int k = MathHelper.floor(entityplayer.posY) + this.rand.nextInt(11) - 5;
            int l = MathHelper.floor(entityplayer.posZ) + this.rand.nextInt(11) - 5;
            this.checkLight(new BlockPos(j, k, l));
        }
        this.profiler.endSection();
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

        if (this.scheduledUpdatesAreImmediate && material != Material.AIR) {
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
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        if (!this.canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob)) {
            entityIn.setDead();
        }
        if (!this.canSpawnNPCs() && entityIn instanceof INpc) {
            entityIn.setDead();
        }
        updateEntityWithOptionalForce0(entityIn, forceUpdate);
    }


    private void updateEntityWithOptionalForce0(Entity entityIn, boolean forceUpdate) {
        if (checkCanUpdate) {
            boolean canUpdate = chunkProviderServer.isChunkLoadedSafe(entityIn.chunkCoordX, entityIn.chunkCoordZ);
            //todo make this pooled
            if (!canUpdate) canUpdate = ForgeEventFactory.canEntityUpdate(entityIn);
            if (!canUpdate) return;
        }

        entityIn.lastTickPosX = entityIn.posX;
        entityIn.lastTickPosY = entityIn.posY;
        entityIn.lastTickPosZ = entityIn.posZ;
        entityIn.prevRotationYaw = entityIn.rotationYaw;
        entityIn.prevRotationPitch = entityIn.rotationPitch;

        if (forceUpdate && entityIn.addedToChunk) try {
            ++entityIn.ticksExisted;

            if (entityIn.isRiding())         entityIn.updateRidden();
            else if(!entityIn.updateBlocked) entityIn.onUpdate();

        } catch (Exception exception){
            server.ticker().onError(this, onEntityError(entityIn, exception));
        }

        if (MathUtls.isInvalidDouble(entityIn.posX))          entityIn.posX = entityIn.lastTickPosX;
        if (MathUtls.isInvalidDouble(entityIn.posY))          entityIn.posY = entityIn.lastTickPosY;
        if (MathUtls.isInvalidDouble(entityIn.posZ))          entityIn.posZ = entityIn.lastTickPosZ;
        if (MathUtls.isInvalidDouble(entityIn.rotationPitch)) entityIn.rotationPitch = entityIn.prevRotationPitch;
        if (MathUtls.isInvalidDouble(entityIn.rotationYaw))   entityIn.rotationYaw = entityIn.prevRotationYaw;

        updateEntityChunkSection(entityIn, forceUpdate);
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
            for (Entity entity4 : entity.getPassengers()) {
                if (!entity4.isDead && entity4.getRidingEntity() == entity) {
                    this.updateEntity(entity4);
                } else {
                    entity4.dismountRidingEntity();
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
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
        } else {
           ForgeEventFactory.onCreateWorldSpawn(this, settings);
        }
    }


    protected void createBonusChest() {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest();
        for (int i = 0; i < 10; ++i) {
            int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            BlockPos blockpos = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();

            if (worldgeneratorbonuschest.generate(this, this.rand, blockpos)) {
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
        this.mapStorage.saveAllData();
        this.perWorldStorage.saveAllData();
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
                this.loadedEntityList.add(entity);
                this.onEntityAdded(entity);
            }
        }
    }


    private boolean canAddEntity(Entity entityIn) {
        if (entityIn.isDead) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityList.getKey(entityIn));
            return false;
        } else {
            UUID uuid = entityIn.getUniqueID();
            if (this.entitiesByUuid.containsKey(uuid)) {
                Entity entity = this.entitiesByUuid.get(uuid);
                if (this.unloadedEntityList.contains(entity)) {
                    this.unloadedEntityList.remove(entity);
                } else {
                    if (!(entityIn instanceof EntityPlayer)) {
                        LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityList.getKey(entity), uuid.toString());
                        return false;
                    }
                    LOGGER.warn("Force-added player with duplicate UUID {}", (Object)uuid.toString());
                }
                this.removeEntityDangerously(entity);
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
            this.server.getPlayerList().sendToAllNearExcept((EntityPlayer)null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimension(), new SPacketSpawnGlobalEntity(entityIn));
            return true;
        } else {
            return false;
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


    private void sendQueuedBlockEvents() {
        while (!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
            int i = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;

            for (BlockEventData blockeventdata : this.blockEventQueue[i]) {
                if (this.fireBlockEvent(blockeventdata)) {
                    this.server.getPlayerList().sendToAllNearExcept(null, blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, this.provider.getDimension(), new SPacketBlockAction(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
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
            this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
        }

        if (this.prevThunderingStrength != this.thunderingStrength) {
            this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
        }

        /* The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
         * rather than to all players on the server. This is what causes the client-side rain, as the
         * client believes that it has started raining locally, rather than in another dimension.
         */
        if (flag != this.isRaining()) {
            if (flag) {
                this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(
                        new SPacketChangeGameState(2, 0.0F), this.provider.getDimension()
                );
            } else {
                this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(
                        new SPacketChangeGameState(1, 0.0F), this.provider.getDimension()
                );
            }
            this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(
                    new SPacketChangeGameState(7, this.rainingStrength),
                    this.provider.getDimension()
            );
            this.server.getPlayerList().sendPacketToAllPlayersInDimensionE(
                    new SPacketChangeGameState(8, this.thunderingStrength),
                    this.provider.getDimension()
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
                && ((ChunkProviderServer)chunkProvider).loadedChunks.isEmpty() &&
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
        int j = this.calculateSkylightSubtracted(1.0F);

        if (j != this.getSkylightSubtracted()) {
            this.setSkylightSubtracted(j);
        }

        this.worldInfo.setWorldTotalTime(prevWorldTotalTime + 1L);

        if (getGameRules().doWeatherCycle.getBoolean()) {
            this.setWorldTime(this.getWorldTime() + 1L);
        }
        //chunk tasks
        Iterator<TChunkTask> itr = _chunkTasks.values().iterator();
        while (itr.hasNext()) {
            TChunkTask task0 = itr.next();
            if (task0.runFinished()) {
                if (task0.hasCallback())
                    task0.syncCallback();
                itr.remove();
            }
        }
        //end tasks

        this.tickUpdates(false);
        this.updateBlocks();
        this.getPlayerChunkMap().tick();


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
        if(++meanTTSkip == 20){
            meanTTSkip = 0;
            meanTTSum += average(FMLCommonHandler.instance().getMinecraftServerInstance().tickTimeArray) * 1.0E-6;
            if (++meanTTCount >= 30) {
                meanTTTime = meanTTSum / meanTTCount;
                meanTTCount = 0;
                meanTTSum = 1.0;
            }
            tryUpdateNavigationLimit = MathHelper.clamp((int)(meanTTTime*0.5),
                    ElcaConfig.STD_UPDATE_NAVIGATION_RATE, ElcaConfig.MAX_UPDATE_NAVIGATION_RATE
            );
        }
    }


    private double meanTTSum = 1.0, meanTTTime = 0.0;
    private int meanTTSkip = 0, meanTTCount = 0;
    //todo add multicore balance
    @Override
    public double meanTickTime() {
        return meanTTTime;
    }


    private static long average(final long[] values) {
        if (values == null || values.length == 0) return 0L;
        long sum = 0L;
        for (final long v : values) sum += v;
        return sum / values.length;
    }


    @Override
    public boolean tickUpdates(boolean runAllPending) {
        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
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
                    IBlockState iblockstate = this.getBlockState(entry.position);

                    if (iblockstate.getMaterial() != Material.AIR && Block.isEqualTo(iblockstate.getBlock(), entry.getBlock())) {
                        try {
                            iblockstate.getBlock().updateTick(this, entry.position, iblockstate, this.rand);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                            CrashReportCategory.addBlockInfo(crashreportcategory, entry.position, iblockstate);
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


    @Override
    public ConcurrentHashMap<TQueuedChunk, TChunkTask> getChunkTasks() {
        return _chunkTasks;
    }


    @Override
    protected void tickPlayers() {
        super.tickPlayers();
        Entity entity, entity1;
        for (int i = 0; i < this.playerEntities.size(); ++i) {
            entity = this.playerEntities.get(i);
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
                    throw new ReportedException(crashreport);
                }
            }
            if (entity.isDead) {
                int j = entity.chunkCoordX, k = entity.chunkCoordZ;
                if (entity.addedToChunk){
                    Chunk chunk = chunkProviderServer.getLoadedChunk(j, k);
                    if(chunk != null)chunk.removeEntity(entity);
                }

                this.loadedEntityList.remove(entity);
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
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Save(this));
        }
    }


    private final BlockPos.MutableBlockPos localUpdateBlocks = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos localUpdateBlocks2 = new BlockPos.MutableBlockPos();
    @Override
    protected void updateBlocks() {
        this.playerCheckLight();
        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            for (Iterator<Chunk> iterator = this.playerChunkMap.getChunkIterator(); iterator.hasNext(); this.profiler.endSection()){
                iterator.next().onTick(false);
            }
        } else {
            int updateLCG = this.updateLCG;
            final int rts = getGameRules().randomTickSpeed.getInt();
            final boolean flag = isRaining(), flag1 = isThundering();
            ChunkSelector selector = localSelector;
            Chunk chunk;
            BlockPos.MutableBlockPos mut = localUpdateBlocks;
            BlockPos.MutableBlockPos mut2 = localUpdateBlocks2;
            for (Iterator<Chunk> iterator = this.playerChunkMap.getChunkIterator(); iterator.hasNext(); this.profiler.endSection()){
                selector.setChunk(chunk = iterator.next());

                int j = chunk.x << 4;
                int k = chunk.z << 4;
                chunk.enqueueRelightChecks();
                chunk.onTick(false);

                if (flag && flag1 && provider.canDoLightning(chunk) && rand.nextInt(100000) == 0) {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int updSeed = updateLCG >> 2;
                    BlockPos blockpos = adjustPosToNearbyEntity(
                            mut.setPos(j + (updSeed & 15), 0, k + (updSeed >> 8 & 15))
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

                if (provider.canDoRainSnowIce(chunk) && rand.nextInt(16) == 0) {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int j2 = updateLCG >> 2;
                    BlockPos pos1 = getPrecipitationHeight(
                            mut.setPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15))
                    );
                    mut2.setPos(pos1.getX(), pos1.getY()-1, pos1.getZ());

                    if (isAreaLoaded(mut2, 1) && canBlockFreezeNoWater(mut2))
                        setBlockState(mut2, Blocks.ICE.getDefaultState());

                    if (flag) {
                        if(canSnowAt(pos1, true))
                            setBlockState(pos1, Blocks.SNOW_LAYER.getDefaultState());

                        if (getBiome(mut2).canRain())
                            getBlockState(mut2).getBlock().fillWithRain(WorldServer.this, mut2);
                    }
                }

                if (rts > 0) {
                    int dx, dz, dy;
                    Block block;
                    for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                        if (storage != Chunk.NULL_BLOCK_STORAGE && storage.needsRandomTick()) {
                            for (int i1 = 0; i1 < rts; ++i1) {
                                updateLCG = updateLCG * 3 + 1013904223;
                                int rand = updateLCG >> 2;

                                IBlockState state = storage.get(dx = rand & 15, dy = rand >> 16 & 15, dz = rand >> 8 & 15);

                                if ((block = state.getBlock()).getTickRandomly()) {
                                    block.randomTick(WorldServer.this,
                                            mut2.setPos(dx + j, dy + storage.getYLocation(), dz + k), state, this.rand
                                    );
                                }
                            }
                        }
                    }
                }
            }

            this.updateLCG = updateLCG;
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
}