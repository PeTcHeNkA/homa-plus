package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.ElcaConfig;
import plus.fserver.server.chunk.PlayerCMapSelector;
import plus.tson.utl.Te4HashSet;


public class PlayerChunkMap {
    private static final Predicate<EntityPlayerMP> NOT_SPECTATOR = p_apply_1_ -> p_apply_1_ != null && !p_apply_1_.isSpectator();
    private static final Predicate<EntityPlayerMP> CAN_GENERATE_CHUNKS = p_apply_1_ -> p_apply_1_ != null && (!p_apply_1_.isSpectator() || p_apply_1_.getServerWorld().getGameRules().getBoolean("spectatorsGenerateChunks"));

    private final WorldServer world;
    private final List<EntityPlayerMP> players = Lists.newArrayList();
    private final Long2ObjectMap<PlayerChunkMapEntry> entryMap = new Long2ObjectOpenHashMap<>(4096);
    private Set<PlayerChunkMapEntry> dirtyEntries = new Te4HashSet<>();
    private final List<PlayerChunkMapEntry> pendingSendToPlayers = Lists.newLinkedList();
    private final List<PlayerChunkMapEntry> entriesWithoutChunks = Lists.newLinkedList();
    private final List<PlayerChunkMapEntry> entries = Lists.newArrayList();

    private int playerViewRadius;
    private long previousTotalWorldTime;
    private boolean sortMissingChunks = true;
    private boolean sortSendToPlayers = true;
    private int nextRadius = -1;

    private final Int2ObjectArrayMap<PlayerCMapSelector> selectors = new Int2ObjectArrayMap<>();

    public PlayerChunkMap(WorldServer serverWorld) {
        this.world = serverWorld;
        this.setPlayerViewRadiusU(serverWorld.getMinecraftServer().getPlayerList().getViewDistance());
    }


    public WorldServer getWorldServer() {
        return this.world;
    }


    //todo сделать динамическую выборку
    public Iterator<Chunk> getChunkIterator() {
        return new AbstractIterator<Chunk>() {
            private final Iterator<PlayerChunkMapEntry> iterator = entries.iterator();
            private final int radius =
                    ((world.getTotalWorldTime()&1)==1)?ElcaConfig.CHUNK_TICK_RADIUS_1:ElcaConfig.CHUNK_TICK_RADIUS_2;
            protected Chunk computeNext() {
                while (true) {
                    if (iterator.hasNext()) {//todo
                        PlayerChunkMapEntry playerchunkmapentry = iterator.next();
                        Chunk chunk = playerchunkmapentry.getChunk();
                        if (chunk == null) continue;
                        if (!chunk.isLightPopulated() && chunk.isTerrainPopulated())
                            return chunk;

                        if (!chunk.wasTicked())
                            return chunk;

                        if (!playerchunkmapentry.hasPlayerMatchingInRange(radius, PlayerChunkMap.NOT_SPECTATOR)) {
                            continue;
                        }
                        return chunk;
                    }
                    return this.endOfData();
                }
            }
        };
    }


