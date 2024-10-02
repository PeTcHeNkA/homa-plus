package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.server.chunk.PlayerCMapSelector;
import plus.tson.utl.Te4HashSet;


public class PlayerChunkMap{
    protected static final Int2ObjectOpenHashMap<PlayerCMapSelector> SELECTORS = new Int2ObjectOpenHashMap<>(7);
    public static final Predicate<EntityPlayerMP> NOT_SPECTATOR = player -> player != null && !player.isSpectator();
    public static final Predicate<EntityPlayerMP> CAN_GENERATE_CHUNKS = player -> player != null && (!player.isSpectator() || player.getServerWorld().getGameRules().getBoolean("spectatorsGenerateChunks"));

    private final WorldServer world;
    private final ChunkProviderServer provider;
    private Te4HashSet<PlayerChunkMapEntry> dirtyEntries = new Te4HashSet<>(7);
    private final List<PlayerChunkMapEntry> pendingSendToPlayers = Lists.newLinkedList();
    private final List<PlayerChunkMapEntry> entriesWithoutChunks = Lists.newLinkedList();

    private int playerViewRadius;
    private long previousTotalWorldTime;
    private boolean sortMissingChunks = true;
    private boolean sortSendToPlayers = true;
    private int nextRadius = -1;

    public PlayerChunkMap(WorldServer serverWorld) {
        this.world = serverWorld;
        this.provider = serverWorld.getChunkProvider();
        this.setPlayerViewRadiusU(serverWorld.getMinecraftServer().getPlayerList().getViewDistance());
    }


    public ChunkProviderServer getProvider() {
        return provider;
    }


    public WorldServer getWorldServer() {
        return this.world;
    }
//
//
//    public ChunkIterator getChunkIterator() {
//        return new ChunkIterator(entries, ElcaConfig.CHUNK_TICK_RADIUS_2);
//    }
//
//
//    public ChunkIterator getChunkIterator(int radius) {
//        return new ChunkIterator(entries, radius);
//    }
//
//
//    public Iterable<PlayerChunkMapEntry> getEntries() {
//        return world.chunkProviderServer.forChunks(Integer.MAX_VALUE);
//    }


    public void tick() {
        long i = this.world.getTotalWorldTime();
        if(nextRadius != -1){
            setPlayerViewRadiusU(nextRadius);
        }

        if (i - this.previousTotalWorldTime > 8000L) {
            this.previousTotalWorldTime = i;

            provider.forEntries(entry -> {
                entry.update(this);
                entry.updateChunkInhabitedTime(this);
            });
        }

        //todo java.util.ConcurrentModificationException
        //todo fixed, used other set. maybe not uptimal (memory)
        if (!this.dirtyEntries.isEmpty()) {
            Te4HashSet<PlayerChunkMapEntry> old = dirtyEntries;
            dirtyEntries = new Te4HashSet<>(7);
            old.forEach(entry -> entry.update(this));
        }


        List<PlayerChunkMapEntry> pendingSendToPlayers = this.pendingSendToPlayers;
        List<PlayerChunkMapEntry> entriesWithoutChunks = this.entriesWithoutChunks;

        if (this.sortMissingChunks && i % 4L == 0L) {
            this.sortMissingChunks = false;
            entriesWithoutChunks.sort(PlayerChunkMapEntry.COMPARATOR);
        }

        if (this.sortSendToPlayers && i % 4L == 2L) {
            this.sortSendToPlayers = false;
            pendingSendToPlayers.sort(PlayerChunkMapEntry.COMPARATOR);
        }

        if (!entriesWithoutChunks.isEmpty()) {
            long l = System.nanoTime() + 50000000L;
            int k = 49;
            Iterator<PlayerChunkMapEntry> iterator = entriesWithoutChunks.iterator();

            while (iterator.hasNext()) {
                final PlayerChunkMapEntry entry;
                if ((entry = iterator.next()).getChunk() == null) {

                    if (entry.providePlayerChunk(this, entry.hasPlayerMatching(CAN_GENERATE_CHUNKS))) {
                        iterator.remove();
                        if (entry.sendToPlayers(this)) {
                            pendingSendToPlayers.remove(entry);
                        }
                        --k;
                        if (k < 0 || System.nanoTime() > l) {
                            break;
                        }
                    }
                }
            }
        }

        if (!pendingSendToPlayers.isEmpty()) {
            int i1 = 81;
            Iterator<PlayerChunkMapEntry> itr = pendingSendToPlayers.iterator();

            while (itr.hasNext()) {
                if (itr.next().sendToPlayers(this)) {
                    itr.remove();
                    if (--i1 < 0) break;
                }
            }
        }

        if (world.curPlayerEntities.isEmpty()) unloadAll();
    }


