package plus.fserver.utls.collections;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public class ChunkIterator implements Iterator<Chunk>, Iterable<Chunk> {
    private final ArrayListE6<PlayerChunkMapEntry> entries;
    private final int radius;
    private Chunk next;
    private int cursor;

    public ChunkIterator(ArrayListE6<PlayerChunkMapEntry> entries, int radius) {
        this.entries = entries;
        this.radius = radius;
        next = calcNext();
    }


    private Chunk calcNext(){
        Object[] data = entries.data;
        while (cursor < entries.size) {
            PlayerChunkMapEntry entry = (PlayerChunkMapEntry) data[cursor++];
            Chunk chunk = entry.getChunk();
            if (chunk == null) continue;
            if (chunk.isPersistent())return chunk;
            if (!chunk.isLightPopulated() && chunk.isTerrainPopulated()) return chunk;
            if (!chunk.wasTicked()) return chunk;

            if (!entry.hasPlayerMatchingInRange(radius, PlayerChunkMap.NOT_SPECTATOR)) continue;
            return chunk;
        }
        return null;
    }


    @NotNull
    @Override
    public Iterator<Chunk> iterator() {
        return this;
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public Chunk next() {
        Chunk result = next;
        next = calcNext();
        return result;
    }
}