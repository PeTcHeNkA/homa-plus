package plus.fserver.world.light;

import net.minecraft.world.chunk.*;
import net.minecraft.world.*;


public class WorldChunkSlice {
    private static final int DIAMETER = 5;
    private final Chunk[] chunks;
    private final int x;
    private final int z;
    
    public WorldChunkSlice(final World world, final int x, final int z) {
        this.chunks = new Chunk[25];
        final int radius = 2;
        for (int xDiff = -radius; xDiff <= radius; ++xDiff) {
            for (int zDiff = -radius; zDiff <= radius; ++zDiff) {
                this.chunks[(xDiff + radius) * 5 + (zDiff + radius)] = world.getChunkProvider().getLoadedChunk(x + xDiff, z + zDiff);
            }
        }
        this.x = x - radius;
        this.z = z - radius;
    }
    
    public Chunk getChunk(final int x, final int z) {
        return this.chunks[x * 5 + z];
    }


    public Chunk getChunkFromWorldCoords(final int x, final int z) {
        return this.getChunk((x >> 4) - this.x, (z >> 4) - this.z);
    }


    public boolean isLoaded(final int x, final int z, final int radius) {
        return this.isLoaded(x - radius, z - radius, x + radius, z + radius);
    }


    public boolean isLoaded(int xStart, int zStart, int xEnd, int zEnd) {
        xStart = (xStart >> 4) - this.x;
        zStart = (zStart >> 4) - this.z;
        xEnd = (xEnd >> 4) - this.x;
        zEnd = (zEnd >> 4) - this.z;
        for (int i = xStart; i <= xEnd; ++i) {
            for (int j = zStart; j <= zEnd; ++j) {
                if (this.getChunk(i, j) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}