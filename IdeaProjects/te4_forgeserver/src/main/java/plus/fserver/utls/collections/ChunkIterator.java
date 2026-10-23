package plus.fserver.utls.collections;

import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public final class ChunkIterator implements Iterator<Chunk>, Iterable<Chunk> {
    private final Object[] data;
    private final int size;
    private final int radiusSq;
    private Chunk next;
    private int cursor;

    public ChunkIterator(ArrayListE6<PlayerChunkMapEntry> entries, int radius) {
        this.data = entries.data;
        this.size = entries.size;
        this.radiusSq = radius*radius;
        calcNext();
    }


    private void calcNext(){
        Object[] data = this.data;
        while (cursor < this.size) {
            PlayerChunkMapEntry entry = (PlayerChunkMapEntry) data[cursor++];
            Chunk chunk = entry.getChunk();
            if (chunk == null) continue;
            if (chunk.isPersistent()){
                next = chunk;
                return;
            }
            if (!chunk.isLightPopulated() && chunk.isTerrainPopulated() || !chunk.wasTicked()) {
                next = chunk;
                return;
            }
            if (!entry.hasCommonPlayerInRangeSq(radiusSq)) continue;
            next = chunk;
            return;
        }
        next = null;
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
        calcNext();
        return result;
    }
}