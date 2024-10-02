package plus.fserver.utls.collections;

import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;
import java.util.function.Consumer;


public class ChunkIteratorPro implements Iterator<Chunk>, Iterable<Chunk> {
    private final Chunk[] chunks;
    private final int radiusSq;
    private Chunk next;
    private int cursor;

    public ChunkIteratorPro(Chunk[] chunks, int radius) {
        this.chunks = chunks;
        this.radiusSq = radius*radius;
        calcNext();
    }


    private void calcNext(){
        Chunk[] chunks = this.chunks;
        int size = chunks.length;
        int cursor = this.cursor;
        while (cursor < size) {
            Chunk chunk = chunks[cursor++];
            PlayerChunkMapEntry entry;
            if (chunk == null || (entry = chunk.chunkMapEntry) == null) continue;
            if (chunk.isPersistent()){
                next = chunk;
                this.cursor = cursor;
                return;
            }
            if (!chunk.isLightPopulated() && chunk.isTerrainPopulated() || !chunk.wasTicked()) {
                next = chunk;
                this.cursor = cursor;
                return;
            }
            if (!entry.hasCommonPlayerInRangeSq(radiusSq)) continue;
            next = chunk;
            this.cursor = cursor;
            return;
        }
        next = null;
    }


    public void forChunks(Consumer<Chunk> task) {
        Chunk[] chunks = this.chunks;
        for (Chunk chunk : chunks) {
            if (chunk != null) task.accept(chunk);
        }
    }


    public void forEntries(Consumer<PlayerChunkMapEntry> task) {
        Chunk[] chunks = this.chunks;
        for (Chunk chunk : chunks) {
            PlayerChunkMapEntry entry;
            if (chunk != null && (entry = chunk.chunkMapEntry) != null) task.accept(entry);
        }
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