    protected void unloadAll(){
        world.getChunkProvider().queueUnloadAll();
    }


    @Nullable
    public PlayerChunkMapEntry getEntry(int x, int z) {
        return provider.getEntry(x, z);
    }


    public PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ) {
        PlayerChunkMapEntry entry = provider.getOrCreateEntry(chunkX, chunkZ);

        if (entry.isNewEntry()) {
            entry.markCreated();

            if (entry.getChunk() == null) {
                this.entriesWithoutChunks.add(entry);
            }

            if (!entry.sendToPlayers(this)) {
                this.pendingSendToPlayers.add(entry);
            }
        }
        return entry;
    }


    public void removeEntry(PlayerChunkMapEntry entry) {
        entry.updateChunkInhabitedTime(this);
        //this.entries.removeRef(entry);
        this.pendingSendToPlayers.remove(entry);
        this.entriesWithoutChunks.remove(entry);
        Chunk chunk = entry.getChunk();

        if (chunk != null) {
            chunk.pcmEntry = null;
            WorldServer world = (WorldServer) chunk.getWorld();
            world.getChunkProvider().queueUnload(chunk);
        }
    }


    public void markBlockForUpdate(BlockPos pos) {
        int px, pz;
        PlayerChunkMapEntry entry = provider.getLoadedEntry((px = pos.getX()) >> 4, (pz = pos.getZ()) >> 4);
        if (entry != null) entry.blockChanged(this, px & 15, pos.getY(), pz & 15);
    }


    public final PlayerCMapSelector getBy(int radius){
        if(radius>playerViewRadius)radius = playerViewRadius;
        if(radius<3)radius = 3;
        PlayerCMapSelector sel = SELECTORS.get(radius);
        if(sel == null){
            synchronized (SELECTORS) {
                SELECTORS.put(radius, sel = new PlayerCMapSelector(radius));
            }
        }
        return sel;
    }


    public void addPlayer(EntityPlayerMP player) {
        PlayerCMapSelector sel = getBy(player.prevRenderRadius = player.renderRadius);
        sel.addPlayer(this, player);
        //players.addF(player);
        this.markSortPending();
    }


    public void removePlayer(EntityPlayerMP player) {
        PlayerCMapSelector sel = getBy(player.renderRadius);
        sel.removePlayer(this, player);
        //players.removeRef(player);
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
        getBy(player.renderRadius).updateMovingPlayer(this, player);
    }


    public boolean isPlayerWatchingChunk(EntityPlayerMP player, int chunkX, int chunkZ) {
        PlayerChunkMapEntry entry = getEntry(chunkX, chunkZ);
        return entry != null && entry.containsRef(player) && entry.isSentToPlayers();
    }


    public void setPlayerViewRadius(int radius) {
        nextRadius = radByFormula((short) radius);
    }


    public static short radByFormula(int radius){
        return (short) radius;
        //return (radius>>1)+2;
    }


    public static short radByFormula(short radius){
        return radius;
        //return (radius>>1)+2;
    }


    public void setPlayerViewRadiusU(int radius) {
        radius = MathHelper.clamp(radius, 3, 32);

        if (radius != this.playerViewRadius) {
            int i = radius - this.playerViewRadius;

            List<EntityPlayer> players = world.curPlayerEntities;
            int size = players.size();

            for (int cursor = 0; cursor < size; cursor++) {
                EntityPlayerMP player = (EntityPlayerMP) players.get(cursor);
                int j = (int)player.posX >> 4;
                int k = (int)player.posZ >> 4;

                if (i > 0) {
                    for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                        for (int k1 = k - radius; k1 <= k + radius; ++k1) {
                            PlayerChunkMapEntry entry = this.getOrCreateEntry(j1, k1);
                            if (!entry.contains(player)) {
                                entry.addPlayer(this, player);
                            }
                        }
                    }
                } else {
                    for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                        for (int i1 = k - this.playerViewRadius; i1 <= k + this.playerViewRadius; ++i1) {
                            if (!this.overlaps(l, i1, j, k, radius)) {
                                PlayerChunkMapEntry entry = getEntry(l, i1);
                                if(entry != null) entry.removePlayer(this, player);
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


    public int getMaxPlayerViewRadius(){
        return playerViewRadius;
    }
}