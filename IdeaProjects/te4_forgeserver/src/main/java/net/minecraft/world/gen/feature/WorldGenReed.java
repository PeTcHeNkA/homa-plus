package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenReed extends CWorldGenerator {
    public WorldGenReed() {}


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for(int i = 0; i < 20; ++i) {
            mut.setPosB(
                    pos.getX() + (rand.nextInt(4) - rand.nextInt(4)),
                    pos.getY(),
                    pos.getZ() + (rand.nextInt(4) - rand.nextInt(4))
            );

            if (selector.isAirBlock(mut)) {
                if (
                        selector.getBlockState(mut.getX()-1, mut.getY()-1, mut.getZ()).getMaterial() == Material.WATER ||
                        selector.getBlockState(mut.getX()+1, mut.getY()-1, mut.getZ()).getMaterial() == Material.WATER ||
                        selector.getBlockState(mut.getX(), mut.getY()-1, mut.getZ()-1).getMaterial() == Material.WATER ||
                        selector.getBlockState(mut.getX(), mut.getY()-1, mut.getZ()+1).getMaterial() == Material.WATER) {

                    int count = 2 + rand.nextInt(rand.nextInt(3) + 1);

                    for(int nY = 0; nY < count; ++nY) {
                        if (Blocks.REEDS.canPlaceBlockAtAsy(selector, mut)) {
                            int prev = mut.getY();
                            mut.setY(prev+nY);
                            selector.setBlockState(mut, Blocks.REEDS.getDefaultState(), 2);
                            mut.setY(prev);
                        }
                    }
                }
            }
        }

        return true;
    }

}
