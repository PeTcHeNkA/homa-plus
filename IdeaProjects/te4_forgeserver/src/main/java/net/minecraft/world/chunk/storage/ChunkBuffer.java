package net.minecraft.world.chunk.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public final class ChunkBuffer extends ByteArrayOutputStream {
    private final RegionFile parent;
    private final int chunkX;
    private final int chunkZ;

    public ChunkBuffer(RegionFile parent, int x, int z) {
        super(8096);
        this.parent = parent;
        this.chunkX = x;
        this.chunkZ = z;
    }


    public void close() throws IOException {
        parent.write(this.chunkX, this.chunkZ, this.buf, this.count);
    }
}