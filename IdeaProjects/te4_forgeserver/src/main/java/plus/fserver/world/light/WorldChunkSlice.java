package plus.fserver.world.light;

import net.minecraft.world.chunk.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.ChunkProviderServer;


public class WorldChunkSlice {
    private static final int DIAMETER = 5;
    private final Chunk[] chunks;
    private int x;
    private int z;

    public WorldChunkSlice(){
        this.chunks = new Chunk[25];
    }

    public WorldChunkSlice(final ChunkProviderServer cps, final int x, final int z) {
        Chunk[] chunks = this.chunks = new Chunk[25];

        for (int xDiff = -2; xDiff <= 2; ++xDiff) {
            for (int zDiff = -2; zDiff <= 2; ++zDiff) {
                chunks[(xDiff + 2) * 5 + (zDiff + 2)] = cps.getLoadedChunk(x + xDiff, z + zDiff);
            }
        }
        this.x = x - 2;
        this.z = z - 2;
    }


    public void reset(){
        Chunk[] chunks = this.chunks;
        for (int i = 0; i < 25; i++)chunks[i] = null;
    }


    public void tryLoad5x5Full(final ChunkProviderServer cps, final int x, final int z){
        Chunk[] chunks = this.chunks;

        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                chunks[(dx + 2) * 5 + (dz + 2)] = cps.getLoadedChunk(x + dx, z + dz);
            }
        }
        this.x = x - 2;
        this.z = z - 2;
    }


    public void tryLoad5x5No3x3(final ChunkProviderServer cps, int x, int z){
        Chunk[] chunks = this.chunks;

        int dx = -2, dz = -2;
        for (; dz <= 2; ++dz) {
            chunks[(dz + 2)] = cps.getLoadedChunk(x + dx, z + dz);
        }

        dx = 2;
        for (dz = -2; dz <= 2; ++dz) {
            chunks[(dx + 2) * 5 + (dz + 2)] = cps.getLoadedChunk(x + dx, z + dz);
        }

        dx = -1; dz = -2;

        for (; dx <= 1; ++dx) {
            chunks[(dx + 2) * 5] = cps.getLoadedChunk(x + dx, z + dz);
        }

        dx = -1; dz = 2;

        for (; dx <= 1; ++dx) {
            chunks[(dx + 2) * 5 + (dz + 2)] = cps.getLoadedChunk(x + dx, z + dz);
        }
    }


    public void tryLoad3x3(final ChunkProviderServer cps, int x, int z){
        Chunk[] chunks = this.chunks;

        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                chunks[(dx + 2) * 5 + (dz + 2)] = cps.getLoadedChunk(x + dx, z + dz);
            }
        }
        this.x = x - 2;
        this.z = z - 2;
    }


    /**                               BY Z             BY X
     * 00000 | 0  1   2  3  4 |   -2 -1 0 +1 2   |  -2 -2 -2 -2 -2
     * 0###0 | 5  6   7  8  9 |   -2 -1 0 +1 2   |  -1 -1 -1 -1 -1
     * 0###0 | 10 11 12 13 14 |   -2 -1 0 +1 2   |   0  0  0  0  0
     * 0###0 | 15 16 17 18 19 |   -2 -1 0 +1 2   |  +1 +1 +1 +1 +1
     * 00000 | 20 21 22 23 24 |   -2 -1 0 +1 2   |  +2 +2 +2 +2 +2
     */

    public boolean isLoaded3x3(){
        Chunk[] chunks = this.chunks;
        return !(chunks[6]  == null ||
                 chunks[7]  == null ||
                 chunks[8]  == null ||
                 chunks[11] == null ||
                 chunks[12] == null ||
                 chunks[13] == null ||
                 chunks[16] == null ||
                 chunks[17] == null ||
                 chunks[18] == null );
    }


    public WorldChunkSlice(final World world, final int x, final int z) {
        Chunk[] chunks = this.chunks = new Chunk[25];
        final int radius = 2;
        for (int xDiff = -radius; xDiff <= radius; ++xDiff) {
            for (int zDiff = -radius; zDiff <= radius; ++zDiff) {
                chunks[(xDiff + radius) * 5 + (zDiff + radius)] = world.getChunkProvider().getLoadedChunk(x + xDiff, z + zDiff);
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