    public void tick() {
        long i = this.world.getTotalWorldTime();
        if(nextRadius != -1){
            setPlayerViewRadiusU(nextRadius);
        }

        if (i - this.previousTotalWorldTime > 8000L) {
            this.previousTotalWorldTime = i;
            for (int j = 0; j < this.entries.size(); ++j) {
                PlayerChunkMapEntry playerchunkmapentry = this.entries.get(j);
                playerchunkmapentry.update();
                playerchunkmapentry.updateChunkInhabitedTime();
            }
        }

        //todo java.util.ConcurrentModificationException
        //todo fixed, used other set. maybe not uptimal (memory)
        if (!this.dirtyEntries.isEmpty()) {
            Set<PlayerChunkMapEntry> old = dirtyEntries;
            dirtyEntries = new Te4HashSet<>();
            for (PlayerChunkMapEntry entry:old){
                entry.update();
            }
        }

        if (this.sortMissingChunks && i % 4L == 0L) {
            this.sortMissingChunks = false;
            this.entriesWithoutChunks.sort((e1, e2) ->
                    ComparisonChain.start().compare(
                            e1.getClosestPlayerDistance(),
                            e2.getClosestPlayerDistance()).result()
            );
        }

        if (this.sortSendToPlayers && i % 4L == 2L) {
            this.sortSendToPlayers = false;
            this.pendingSendToPlayers.sort((e1, e2) ->
                    ComparisonChain.start().compare(
                            e1.getClosestPlayerDistance(),
                            e2.getClosestPlayerDistance()).result()
            );
        }

        if (!this.entriesWithoutChunks.isEmpty()) {
            long l = System.nanoTime() + 50000000L;
            int k = 49;
            Iterator<PlayerChunkMapEntry> iterator = this.entriesWithoutChunks.iterator();

            while (iterator.hasNext()) {
                PlayerChunkMapEntry entry = iterator.next();

                if (entry.getChunk() == null) {
                    boolean flag = entry.hasPlayerMatching(CAN_GENERATE_CHUNKS);

                    if (entry.providePlayerChunk(flag)) {
                        iterator.remove();
                        if (entry.sendToPlayers()) {
                            this.pendingSendToPlayers.remove(entry);
                        }
                        --k;
                        if (k < 0 || System.nanoTime() > l) {
                            break;
                        }
                    }
                }
            }
        }

        if (!this.pendingSendToPlayers.isEmpty()) {
            int i1 = 81;
            Iterator<PlayerChunkMapEntry> iterator1 = this.pendingSendToPlayers.iterator();

            while (iterator1.hasNext()) {
                PlayerChunkMapEntry entry = iterator1.next();

                if (entry.sendToPlayers()) {
                    iterator1.remove();
                    --i1;

                    if (i1 < 0) {
                        break;
                    }
                }
            }
        }

        if (this.players.isEmpty()) {
            //WorldProvider worldprovider = this.world.provider;

            //if (!worldprovider.canRespawnHere()) {
            this.world.getChunkProvider().queueUnloadAll();
            //}
        }
    }


    public boolean contains(int chunkX, int chunkZ) {
        long i = getIndex(chunkX, chunkZ);
        return this.entryMap.get(i) != null;
    }


    @Nullable
    public PlayerChunkMapEntry getEntry(int x, int z) {
        return this.entryMap.get(getIndex(x, z));
    }


