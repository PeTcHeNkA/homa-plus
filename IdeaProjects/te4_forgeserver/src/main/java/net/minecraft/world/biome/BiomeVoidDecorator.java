package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class BiomeVoidDecorator extends BiomeDecorator {
    public BiomeVoidDecorator() {}

    @Override
    public void decorate(World world, Random rand, Biome biome, BlockPos pos) {
        BlockPos point = world.getSpawnPoint();

        double range = point.distanceSq(
                pos.getX()+8,
                    pos.getY(),
                pos.getZ()+8
        );

        if (!(range > 1024.0)) {
            BlockPos pos1 = new BlockPos(point.getX() - 16, point.getY() - 1, point.getZ() - 16);
            BlockPos pos2 = new BlockPos(point.getX() + 16, point.getY() - 1, point.getZ() + 16);

            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(pos1);
            ChunkSelector selector = new ChunkSelector(world);

            for(int z = pos.getZ(); z < pos.getZ() + 16; ++z) {
                for(int x = pos.getX(); x < pos.getX() + 16; ++x) {
                    if (z >= pos1.getZ() && z <= pos2.getZ() && x >= pos1.getX() && x <= pos2.getX()) {
                        mut.setPos(x, mut.getY(), z);
                        if (point.getX() == x && point.getZ() == z) {
                            selector.setBlockState(mut, Blocks.COBBLESTONE.getDefaultState(), 2);
                        } else {
                            selector.setBlockState(mut, Blocks.STONE.getDefaultState(), 2);
                        }
                    }
                }
            }

        }
    }
}