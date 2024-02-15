package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.utls.ChunkSelector;


public class WorldGenBlockBlob extends WorldGenerator {
    private final IBlockState state;
    private final int startRadius;

    public WorldGenBlockBlob(Block blockIn, int startRadiusIn) {
        super(false);
        this.state = blockIn.getDefaultState();
        this.startRadius = startRadiusIn;
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        while (true) {
            label50:{
                if (position.getY() > 3) {
                    if (selector.isAirBlock(position.down())) {
                        break label50;
                    }

                    Block block = selector.getState(position.down()).getBlock();

                    if (block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.STONE) {
                        break label50;
                    }
                }

                if (position.getY() <= 3) {
                    return false;
                }

                int i1 = this.startRadius;

                for (int i = 0; i1 >= 0 && i < 3; ++i) {
                    int j = i1 + rand.nextInt(2);
                    int k = i1 + rand.nextInt(2);
                    int l = i1 + rand.nextInt(2);
                    float f = (float)(j + k + l) * 0.333F + 0.5F;

                    for (BlockPos blockpos : BlockPos.getAllInBox(position.add(-j, -k, -l), position.add(j, k, l))) {
                        if (blockpos.distanceSq(position) <= (double)(f * f)) {
                            selector.setBlockState(blockpos, state);
                        }
                    }
                    position = position.add(-(i1 + 1) + rand.nextInt(2 + i1 * 2), -rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
                }
                return true;
            }
            position = position.down();
        }
    }
}