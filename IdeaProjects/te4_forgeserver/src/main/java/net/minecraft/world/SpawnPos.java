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


    void setRandomChunkPos(int x, int z) {
        chunk = world.getChunk(x, z);
        int rand = world.rand.nextInt();
        int nX = (x << 4) + (rand & 15);
        int nZ = (z << 4) + ((rand >> 4) & 15);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(nX, 0, nZ)) + 1, 16);
        int nY = world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        setPos(nX, nY, nZ);
    }
}
