package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenLakes extends WorldGenerator {
    private final IBlockState state;

    public WorldGenLakes(Block blockIn) {
        this.state = blockIn.getDefaultState();
    }


    public boolean generate(World worldIn, Random rand, BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(
                position.getX() -8, position.getY(), position.getZ()-8
        );
        while (mutPos.getY() > 5 && selector.isAirBlock(mutPos)) {
            mutPos.setY(mutPos.getY()-1);
        }

        if (position.getY() <= 4) {
            return false;
        } else {
            position = mutPos.down(4);
            boolean[] aboolean = new boolean[2048];
            int i = rand.nextInt(4) + 4;

            for (int j = 0; j < i; ++j) {
                double d0 = rand.nextDouble() * 6 + 3;
                double d1 = rand.nextDouble() * 4 + 2;
                double d2 = rand.nextDouble() * 6 + 3;
                double d3 = rand.nextDouble() * (16 - d0 - 2) + 1.0D + d0 / 2;
                double d4 = rand.nextDouble() * (8 - d1 - 4) + 2.0D + d1 / 2;
                double d5 = rand.nextDouble() * (16 - d2 - 2) + 1.0D + d2 / 2;

                for (int l = 1; l < 15; ++l) {
                    for (int i1 = 1; i1 < 15; ++i1) {
                        for (int j1 = 1; j1 < 7; ++j1) {
                            double d6 = ((double)l - d3) / (d0 / 2);
                            double d7 = ((double)j1 - d4) / (d1 / 2);
                            double d8 = ((double)i1 - d5) / (d2 / 2);
                            double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                            if (d9 < 1.0D) {
                                aboolean[(l * 16 + i1) * 8 + j1] = true;
                            }
                        }
                    }
                }
            }
            IBlockState tempState;
            for (int k1 = 0; k1 < 16; ++k1)
                for (int l2 = 0; l2 < 16; ++l2) {
                    for (int k = 0; k < 8; ++k) {
                        boolean flag = !aboolean[(k1 * 16 + l2) * 8 + k] && (k1 < 15 && aboolean[((k1 + 1) * 16 + l2) * 8 + k] || k1 > 0 && aboolean[((k1 - 1) * 16 + l2) * 8 + k] || l2 < 15 && aboolean[(k1 * 16 + l2 + 1) * 8 + k] || l2 > 0 && aboolean[(k1 * 16 + (l2 - 1)) * 8 + k] || k < 7 && aboolean[(k1 * 16 + l2) * 8 + k + 1] || k > 0 && aboolean[(k1 * 16 + l2) * 8 + (k - 1)]);

                        if (flag) {
                            Material material = (tempState = selector.getState(position.add(k1, k, l2)))
                                    .getMaterial();

                            if (k >= 4 && material.isLiquid())
                                return false;
                            if (k < 4 && !material.isSolid() && tempState.getBlock() != state.getBlock())
                                return false;
                        }
                    }
                }

            for (int l1 = 0; l1 < 16; ++l1)
                for (int i3 = 0; i3 < 16; ++i3)
                    for (int i4 = 0; i4 < 8; ++i4) {
                        if (aboolean[(l1 * 16 + i3) * 8 + i4])
                            selector.setBlockState(position.add(l1, i4, i3), i4 >= 4 ?
                                    BlockIDIndexes.AIR_STATE : state);
                    }

            for (int i2 = 0; i2 < 16; ++i2)
                for (int j3 = 0; j3 < 16; ++j3)
                    for (int j4 = 4; j4 < 8; ++j4)
                        if (aboolean[(i2 * 16 + j3) * 8 + j4]) {
                            BlockPos blockpos = position.add(i2, j4 - 1, j3);

                            if (selector.getState(blockpos).getBlock() == Blocks.DIRT
                                    && selector.current().getLightFor(EnumSkyBlock.SKY, position.add(i2, j4, j3)) > 0) {

                                Biome biome = selector.getBiome(blockpos);

                                if (biome.topBlock.getBlock() == Blocks.MYCELIUM)
                                    selector.setCurState(blockpos, Blocks.MYCELIUM.getDefaultState());
                                else
                                    selector.setCurState(blockpos, Blocks.GRASS.getDefaultState());
                            }
                        }

            BlockPos temp;

            if (this.state.getMaterial() == Material.LAVA)
                for (int j2 = 0; j2 < 16; ++j2)
                    for (int k3 = 0; k3 < 16; ++k3)
                        for (int k4 = 0; k4 < 8; ++k4) {
                            boolean flag1 = !aboolean[(j2 * 16 + k3) * 8 + k4] && (j2 < 15 && aboolean[((j2 + 1) * 16 + k3) * 8 + k4] || j2 > 0 && aboolean[((j2 - 1) * 16 + k3) * 8 + k4] || k3 < 15 && aboolean[(j2 * 16 + k3 + 1) * 8 + k4] || k3 > 0 && aboolean[(j2 * 16 + (k3 - 1)) * 8 + k4] || k4 < 7 && aboolean[(j2 * 16 + k3) * 8 + k4 + 1] || k4 > 0 && aboolean[(j2 * 16 + k3) * 8 + (k4 - 1)]);
                            if (flag1 && (k4 < 4 || rand.nextInt(2) != 0)
                                    && selector.getState(temp = position.add(j2, k4, k3)).getMaterial().isSolid())
                                selector.setCurState(temp, BlockIDIndexes.STONE_STATE);
                        }

            if (this.state.getMaterial() == Material.WATER)
                for (int k2 = 0; k2 < 16; ++k2)
                    for (int l3 = 0; l3 < 16; ++l3)
                        if (worldIn.canBlockFreezeWater(temp = position.add(k2, 4, l3))) {
                            int flag = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 2| 16 : 2;
                            selector.setBlockState(temp, Blocks.ICE.getDefaultState(), flag);
                        }
            return true;
        }
    }
}