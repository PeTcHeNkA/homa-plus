package net.minecraft.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;

class SpawnPos extends BlockPos.MutableBlockPos {
    final World world;
    Chunk chunk;

    SpawnPos(World world) {
        this.world = world;
    }


    void setRandomChunkPos(Chunk chunk) {
        this.chunk = chunk;
        int rand = world.rand.nextInt();
        int nX = (chunk.x << 4) + (rand & 15);
        int nZ = (chunk.z << 4) + ((rand >> 4) & 15);
        int k = MathHelper.roundUp(chunk.getHeightValue(nX & 15, nZ & 15) + 1, 16);
        int nY = world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        editRoot(nX, nY, nZ);
    }


    void setRandomChunkPos(int x, int z) {
        chunk = world.getChunk(x, z);
        int rand = world.rand.nextInt();
        int nX = (x << 4) + (rand & 15);
        int nZ = (z << 4) + ((rand >> 4) & 15);
        int k = MathHelper.roundUp(chunk.getHeightValue(nX & 15, nZ & 15) + 1, 16);
        int nY = world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        editRoot(nX, nY, nZ);
    }
}
