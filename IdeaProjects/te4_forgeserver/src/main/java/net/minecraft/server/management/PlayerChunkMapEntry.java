package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.fserver.utls.collections.ArrayListE6;


public class PlayerChunkMapEntry extends ArrayListE6<EntityPlayerMP> implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger();
    private final PlayerChunkMap playerChunkMap;
    //private final List<EntityPlayerMP> players = new ArrayListE6<>();
    public final int posX, posZ;
    private short[] changedBlocks = new short[ElcaConfig.MIN_CHANGED_BLOCKS_BUF];
    @Nullable
    private Chunk chunk;
    private int changes, changedSectionFilter;
    private long lastUpdateInhabitedTime;
    private boolean sentToPlayers, loading = true;

    public PlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
        this.playerChunkMap = mapIn;
        this.posX = chunkX;this.posZ = chunkZ;
        mapIn.getWorldServer().getChunkProvider().loadChunk(chunkX, chunkZ, this);
    }


    @Override
    public void run() {
        chunk = PlayerChunkMapEntry.this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(posX, posZ);
        if(chunk != null)chunk.setUnloadQueued(false);
        loading = false;
    }


    @Deprecated
    public ChunkPos getPos() {
        return new ChunkPos(posX, posZ);
    }


    public void addPlayer(EntityPlayerMP player) {
        if (containsRef(player)) {
            LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", player, posX, posZ);
        } else {
            if (isEmpty()) {
                this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
            }
            addF(player);
            if (this.sentToPlayers) {
                this.sendToPlayer(player);
                MinecraftForge.EVENT_BUS.post(player.world.chunkWatchEvent.init(chunk, player));
                //MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(this.chunk, player));
            }
        }
    }


    public void removePlayer(EntityPlayerMP player) {
        if (removeRef(player)) {
            if (this.chunk == null) {

                if (isEmpty()) {
                    if (this.loading) TChunkExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), posX, posZ, this);
                    this.playerChunkMap.removeEntry(this);
                }
                return;
            }
            if (this.sentToPlayers) {
                player.connection.netManager.addToQueueE(new SPacketUnloadChunk(posX, posZ));
            }

            MinecraftForge.EVENT_BUS.post(player.world.chunkUnWatchEvent.init(chunk, player));
            //MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.UnWatch(this.chunk, player));
            if (isEmpty()) {
                this.playerChunkMap.removeEntry(this);
            }
        }
    }


    public boolean providePlayerChunk(boolean canGenerate) {
        if (this.loading) return false;
        if (this.chunk != null) {
            return true;
        } else {
            if (canGenerate) {
                this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().provideChunk(posX, posZ);
            } else {
                this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(posX, posZ);
            }
            return this.chunk != null;
        }
    }


    public boolean sendToPlayers() {
        if (this.sentToPlayers) {
            return true;
        } else if (this.chunk == null) {
            return false;
        } else if (!this.chunk.isPopulated()) {
            return false;
        } else {
            if (isEmpty()) {
                this.changes = 0;
                this.changedSectionFilter = 0;
                this.sentToPlayers = true;
                return true;
            } else try {
                Packet<?> packet = new SPacketChunkData(this.chunk, 65535);
                for (int i=0;i<size;i++) {
                    EntityPlayerMP player = get(i);
                    player.connection.netManager.addToQueueE(packet);
                    this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(player, this.chunk);

                    MinecraftForge.EVENT_BUS.post(player.world.chunkWatchEvent.init(chunk, player));
                    //MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(this.chunk, player));
                }
                this.changes = 0;
                this.changedSectionFilter = 0;
                this.sentToPlayers = true;
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
            player.connection.netManager.addToQueueE(new SPacketChunkData(this.chunk, 65535));
            this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(player, this.chunk);
        }
    }


    public void updateChunkInhabitedTime() {
        long i = this.playerChunkMap.getWorldServer().getTotalWorldTime();
        if (this.chunk != null) {
            this.chunk.setInhabitedTime(this.chunk.getInhabitedTime() + i - this.lastUpdateInhabitedTime);
        }
        this.lastUpdateInhabitedTime = i;
    }


    public void blockChanged(int x, int y, int z) {
        if (this.sentToPlayers) {
            if (this.changes == 0) {
                this.playerChunkMap.entryChanged(this);
            }
            this.changedSectionFilter |= 1 << (y >> 4);
            short short1 = (short) (x << 12 | z << 8 | y);

            for (int i = 0; i < this.changes; ++i) {
                if (this.changedBlocks[i] == short1) {
                    return;
                }
            }
            if (this.changes == this.changedBlocks.length)
                this.changedBlocks = java.util.Arrays.copyOf(this.changedBlocks, this.changedBlocks.length << 1);
            this.changedBlocks[this.changes++] = short1;
        }
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


    @SuppressWarnings("unused")
    public void update() {
        if (this.sentToPlayers && this.chunk != null) {
            if (this.changes != 0) {
                if (this.changes == 1) {
                    int i = (this.changedBlocks[0] >> 12 & 15) + posX << 4;
                    int j = this.changedBlocks[0] & 255;
                    int k = (this.changedBlocks[0] >> 8 & 15) + posZ << 4;
                    BlockPos blockpos = new BlockPos(i, j, k);
                    this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), blockpos));
                    net.minecraft.block.state.IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos);

                    if (state.getBlock().hasTileEntity(state)) {
                        this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos));
                    }
                }
                else if (this.changes >= net.minecraftforge.common.ForgeModContainer.clumpingThreshold) {
                    this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
                    //TODO: FDix Mojang's fuckup to modded by combining all TE data into the chunk data packet... seriously... packet size explosion!
                } else {
                    this.sendPacket(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));
                    //} Keep this in the else until we figure out a fix for mojang's derpitude on the data packet so we don't double send crap.
                    //{// Forge: Send only the tile entities that are updated, Adding this brace lets us keep the indent and the patch small
                    for (int l = 0; l < this.changes; ++l)
                    {
                        int i1 = (this.changedBlocks[l] >> 12 & 15) + posX << 4;
                        int j1 = this.changedBlocks[l] & 255;
                        int k1 = (this.changedBlocks[l] >> 8 & 15) + posZ << 4;
                        BlockPos blockpos1 = new BlockPos(i1, j1, k1);
                        net.minecraft.block.state.IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos1);

                        if (state.getBlock().hasTileEntity(state)) {
                            this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos1));
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
        return Iterables.tryFind(this, predicate).isPresent();
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
        double d2 = ((posZ << 4) + 8) - entityIn.posX;
        double d3 = ((posZ << 4) + 8) - entityIn.posZ;
        return d2 * d2 + d3 * d3;
    }


    public List<EntityPlayerMP> getWatchingPlayers() {
        return isSentToPlayers() ? java.util.Collections.unmodifiableList(this) : java.util.Collections.emptyList();
    }
}