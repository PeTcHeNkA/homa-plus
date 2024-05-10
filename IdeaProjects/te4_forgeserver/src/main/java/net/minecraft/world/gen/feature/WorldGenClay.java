package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenClay extends WorldGenerator {
    private final IBlockState state = Blocks.CLAY.getDefaultState();
    private final int numberOfBlocks;

    public WorldGenClay(int num) {
        this.numberOfBlocks = num;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        Chunk chunk = worldIn.getChunk(position);
        if (chunk.getBlockState(position).getMaterial() != Material.WATER) {
            return false;
        } else {
            ChunkSelector selector = new ChunkSelector(chunk);
            Block block;
            int i = rand.nextInt(this.numberOfBlocks - 2) + 2;

            for (int k = position.getX() - i, i1, j1; k <= position.getX() + i; ++k) {
                for (int l = position.getZ() - i; l <= position.getZ() + i; ++l) {
                    i1 = k - position.getX();
                    j1 = l - position.getZ();

                    if (i1 * i1 + j1 * j1 <= i * i) {
                        for (int k1 = position.getY() - 1; k1 <= position.getY() + 1; ++k1) {
                            block = selector.getBlockState(k, k1, l).getBlock();

                            if (block == Blocks.DIRT || block == Blocks.CLAY) {
                                selector.setCurStateFast(k, k1, l, state);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
}