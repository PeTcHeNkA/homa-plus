package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenCactus extends WorldGenerator {
    public WorldGenCactus() {}


    @Override
    public boolean generate(World worldIn, Random random, BlockPos pos) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for(int cursor = 0; cursor < 10; ++cursor) {
            FastRandom.INST.setRandom8x8x4(mut, pos);
            if (mut.getY()<255 && mut.getY()>0 && selector.isAirBlock(mut)) {
                int height = 1 + random.nextInt(random.nextInt(3) + 1);
                int startY = mut.getY();

                for(int yPos = 0; yPos < height; ++yPos) {
                    if (Blocks.CACTUS.canBlockStay(worldIn, mut)) {
                        mut.setY(startY+yPos);
                        selector.setBlockState(mut, Blocks.CACTUS.getDefaultState(), 2);
                    }
                }
            }
        }
        return true;
    }
}