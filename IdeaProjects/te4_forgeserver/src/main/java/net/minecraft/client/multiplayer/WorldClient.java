package net.minecraft.client.multiplayer;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaCV;
import plus.fserver.utls.collections.ArrayListE6;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


@SideOnly(Side.CLIENT)
public class WorldClient extends World {
    private final NetHandlerPlayClient connection;
    private ChunkProviderClient clientChunkProvider;
    private final Set<Entity> entityList = Sets.newHashSet();
    private final Set<Entity> entitySpawnQueue = Sets.newHashSet();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<ChunkPos> previousActiveChunkSet = Sets.newHashSet();
    private int ambienceTicks;
    protected Set<ChunkPos> visibleChunks;

    public WorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), net.minecraftforge.common.DimensionManager.createProviderFor(dimension), profilerIn, true);
        if(SERVER_COMPILE)throw new RuntimeException("Server compile not supports client world!");
        this.ambienceTicks = this.rand.nextInt(12000);
        this.visibleChunks = Sets.newHashSet();
        this.connection = netHandler;
        this.getWorldInfo().setDifficulty(difficulty);
        this.provider.setWorld(this);
        this.setSpawnPoint(new BlockPos(8, 64, 8)); //Forge: Moved below registerWorld to prevent NPE in our redirect.
        this.chunkProvider = this.createChunkProvider();
        this.mapStorage = new SaveDataMemoryStorage();
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.initCapabilities();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(this));
    }


    @Override
    public void tick() {
        super.tick();
        this.setTotalWorldTime(this.getTotalWorldTime() + 1L);
        if (this.getGameRules().getBoolean("doDaylightCycle")) {
            this.setWorldTime(this.getWorldTime() + 1L);
        }

        this.profiler.startSection("reEntryProcessing");

        for (int i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); ++i) {
            Entity entity = this.entitySpawnQueue.iterator().next();
            this.entitySpawnQueue.remove(entity);

            if (!this.loadedEntityList.contains(entity)) {
                this.spawnEntity(entity);
            }
        }
        this.profiler.endStartSection("chunkCache");
        this.clientChunkProvider.tick();
        this.profiler.endStartSection("blocks");
        this.updateBlocks();
        this.profiler.endSection();
    }


    public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {}


    protected IChunkProvider createChunkProvider() {
        this.clientChunkProvider = new ChunkProviderClient(this);
        return this.clientChunkProvider;
    }


    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return allowEmpty || !this.getChunkProvider().provideChunk(x, z).isEmpty();
    }


    protected void refreshVisibleChunks() {
        this.visibleChunks.clear();
        int i = this.mc.gameSettings.renderDistanceChunks;
        this.profiler.startSection("buildList");
        int j = MathHelper.floor(this.mc.player.posX / 16.0D);
        int k = MathHelper.floor(this.mc.player.posZ / 16.0D);

        for (int l = -i; l <= i; ++l) {
            for (int i1 = -i; i1 <= i; ++i1) {
                this.visibleChunks.add(new ChunkPos(l + j, i1 + k));
            }
        }
        this.profiler.endSection();
    }


    protected void updateBlocks() {
        this.refreshVisibleChunks();

        if (this.ambienceTicks > 0) {
            --this.ambienceTicks;
        }

        this.previousActiveChunkSet.retainAll(this.visibleChunks);

        if (this.previousActiveChunkSet.size() == this.visibleChunks.size()) {
            this.previousActiveChunkSet.clear();
        }

        int i = 0;

        for (ChunkPos chunkpos : this.visibleChunks) {
            if (!this.previousActiveChunkSet.contains(chunkpos)) {
                int j = chunkpos.x * 16;
                int k = chunkpos.z * 16;
                this.profiler.startSection("getChunk");
                Chunk chunk = this.getChunk(chunkpos.x, chunkpos.z);
                this.playMoodSoundAndCheckLight(j, k, chunk);
                this.profiler.endSection();
                this.previousActiveChunkSet.add(chunkpos);
                ++i;

                if (i >= 10) {
                    return;
                }
            }
        }
    }


    public void doPreChunk(int chunkX, int chunkZ, boolean loadChunk) {
        if (loadChunk) {
            this.clientChunkProvider.loadChunk(chunkX, chunkZ);
        } else {
            this.clientChunkProvider.unloadChunk(chunkX, chunkZ);
            this.markBlockRangeForRenderUpdate(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);
        }
    }


    public boolean spawnEntity(Entity entityIn) {
        boolean flag = super.spawnEntity(entityIn);
        this.entityList.add(entityIn);

        if (flag) {
            if (entityIn instanceof EntityMinecart) {
                this.mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)entityIn));
            }
        } else {
            this.entitySpawnQueue.add(entityIn);
        }
        return flag;
    }


    public void removeEntity(Entity entityIn) {
        super.removeEntity(entityIn);
        this.entityList.remove(entityIn);
    }


    public void onEntityAdded(Entity entityIn) {
        super.onEntityAdded(entityIn);
        if (this.entitySpawnQueue.contains(entityIn)) {
            this.entitySpawnQueue.remove(entityIn);
        }
    }


    public void onEntityRemoved(Entity entityIn) {
        super.onEntityRemoved(entityIn);

        if (this.entityList.contains(entityIn)) {
            if (entityIn.isEntityAlive()) {
                this.entitySpawnQueue.add(entityIn);
            } else {
                this.entityList.remove(entityIn);
            }
        }
    }


    public void addEntityToWorld(int entityID, Entity entityToSpawn) {
        Entity entity = this.getEntityByID(entityID);
        if (entity != null) {
            this.removeEntity(entity);
        }

        this.entityList.add(entityToSpawn);
        entityToSpawn.setEntityId(entityID);

        if (!this.spawnEntity(entityToSpawn)) {
            this.entitySpawnQueue.add(entityToSpawn);
        }
        this.entitiesById.addKey(entityID, entityToSpawn);
    }


    @Nullable
    public Entity getEntityByID(int id) {
        return (Entity)(id == this.mc.player.getEntityId() ? this.mc.player : super.getEntityByID(id));
    }


    public Entity removeEntityFromWorld(int entityID) {
        Entity entity = this.entitiesById.removeObject(entityID);
        if (entity != null) {
            this.entityList.remove(entity);
            this.removeEntity(entity);
        }
        return entity;
    }


    @Deprecated
    public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        this.invalidateBlockReceiveRegion(i, j, k, i, j, k);
        return super.setBlockState(pos, state, 3);
    }


    @Override
    public void sendQuittingDisconnectingPacket() {
        this.connection.getNetworkManager().closeChannel(new TextComponentString("Quitting"));
    }


    @Override
    protected void updateWeather() {}


    @Override
    protected void playMoodSoundAndCheckLight(int x, int z, Chunk chunkIn) {
        super.playMoodSoundAndCheckLight(x, z, chunkIn);

        if (this.ambienceTicks == 0) {
            this.updateLCG = this.updateLCG * 3 + 1013904223;
            int i = this.updateLCG >> 2;
            int j = i & 15;
            int k = i >> 8 & 15;
            int l = i >> 16 & 255;
            BlockPos blockpos = new BlockPos(j + x, l, k + z);
            IBlockState iblockstate = chunkIn.getBlockState(blockpos);
            j = j + x;
            k = k + z;

            if (iblockstate.getMaterial() == Material.AIR && this.getLight(blockpos) <= this.rand.nextInt(8) && this.getLightFor(EnumSkyBlock.SKY, blockpos) <= 0) {
                double d0 = this.mc.player.getDistanceSq((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D);

                if (this.mc.player != null && d0 > 4.0D && d0 < 256.0D) {
                    this.playSound((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.rand.nextFloat() * 0.2F, false);
                    this.ambienceTicks = this.rand.nextInt(12000) + 6000;
                }
            }
        }
    }


    public void doVoidFogParticles(int posX, int posY, int posZ) {
        int i = 32;
        Random random = new Random();
        ItemStack itemstack = this.mc.player.getHeldItemMainhand();
        boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j) {
            this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }


    public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
        int i = x + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        int j = y + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        int k = z + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        pos.setPos(i, j, k);
        IBlockState iblockstate = this.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, this, pos, random);

        if (holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
            this.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
        }
    }


    public void removeAllEntities() {
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int i = 0; i < this.unloadedEntityList.size(); ++i) {
            Entity entity = this.unloadedEntityList.get(i);
            int j = entity.chunkCoordX;
            int k = entity.chunkCoordZ;

            if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
                this.getChunk(j, k).removeEntity(entity);
            }
        }

        for (int i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
            this.onEntityRemoved(this.unloadedEntityList.get(i1));
        }

        this.unloadedEntityList.clear();

        for (int j1 = 0; j1 < this.loadedEntityList.size(); ++j1) {
            Entity entity1 = this.loadedEntityList.get(j1);
            Entity entity2 = entity1.getRidingEntity();

            if (entity2 != null) {
                if (!entity2.isDead && entity2.isPassenger(entity1)) {
                    continue;
                }

                entity1.dismountRidingEntity();
            }

            if (entity1.isDead) {
                int k1 = entity1.chunkCoordX;
                int l = entity1.chunkCoordZ;

                if (entity1.addedToChunk && this.isChunkLoaded(k1, l, true)) {
                    this.getChunk(k1, l).removeEntity(entity1);
                }
                this.loadedEntityList.remove(j1--);
                this.onEntityRemoved(entity1);
            }
        }
    }


    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
        crashreportcategory.addDetail("Forced entities", new ICrashReportDetail<String>()
        {
            public String call() {
                return WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList;
            }
        });
        crashreportcategory.addDetail("Retry entities", new ICrashReportDetail<String>() {
            public String call() {
                return WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue;
            }
        });
        crashreportcategory.addDetail("Server brand", new ICrashReportDetail<String>() {
            public String call() throws Exception {
                return WorldClient.this.mc.player.getServerBrand();
            }
        });
        crashreportcategory.addDetail("Server type", new ICrashReportDetail<String>() {
            public String call() throws Exception {
                return WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
            }
        });
        return crashreportcategory;
    }


    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        if (player == this.mc.player) {
            this.playSound(x, y, z, soundIn, category, volume, pitch, false);
        }
    }


    public void playSound(BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        this.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch, distanceDelay);
    }


    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        double d0 = this.mc.getRenderViewEntity().getDistanceSq(x, y, z);
        PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(soundIn, category, volume, pitch, (float)x, (float)y, (float)z);

        if (distanceDelay && d0 > 100.0D) {
            double d1 = Math.sqrt(d0) / 40.0D;
            this.mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int)(d1 * 20.0D));
        } else {
            this.mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }


    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {
        this.mc.effectRenderer.addEffect(new ParticleFirework.Starter(this, x, y, z, motionX, motionY, motionZ, this.mc.effectRenderer, compound));
    }


    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        this.connection.sendPacket(packetIn);
    }


    public void setWorldScoreboard(Scoreboard scoreboardIn) {
        this.worldScoreboard = scoreboardIn;
    }


    @Override
    public void setWorldTime(long time) {
        if (time < 0L) {
            time = -time;
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        } else {
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
        }
        super.setWorldTime(time);
    }


    @Override
    public ChunkProviderClient getChunkProvider() {
        return (ChunkProviderClient)super.getChunkProvider();
    }


    //to merge ========================================================================================================================
    protected <T extends Entity> void getEntitiesWithinAABB0(List<T> list, Class <? extends T > clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T > filter) {
        int j2 = MathHelper.floor((aabb.minX - MAX_ENTITY_RADIUS) / 16.0D);
        int k2 = MathHelper.ceil((aabb.maxX + MAX_ENTITY_RADIUS) / 16.0D);
        int l2 = MathHelper.floor((aabb.minZ - MAX_ENTITY_RADIUS) / 16.0D);
        int i3 = MathHelper.ceil((aabb.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

        for (int j3 = j2; j3 < k2; ++j3) {
            for (int k3 = l2; k3 < i3; ++k3) {
                if (this.isChunkLoaded(j3, k3, true)) {
                    this.getChunk(j3, k3).getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
                }
            }
        }
    }


    public boolean isBlockFullCube(BlockPos pos) {
        AxisAlignedBB axisalignedbb = this.getBlockState(pos).getCollisionBoundingBox(this, pos);
        return axisalignedbb != Block.NULL_AABB && axisalignedbb.getAverageEdgeLength() >= 1.0D;
    }


    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        if (this.isOutsideBuildHeight(pos)) {
            return false;
        } else {
            Chunk chunk1 = this.chunkProvider.getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);

            if (chunk1 != null && !chunk1.isEmpty()) {
                IBlockState iblockstate1 = this.getBlockState(pos);
                return iblockstate1.getBlock().isNormalCube(iblockstate1, this, pos);
            } else {
                return _default;
            }
        }
    }


    public boolean isFlammableWithin(AxisAlignedBB bb) {
        int j2 = MathHelper.floor(bb.minX);
        int k2 = MathHelper.ceil(bb.maxX);
        int l2 = MathHelper.floor(bb.minY);
        int i3 = MathHelper.ceil(bb.maxY);
        int j3 = MathHelper.floor(bb.minZ);
        int k3 = MathHelper.ceil(bb.maxZ);

        if (this.isAreaLoaded(j2, l2, j3, k2, i3, k3, true)) {
            BlockPos.PooledMutableBlockPos pooledPos = BlockPos.PooledMutableBlockPos.retain();

            for (int l3 = j2; l3 < k2; ++l3) {
                for (int i4 = l2; i4 < i3; ++i4) {
                    for (int j4 = j3; j4 < k3; ++j4) {
                        Block block = this.getBlockState(pooledPos.setPos(l3, i4, j4)).getBlock();

                        if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                            pooledPos.release();
                            return true;
                        } else if (block.isBurning(this, new BlockPos(l3, i4, j4))) {
                            pooledPos.release();
                            return true;
                        }
                    }
                }
            }
            pooledPos.release();
        }
        return false;
    }


    protected boolean getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, boolean fastCheck, ArrayListE6<AxisAlignedBB> outList) {
        if (fastCheck){
            if(ElcaCV.ENABLE_COLLISION_EVENTS){
                MinecraftForge.EVENT_BUS.post(collisionEvent.init(entityIn, aabb, outList));
            }
            if(!outList.isEmpty())return true;
        }
        try {
            int minX = MathHelper.floor(aabb.minX) - 1;
            int maxX = MathHelper.ceil(aabb.maxX) + 1;
            int minY = MathHelper.floor(aabb.minY) - 1;
            int maxY = MathHelper.ceil(aabb.maxY) + 1;
            int minZ = MathHelper.floor(aabb.minZ) - 1;
            int maxZ = MathHelper.ceil(aabb.maxZ) + 1;

            WorldBorder worldborder = this.getWorldBorder();
            boolean flag = entityIn != null && entityIn.isOutsideBorder();
            boolean flag1 = entityIn != null && this.isInsideWorldBorder(entityIn);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            IBlockState state;
            //IBlockEAccess access = isRemote?this:new ChunkSelector(this);
            for (int xPos = minX; xPos < maxX; ++xPos) {
                for (int zPos = minZ; zPos < maxZ; ++zPos) {
                    boolean flag2 = xPos == minX || xPos == maxX - 1;
                    boolean flag3 = zPos == minZ || zPos == maxZ - 1;

                    if ((!flag2 || !flag3) && isChunkLoaded(xPos>>4, zPos>>4, true)) {
                        Chunk chunk = null;
                        for (int i2 = minY; i2 < maxY; ++i2) {
                            if (!flag2 && !flag3 || i2 != maxY - 1) {
                                if (fastCheck) {
                                    if (xPos < -30000000 || xPos >= 30000000 || zPos < -30000000 || zPos >= 30000000) {
                                        return true;
                                    }
                                } else if (entityIn != null && flag == flag1) {
                                    entityIn.setOutsideBorder(!flag1);
                                }

                                if (!fastCheck && flag1 && !worldborder.contains(xPos, zPos)) {
                                    state = BlockIDIndexes.STONE_STATE;
                                } else {
                                    if(chunk == null)chunk = getChunk(xPos>>4, zPos>>4);
                                    state = chunk.getBlockState(xPos, i2, zPos);
                                }

                                state.addCollisionBoxToListF(this, pos.setPos(xPos, i2, zPos), aabb, outList, entityIn, false);

                                if (fastCheck) {
                                    if(ElcaCV.ENABLE_COLLISION_EVENTS){
                                        MinecraftForge.EVENT_BUS.post(collisionEvent.init(entityIn, aabb, outList));
                                        if(!outList.isEmpty())return true;
                                    }else return true;
                                }
                            }
                        }
                    }
                }
            }
        } finally {}
        return !outList.isEmpty();
    }


    protected void getEntitiesInAABBexcludingNoCacheLimited(List<Entity> list, @Nullable Entity entityIn, AxisAlignedBB boundingBox,
                                                            @Nullable Predicate <? super Entity > predicate, int lim) {
        int k2 = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS) / 16.0D);
        int l2 = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS) / 16.0D);
        int i3 = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

        for (int j3 = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS) / 16.0D); j3 <= k2; ++j3) {
            for (int k3 = l2; k3 <= i3; ++k3) {
                if (this.isChunkLoaded(j3, k3, true)) {
                    this.getChunk(j3, k3).getEntitiesWithinAABBForEntityLimited(entityIn, boundingBox, list, predicate, lim);
                }
            }
        }
    }


    public void getEntitiesInAABBexcludingNoCache(List<Entity> list, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate <? super Entity > predicate) {
        int k2 = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS) / 16.0D);
        int l2 = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS) / 16.0D);
        int i3 = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

        for (int j3 = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS) / 16.0D); j3 <= k2; ++j3) {
            for (int k3 = l2; k3 <= i3; ++k3) {
                if (this.isChunkLoaded(j3, k3, true)) {
                    this.getChunk(j3, k3).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
                }
            }
        }
    }


    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        IBlockState iblockstate1 = this.getBlockState(pos);
        return iblockstate1.getBlock().shouldCheckWeakPower(iblockstate1, this, pos, facing) ? this.getStrongPower(pos) : iblockstate1.getWeakPower(this, pos, facing);
    }


    public void neighborChanged(BlockPos pos, final Block blockIn, BlockPos fromPos) {
        if (!this.isRemote) {
            IBlockState iblockstate = this.getBlockState(pos);
            try {
                iblockstate.neighborChanged(this, pos, blockIn, fromPos);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
                crashreportcategory.addDetail("Source block type", new ICrashReportDetail<String>() {
                    public String call() throws Exception {
                        try {
                            return String.format("ID #%d (%s // %s // %s)", Block.getIdFromBlock(blockIn), blockIn.getTranslationKey(), blockIn.getClass().getName(), blockIn.getRegistryName());
                        } catch (Throwable var2) {
                            return "ID #" + Block.getIdFromBlock(blockIn);
                        }
                    }
                });
                CrashReportCategory.addBlockInfo(crashreportcategory, pos, iblockstate);
                throw new ReportedException(crashreport);
            }
        }
    }


    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.getBlockState(pos).getStrongPower(this, pos, direction);
    }


    public int getLight(BlockPos pos, boolean checkNeighbors) {
        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
            if (checkNeighbors && this.getBlockState(pos).useNeighborBrightness()) {
                int i1 = this.getLight(pos.up(), false);
                int i = this.getLight(pos.east(), false);
                int j = this.getLight(pos.west(), false);
                int k = this.getLight(pos.south(), false);
                int l = this.getLight(pos.north(), false);

                if (i > i1) i1 = i;
                if (j > i1) i1 = j;
                if (k > i1) i1 = k;
                if (l > i1) i1 = l;

                return i1;
            } else if (pos.getY() < 0) {
                return 0;
            } else {
                if (pos.getY() >= 256) {
                    pos = new BlockPos(pos.getX(), 255, pos.getZ());
                }
                Chunk chunk = this.getChunk(pos);
                return chunk.getLightSubtracted(pos, this.skylightSubtracted);
            }
        } else {
            return 15;
        }
    }


    public boolean isAreaLoaded(BlockPos center, int radius) {
        return this.isAreaLoaded(center, radius, true);
    }


    public boolean isAirBlock(BlockPos pos) {
        return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
    }


    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        this.observedNeighborChanged(pos.west(),  blockType, pos);
        this.observedNeighborChanged(pos.east(),  blockType, pos);
        this.observedNeighborChanged(pos.down(),  blockType, pos);
        this.observedNeighborChanged(pos.up(),    blockType, pos);
        this.observedNeighborChanged(pos.north(), blockType, pos);
        this.observedNeighborChanged(pos.south(), blockType, pos);
    }


    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        int j2 = MathHelper.floor(bb.minX);
        int k2 = MathHelper.ceil(bb.maxX);
        int l2 = MathHelper.floor(bb.minY);
        int i3 = MathHelper.ceil(bb.maxY);
        int j3 = MathHelper.floor(bb.minZ);
        int k3 = MathHelper.ceil(bb.maxZ);

        if (!this.isAreaLoaded(j2, l2, j3, k2, i3, k3, true)) {
            return false;
        } else {
            boolean flag = false;
            Vec3d vec3d = Vec3d.ZERO;
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (int l3 = j2; l3 < k2; ++l3) {
                for (int i4 = l2; i4 < i3; ++i4) {
                    for (int j4 = j3; j4 < k3; ++j4) {
                        blockpos$pooledmutableblockpos.setPos(l3, i4, j4);
                        IBlockState iblockstate1 = this.getBlockState(blockpos$pooledmutableblockpos);
                        Block block = iblockstate1.getBlock();

                        Boolean result = block.isEntityInsideMaterial(this, blockpos$pooledmutableblockpos, iblockstate1, entityIn, (double)i3, materialIn, false);
                        if (result != null && result == true) {
                            // Forge: When requested call blocks modifyAcceleration method, and more importantly cause this method to return true, which results in an entity being "inWater"
                            flag = true;
                            vec3d = block.modifyAcceleration(this, blockpos$pooledmutableblockpos, entityIn, vec3d);
                            continue;
                        }
                        else if (result != null && result == false) continue;

                        if (iblockstate1.getMaterial() == materialIn) {
                            double d0 = (double)((float)(i4 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate1.getValue(BlockLiquid.LEVEL)).intValue()));

                            if ((double)i3 >= d0) {
                                flag = true;
                                vec3d = block.modifyAcceleration(this, blockpos$pooledmutableblockpos, entityIn, vec3d);
                            }
                        }
                    }
                }
            }
            blockpos$pooledmutableblockpos.release();

            if (vec3d.length() > 0.0D && entityIn.isPushedByWater()) {
                vec3d = vec3d.normalize();
                double d1 = 0.014D;
                entityIn.motionX += vec3d.x * 0.014D;
                entityIn.motionY += vec3d.y * 0.014D;
                entityIn.motionZ += vec3d.z * 0.014D;
            }
            return flag;
        }
    }


    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        int j2 = MathHelper.floor(bb.minX);
        int k2 = MathHelper.ceil(bb.maxX);
        int l2 = MathHelper.floor(bb.minY);
        int i3 = MathHelper.ceil(bb.maxY);
        int j3 = MathHelper.floor(bb.minZ);
        int k3 = MathHelper.ceil(bb.maxZ);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        for (int l3 = j2; l3 < k2; ++l3) {
            for (int i4 = l2; i4 < i3; ++i4) {
                for (int j4 = j3; j4 < k3; ++j4) {
                    IBlockState iblockstate1 = this.getBlockState(blockpos$pooledmutableblockpos.setPos(l3, i4, j4));
                    Boolean result = iblockstate1.getBlock().isAABBInsideMaterial(this, blockpos$pooledmutableblockpos, bb, materialIn);
                    if (result != null) {
                        if (!result) continue;
                        blockpos$pooledmutableblockpos.release();
                        return true;
                    }
                    if (iblockstate1.getMaterial() == materialIn) {
                        blockpos$pooledmutableblockpos.release();
                        return true;
                    }
                }
            }
        }
        blockpos$pooledmutableblockpos.release();
        return false;
    }
}