    public PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ) {
        long i = getIndex(chunkX, chunkZ);
        PlayerChunkMapEntry playerchunkmapentry = this.entryMap.get(i);

        if (playerchunkmapentry == null) {
            playerchunkmapentry = new PlayerChunkMapEntry(this, chunkX, chunkZ);
            this.entryMap.put(i, playerchunkmapentry);
            this.entries.add(playerchunkmapentry);

            if (playerchunkmapentry.getChunk() == null) {
                this.entriesWithoutChunks.add(playerchunkmapentry);
            }

            if (!playerchunkmapentry.sendToPlayers()) {
                this.pendingSendToPlayers.add(playerchunkmapentry);
            }
        }
        return playerchunkmapentry;
    }


    public void markBlockForUpdate(BlockPos pos) {
        int i = pos.getX() >> 4;
        int j = pos.getZ() >> 4;
        PlayerChunkMapEntry playerchunkmapentry = this.getEntry(i, j);
        if (playerchunkmapentry != null) {
            playerchunkmapentry.blockChanged(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        }
    }


    private PlayerCMapSelector getBy(int radius){
        if(radius>playerViewRadius)radius = playerViewRadius;
        if(radius<3)radius = 3;
        PlayerCMapSelector sel = selectors.get(radius);
        if(sel == null){
            selectors.put(radius, sel = new PlayerCMapSelector(radius));
        }
        return sel;
    }


    //todo
    public void addPlayer(EntityPlayerMP player) {
        PlayerCMapSelector sel = getBy(player.prevRenderRadius = player.renderRadius);
        sel.addPlayer(this, player);
//        int i = (int)player.posX >> 4;
//        int j = (int)player.posZ >> 4;
//        player.managedPosX = player.posX;
//        player.managedPosZ = player.posZ;
//
//        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
//            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
//                this.getOrCreateEntry(k, l).addPlayer(player);
//            }
//        }
        this.players.add(player);
        this.markSortPending();
    }


    public void removePlayer(EntityPlayerMP player) {
        int i = (int)player.managedPosX >> 4;
        int j = (int)player.managedPosZ >> 4;

        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                PlayerChunkMapEntry playerchunkmapentry = this.getEntry(k, l);
                if (playerchunkmapentry != null) {
                    playerchunkmapentry.removePlayer(player);
                }
            }
        }
        this.players.remove(player);
        this.markSortPending();
    }


    private boolean overlaps(int x1, int z1, int x2, int z2, int radius) {
        int i = x1 - x2;
        int j = z1 - z2;

        if (i >= -radius && i <= radius) {
            return j >= -radius && j <= radius;
        } else {
            return false;
        }
    }


    public void updateMovingPlayer(EntityPlayerMP player) {
        PlayerCMapSelector sel = getBy(player.renderRadius);
        sel.updateMovingPlayer(this, player);
//        int i = (int)player.posX >> 4;
//        int j = (int)player.posZ >> 4;
//        double d0 = player.managedPosX - player.posX;
//        double d1 = player.managedPosZ - player.posZ;
//        double d2 = d0 * d0 + d1 * d1;
//
//        if (d2 >= 64.0D) {
//            int k = (int)player.managedPosX >> 4;
//            int l = (int)player.managedPosZ >> 4;
//            int i1 = this.playerViewRadius;
//            int j1 = i - k;
//            int k1 = j - l;
//
//            if (j1 != 0 || k1 != 0) {
//                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
//                    for (int i2 = j - i1; i2 <= j + i1; ++i2) {
//                        if (!this.overlaps(l1, i2, k, l, i1)) {
//                            this.getOrCreateEntry(l1, i2).addPlayer(player);
//                        }
//
//                        if (!this.overlaps(l1 - j1, i2 - k1, i, j, i1)) {
//                            PlayerChunkMapEntry playerchunkmapentry = this.getEntry(l1 - j1, i2 - k1);
//
//                            if (playerchunkmapentry != null)
//                            {
//                                playerchunkmapentry.removePlayer(player);
//                            }
//                        }
//                    }
//                }
//                player.managedPosX = player.posX;
//                player.managedPosZ = player.posZ;
//                this.markSortPending();
//            }
//        }
    }


    public boolean isPlayerWatchingChunk(EntityPlayerMP player, int chunkX, int chunkZ) {
        PlayerChunkMapEntry playerchunkmapentry = this.getEntry(chunkX, chunkZ);
        return playerchunkmapentry != null && playerchunkmapentry.containsPlayer(player) && playerchunkmapentry.isSentToPlayers();
    }


    public void setPlayerViewRadius(int radius) {
        nextRadius = radByFormula(radius);
    }


    public static int radByFormula(int radius){
        return radius;
        //return (radius>>1)+2;
    }


    public void setPlayerViewRadiusU(int radius) {
        radius = MathHelper.clamp(radius, 3, 32);

        if (radius != this.playerViewRadius) {
            int i = radius - this.playerViewRadius;

            for (EntityPlayerMP entityplayermp : Lists.newArrayList(this.players)) {
                int j = (int)entityplayermp.posX >> 4;
                int k = (int)entityplayermp.posZ >> 4;

                if (i > 0) {
                    for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                        for (int k1 = k - radius; k1 <= k + radius; ++k1) {
                            PlayerChunkMapEntry playerchunkmapentry = this.getOrCreateEntry(j1, k1);

                            if (!playerchunkmapentry.containsPlayer(entityplayermp)) {
                                playerchunkmapentry.addPlayer(entityplayermp);
                            }
                        }
                    }
                } else {
                    for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                        for (int i1 = k - this.playerViewRadius; i1 <= k + this.playerViewRadius; ++i1) {
                            if (!this.overlaps(l, i1, j, k, radius)) {
                                this.getOrCreateEntry(l, i1).removePlayer(entityplayermp);
                            }
                        }
                    }
                }
            }
            this.playerViewRadius = radius;
            this.markSortPending();
        }
        nextRadius = -1;
    }


    public void markSortPending() {
        this.sortMissingChunks = true;
        this.sortSendToPlayers = true;
    }


    public static int getFurthestViewableBlock(int distance) {
        return distance * 16 - 16;
    }


    private static long getIndex(int chunkX, int chunkZ) {
        return (long)chunkX + 2147483647L | (long)chunkZ + 2147483647L << 32;
    }


    public void entryChanged(PlayerChunkMapEntry entry) {
        this.dirtyEntries.add(entry);
    }


    public void removeEntry(PlayerChunkMapEntry entry) {
        ChunkPos chunkpos = entry.getPos();
        long i = getIndex(chunkpos.x, chunkpos.z);
        entry.updateChunkInhabitedTime();
        this.entryMap.remove(i);
        this.entries.remove(entry);
        this.dirtyEntries.remove(entry);
        this.pendingSendToPlayers.remove(entry);
        this.entriesWithoutChunks.remove(entry);
        Chunk chunk = entry.getChunk();

        if (chunk != null) {
            this.getWorldServer().getChunkProvider().queueUnload(chunk);
        }
    }


    public int getMaxPlayerViewRadius(){
        return playerViewRadius;
    }
}