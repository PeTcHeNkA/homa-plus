package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.ElcaConfig;
import plus.fserver.server.chunk.PlayerCMapSelector;
import plus.tson.utl.Te4HashSet;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;


public class PlayerChunkMap extends Long2ObjectOpenHashMap<PlayerChunkMapEntry> {
    protected static final Int2ObjectOpenHashMap<PlayerCMapSelector> SELECTORS = new Int2ObjectOpenHashMap<>(7);
    public static final Predicate<EntityPlayerMP> NOT_SPECTATOR = player -> player != null && !player.isSpectator();
    public static final Predicate<EntityPlayerMP> CAN_GENERATE_CHUNKS = player -> player != null && (!player.isSpectator() || player.getServerWorld().getGameRules().getBoolean("spectatorsGenerateChunks"));

    private final WorldServer world;
    private final ChunkProviderServer provider;
    private Te4HashSet<PlayerChunkMapEntry> dirtyEntries = new Te4HashSet<>(7);
    private final List<PlayerChunkMapEntry> pendingSendToPlayers = Lists.newLinkedList();
    private final List<PlayerChunkMapEntry> entriesWithoutChunks = Lists.newLinkedList();
    //private final ArrayRemSwapListE15<PlayerChunkMapEntry> entries = new ArrayRemSwapListE15<>();

    private int playerViewRadius;
    private long previousTotalWorldTime;
    private boolean sortMissingChunks = true;
    private boolean sortSendToPlayers = true;
    private int nextRadius = -1;

    public PlayerChunkMap(WorldServer serverWorld) {
        super(64, 0.85F);
        this.world = serverWorld;
        this.provider = serverWorld.getChunkProvider();
        this.setPlayerViewRadiusU(serverWorld.getMinecraftServer().getPlayerList().getViewDistance());
    }


    public WorldServer getWorldServer() {
        return this.world;
    }


    public ChunkProviderServer getProvider(){
        return provider;
    }


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
//        return entries;
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

        List<PlayerChunkMapEntry> entriesWithoutChunks = this.entriesWithoutChunks;
        List<PlayerChunkMapEntry> pendingSendToPlayers = this.pendingSendToPlayers;

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
            int k = ElcaConfig.PCM_MAX_LOAD_PER_TICK;
            Iterator<PlayerChunkMapEntry> iterator = entriesWithoutChunks.iterator();

            while (iterator.hasNext()) {
                final PlayerChunkMapEntry entry;
                if ((entry = iterator.next()).getChunk() == null) {

                    if (entry.providePlayerChunk(this, entry.hasPlayerMatching(CAN_GENERATE_CHUNKS))) {
                        iterator.remove();
                        super.remove(ChunkPos.asLong(entry.posX, entry.posZ));

                        if (entry.sendToPlayers(this)) {
                            pendingSendToPlayers.remove(entry);
                        }
                        --k;
                        if (k < 0 || System.nanoTime() > l) {
                            break;
                        }
                    }
                } else {
                    iterator.remove();
                    super.remove(ChunkPos.asLong(entry.posX, entry.posZ));
                }
            }
            super.trim(Math.min(64, super.size()));
        }

        if (!pendingSendToPlayers.isEmpty()) {
            int count = ElcaConfig.PCM_MAX_SEND_PER_TICK;
            Iterator<PlayerChunkMapEntry> itr = pendingSendToPlayers.iterator();

            //баг - чанк не населяется из-за Н-ной ошибки -> он остается здесь навечно
            while (itr.hasNext()) {
                if (itr.next().sendToPlayers(this)) {
                    itr.remove();
                    if (--count < 0) break;
                }
            }
        }

        if (world.curPlayerEntities.isEmpty()) {
            provider.queueUnloadAll();
        }
    }


    @Nullable
    public PlayerChunkMapEntry getEntry(int x, int z) {
        Chunk chunk = provider.getLoadedChunk(x, z);
        if(chunk == null)return super.get(ChunkPos.asLong(x, z));
        return chunk.chunkMapEntry;
        //return get(x, z);
    }


    public PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ) {
        Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
        PlayerChunkMapEntry entry;

        if(chunk == null){
            entry = super.get(ChunkPos.asLong(chunkX, chunkZ));
        } else {
            entry = chunk.chunkMapEntry;
        }

        if (entry == null) {
            if(chunk == null){
                entry = new PlayerChunkMapEntry(this, chunkX, chunkZ);

                if (entry.getChunk() == null) {
                    put(ChunkPos.asLong(chunkX, chunkZ), entry);
                    this.entriesWithoutChunks.add(entry);
                }
            } else {
                entry = new PlayerChunkMapEntry(chunk);
            }

            if (!entry.sendToPlayers(this)) {
                this.pendingSendToPlayers.add(entry);
            }
        }
        return entry;
    }


    public void removeEntry(PlayerChunkMapEntry entry) {
        entry.updateChunkInhabitedTime(this);
        this.pendingSendToPlayers.remove(entry);
        if(this.entriesWithoutChunks.remove(entry)){
            remove(ChunkPos.asLong(entry.posX, entry.posZ));
        }
        Chunk chunk = entry.getChunk();

        if (chunk != null) {
            chunk.chunkMapEntry = null;
            WorldServer world = (WorldServer) chunk.getWorld();
            world.getChunkProvider().queueUnload(chunk);
        }
    }


    public void markBlockForUpdate(BlockPos pos) {
        int px, pz;
        PlayerChunkMapEntry entry = super.get(ChunkPos.asLong((px = pos.getX()) >> 4, (pz = pos.getZ()) >> 4));
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
        PlayerChunkMapEntry entry = super.get(ChunkPos.asLong(chunkX, chunkZ));
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
            int playerViewRadius = this.playerViewRadius;
            int i = radius - playerViewRadius;

            List<EntityPlayer> players = world.curPlayerEntities;
            int size = players.size();

            for (int cursor = 0; cursor < size; cursor++) {
                EntityPlayerMP player = (EntityPlayerMP) players.get(cursor);
                int j = (int)player.posX >> 4;
                int k = (int)player.posZ >> 4;

                if (i > 0) {
                    for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                        for (int k1 = k - radius; k1 <= k + radius; ++k1) {
                            this.getOrCreateEntry(j1, k1).addPlayer(this, player);
                        }
                    }
                } else {
                    for (int l = j - playerViewRadius; l <= j + playerViewRadius; ++l) {
                        for (int i1 = k - playerViewRadius; i1 <= k + playerViewRadius; ++i1) {
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