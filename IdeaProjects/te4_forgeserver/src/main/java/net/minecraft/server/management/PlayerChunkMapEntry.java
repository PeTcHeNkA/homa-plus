package net.minecraft.server.management;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaCV;
import plus.fserver.ElcaConfig;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.fserver.utls.collections.EntityList;

import static net.minecraft.server.management.PlayerChunkMap.CAN_GENERATE_CHUNKS;


public class PlayerChunkMapEntry extends EntityList<EntityPlayerMP> implements Consumer<Chunk> {
    public static final Comparator<PlayerChunkMapEntry> COMPARATOR = (o1, o2) -> {
        double d1 = o1.getClosestPlayerDistance();
        double d2 = o2.getClosestPlayerDistance();
        if (d1 < d2) return -1;
        if (d1 > d2) return 1;
        return 0;
    };

    private static final Logger LOGGER = LogManager.getLogger();
    public final int posX, posZ;
    private short[] changedBlocks = null;
    @Nullable
    private Chunk chunk;
    private int changes, changedSectionFilter;
    private long lastUpdateInhabitedTime;
    private boolean sentToPlayers = false, loading = true;

    public PlayerChunkMapEntry(Chunk chunk){
        this.posX = chunk.x;
        this.posZ = chunk.z;
        accept(chunk);
    }


    public PlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
        //this.playerChunkMap = mapIn;
        this.posX = chunkX;
        this.posZ = chunkZ;

        if(!ElcaCV.DEBUG_NO_SERVER){
            mapIn.getProvider().loadChunk(chunkX, chunkZ, this);
        }
    }


    @Override
    public void accept(@Nullable Chunk chunk) {
        if(chunk != null){
            this.chunk = chunk;
            chunk.chunkMapEntry = this;
            chunk.setUnloadQueued(false);
        }
        loading = false;
    }


    @Deprecated
    public ChunkPos getPos() {
        return new ChunkPos(posX, posZ);
    }


    public void addPlayer(PlayerChunkMap map, EntityPlayerMP player) {
        if (!containsRef(player)) {
            if (isEmpty()) {
                this.lastUpdateInhabitedTime = map.getWorldServer().getTotalWorldTime();
            }
            addF(player);
            if (this.sentToPlayers) {
                this.sendToPlayer(player);
                player.world.chunkWatchEvent.post(chunk, player);
                //MinecraftForge.EVENT_BUS.postF(player.world.chunkWatchEvent.init(chunk, player));
            }
        }
    }


    public void removePlayer(PlayerChunkMap map, EntityPlayerMP player) {
        if (removeRef(player)) {
            if (this.chunk == null) {
                if (isEmpty()) {
                    if (this.loading) TChunkExecutor.dropQueuedChunkLoad(map.getWorldServer(), posX, posZ, this);
                    map.removeEntry(this);
                }
                return;
            }
            if (this.sentToPlayers) {
                player.connection.netManager.addToQueueN(new SPacketUnloadChunk(posX, posZ));
            }

            player.world.chunkUnWatchEvent.post(chunk, player);
            //MinecraftForge.EVENT_BUS.postF(player.world.chunkUnWatchEvent.init(chunk, player));
            if (isEmpty()) {
                map.removeEntry(this);
                this.sentToPlayers = false;
                this.chunk = null;
            }
        }
    }


    public boolean providePlayerChunk(PlayerChunkMap map) {
        return providePlayerChunk(map, this.hasPlayerMatching(CAN_GENERATE_CHUNKS));
    }


    public boolean providePlayerChunk(PlayerChunkMap map, boolean canGenerate) {
        if (this.loading) return false;
        if (this.chunk != null) {
            return true;
        } else {
            if (canGenerate) {
                this.chunk = map.getProvider().provideChunk(posX, posZ);
            } else {
                this.chunk = map.getProvider().loadChunk(posX, posZ);
            }
            return this.chunk != null;
        }
    }


    public boolean sendToPlayers(PlayerChunkMap map) {
        if (this.sentToPlayers)return true;
        Chunk chunk;
        if ((chunk = this.chunk) == null) {
            return false;
        } else if (!chunk.maybeSend()) {
            return false;
        } else {
            if (isEmpty()) {
                this.changes = 0;
                this.changedSectionFilter = 0;
                this.sentToPlayers = true;
                chunk.setSentToPlayers(true);
                return true;
            } else try {
                if(!chunk.isLightPopulated()){
                    chunk.checkLight();
                }
                int size;
                if((size = this.size) > 0){
                    SPacketChunkData packet = new SPacketChunkData(chunk, 65535, size==1);

                    map.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(this, chunk);

                    for (int i = 0; i < size; i++) {
                        EntityPlayerMP player = super.get(i);
                        player.connection.netManager.addToQueueN(packet);
                        player.world.chunkWatchEvent.post(chunk, player);
                    }
                }
                this.changes = changedSectionFilter = 0;
                this.sentToPlayers = true;
                chunk.setSentToPlayers(true);
                return true;
            } catch (Exception e){
                WorldServer world = ((WorldServer)chunk.getWorld());
                world.getMinecraftServer().ticker().onError(world, e);
                return false;
            }
        }
    }


    public void sendToPlayer(EntityPlayerMP player) {
        if (this.sentToPlayers) {
            Chunk chunk = this.chunk;
            player.connection.netManager.sendPacketE(new SPacketChunkData(chunk, 65535, true));
            EntityTracker.sendLeashedEntitiesInChunkS(player, chunk);
        }
    }


    public void updateChunkInhabitedTime(PlayerChunkMap map) {
        long i = map.getWorldServer().getTotalWorldTime();
        Chunk chunk;
        if ((chunk = this.chunk) != null) {
            chunk.setInhabitedTime(this.chunk.getInhabitedTime() + i - this.lastUpdateInhabitedTime);
        }
        this.lastUpdateInhabitedTime = i;
    }


    public void blockChanged(PlayerChunkMap map, int x, int y, int z) {
        if (this.sentToPlayers) {
            int changes;
            if ((changes = this.changes) == 0) {
                map.entryChanged(this);
            }
            if(changes >= ForgeModContainer.clumpingThreshold)return;
            this.changedSectionFilter |= 1 << (y >> 4);
            short codedPos = (short) (x << 12 | z << 8 | y);

            short[] changedBlocks = this.changedBlocks;
            if(changedBlocks == null){
                changedBlocks = this.changedBlocks = new short[ElcaConfig.MIN_CHANGED_BLOCKS_BUF];

                changedBlocks[0] = codedPos;
                this.changes = 1;
            } else {
                for (int i = 0; i < changes; ++i) {
                    if (changedBlocks[i] == codedPos) return;
                }
                blockChanged(codedPos);
            }
        }
    }


    protected void blockChanged(short codedPos){
        short[] changedBlocks = this.changedBlocks;
        if (changes == changedBlocks.length)
            this.changedBlocks = changedBlocks = Arrays.copyOf(changedBlocks, changedBlocks.length << 1);

        changedBlocks[this.changes++] = codedPos;
    }


    public void sendPacket(Packet<?> packetIn) {
        if (this.sentToPlayers && !isEmpty()) {
            NetworkManager manager = get(0).connection.netManager;
            manager.exec(() -> {
                manager.sendPacketSync(packetIn);
                for (int i = 1; i < size; ++i) {
                    (get(i)).connection.netManager.sendPacketSync(packetIn);
                }
            });
        }
    }


    //todo maybe use Elca cache? MC -> Netty
    public void update(PlayerChunkMap map) {
        Chunk chunk;
        if (this.sentToPlayers && (chunk = this.chunk) != null) {
            if (this.changes != 0) {
                if (this.changes == 1) {
                    short cPos = this.changedBlocks[0];

                    int cX = (cPos >> 12 & 15) + (posX << 4);
                    int cY =  cPos & 255;
                    int cZ = (cPos >> 8 & 15) + (posZ << 4);

                    IBlockState state = chunk.getBlockStateUnchecked(cX, cY, cZ);
                    BlockPos blockpos;
                    this.sendPacket(new SPacketBlockChange(state, blockpos = new BlockPos(cX, cY, cZ)));

                    if (state.hasTile()) {
                        this.sendBlockEntity(map.getWorldServer().getTileEntity(blockpos));
                    }
                }
                else if (this.changes >= ForgeModContainer.clumpingThreshold) {
                    this.sendPacket(new SPacketChunkData(chunk, this.changedSectionFilter, true));
                    //TODO: FDix Mojang's fuckup to modded by combining all TE data into the chunk data packet... seriously... packet size explosion!
                } else l1:{
                    short[] changedBlocks = this.changedBlocks;
                    if(changedBlocks == null)break l1;

                    WorldServer world = map.getWorldServer();

                    this.sendPacket(new SPacketMultiBlockChange(this.changes, changedBlocks, chunk));
                    //} Keep this in the else until we figure out a fix for mojang's derpitude on the
                    // data packet so we don't double send crap.
                    //{// Forge: Send only the tile entities that are updated, Adding this brace
                    // lets us keep the indent and the patch small
                    BlockPos.MutableBlockPos pos = world.localTempPos;
                    final int posX = this.posX, posZ = this.posZ;

                    for (int l = 0; l < this.changes; ++l) {
                        short cPos = changedBlocks[l];

                        int cX = (cPos >> 12 & 15) + (posX << 4);
                        int cY =  cPos & 255;
                        int cZ = (cPos >> 8 & 15) + (posZ << 4);

                        IBlockState state = chunk.getBlockStateUnchecked(cX, cY, cZ);

                        if (state.hasTile()) {
                            this.sendBlockEntity(world.getTileEntity(pos.setPos(cX, cY, cZ)));
                        }
                    }
                }
                this.changes = 0;
                this.changedSectionFilter = 0;
            }
        }
    }


    private void sendBlockEntity(@Nullable TileEntity be) {
        if (be != null) {
            SPacketUpdateTileEntity spacketupdatetileentity = be.getUpdatePacket();
            if (spacketupdatetileentity != null) {
                this.sendPacket(spacketupdatetileentity);
            }
        }
    }


    public boolean containsPlayer(EntityPlayerMP player) {
        return containsRef(player);
    }


    public boolean hasPlayerMatching(Predicate<EntityPlayerMP> predicate) {
        for (int i = 0, s = size;i<s;i++) {
            if(predicate.apply(super.get(i)))return true;
        }
        return false;
    }


    public boolean hasPlayerMatchingInRange(double range, Predicate<EntityPlayerMP> predicate) {
        range *= range;
        for (int j = size, i = 0; i < j; ++i) {
            EntityPlayerMP entityplayermp = get(i);
            if (predicate.apply(entityplayermp) && getDistanceSq(entityplayermp) < range) {
                return true;
            }
        }
        return false;
    }


    public boolean hasCommonPlayerInRangeSq(double range) {
        for (int j = size, i = 0; i < j; ++i) {
            EntityPlayerMP player = get(i);
            if (!player.isSpectator() && getDistanceSq(player) < range) return true;
        }
        return false;
    }


    public boolean isSentToPlayers() {
        return this.sentToPlayers;
    }


    @Nullable
    public Chunk getChunk() {
        return this.chunk;
    }


    public double getClosestPlayerDistance() {
        double d0 = Double.MAX_VALUE;
        for (EntityPlayerMP entityplayermp : this) {
            double d1 = getDistanceSq(entityplayermp);
            if (d1 < d0) d0 = d1;
        }
        return d0;
    }


    public double getDistanceSq(Entity entityIn) {
        double d2 = ((posX << 4) + 8) - entityIn.posX;
        double d3 = ((posZ << 4) + 8) - entityIn.posZ;
        return d2 * d2 + d3 * d3;
    }


    @Override
    public int hashCode() {
        return posX ^ posZ;
    }


    @Override
    public Entity[] newArrayAuto() {
        Entity[] data;
        if((data = this.data) == EMPTY_ARR)return new Entity[1];
        return expand(data, data.length+2);
    }


    public List<EntityPlayerMP> getWatchingPlayers() {
        if(isSentToPlayers())return Collections.unmodifiableList(this);
        return Collections.emptyList();
    }
}