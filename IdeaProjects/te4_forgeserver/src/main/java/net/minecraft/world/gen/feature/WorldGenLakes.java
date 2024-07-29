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
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenLakes extends WorldGenerator {
    private final IBlockState state;

    public WorldGenLakes(Block blockIn) {
        this.state = blockIn.getDefaultState();
    }


    @Keep
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
                double d3 = rand.nextDouble() * (16 - d0 - 2) + 1.0D + d0 * 0.5;
                double d4 = rand.nextDouble() * ( 8 - d1 - 4) + 2.0D + d1 * 0.5;
                double d5 = rand.nextDouble() * (16 - d2 - 2) + 1.0D + d2 * 0.5;

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
            for (int dx = 0; dx < 16; ++dx)
                for (int dz = 0; dz < 16; ++dz) {
                    for (int dy = 0; dy < 8; ++dy) {
                        boolean flag = !aboolean[(dx * 16 + dz) * 8 + dy] && (dx < 15 && aboolean[((dx + 1) * 16 + dz) * 8 + dy] || dx > 0 && aboolean[((dx - 1) * 16 + dz) * 8 + dy] || dz < 15 && aboolean[(dx * 16 + dz + 1) * 8 + dy] || dz > 0 && aboolean[(dx * 16 + (dz - 1)) * 8 + dy] || dy < 7 && aboolean[(dx * 16 + dz) * 8 + dy + 1] || dy > 0 && aboolean[(dx * 16 + dz) * 8 + (dy - 1)]);

                        if (flag) {
                            mutPos.setPosB(
                                    position.getX() + dx,
                                    position.getY() + dy,
                                    position.getZ() + dz
                            );
                            Material material = (tempState = selector.getState(mutPos))
                                    .getMaterial();

                            if (dy >= 4 && material.isLiquid())
                                return false;
                            if (dy < 4 && !material.isSolid() && tempState.getBlock() != state.getBlock())
                                return false;
                        }
                    }
                }

            for (int dx = 0; dx < 16; ++dx)
                for (int dz = 0; dz < 16; ++dz)
                    for (int dy = 0; dy < 8; ++dy) {
                        if (aboolean[(dx * 16 + dz) * 8 + dy]) {
                            mutPos.setPosB(
                                    position.getX() + dx,
                                    position.getY() + dy,
                                    position.getZ() + dz
                            );
                            selector.setBlockState(mutPos, dy >= 4 ?
                                    BlockIDIndexes.AIR_STATE : state);
                        }
                    }

            for (int dx = 0; dx < 16; ++dx)
                for (int dz = 0; dz < 16; ++dz)
                    for (int dy = 4; dy < 8; ++dy)
                        if (aboolean[(dx * 16 + dz) * 8 + dy]) {
                            mutPos.setPosB(
                                    position.getX() + dx,
                                    position.getY() + dy - 1,
                                    position.getZ() + dz
                            );

                            if (selector.getState(mutPos).getBlock() == Blocks.DIRT
                                    && selector.current().getLightFor(EnumSkyBlock.SKY, position.add(dx, dy, dz)) > 0) {

                                Biome biome = selector.getBiome(mutPos);

                                if (biome.topBlock.getBlock() == Blocks.MYCELIUM)
                                    selector.setCurState(mutPos, Blocks.MYCELIUM.getDefaultState());
                                else
                                    selector.setCurState(mutPos, Blocks.GRASS.getDefaultState());
                            }
                        }

            //BlockPos temp;

            if (this.state.getMaterial() == Material.LAVA)
                for (int dx = 0; dx < 16; ++dx)
                    for (int dz = 0; dz < 16; ++dz)
                        for (int dy = 0; dy < 8; ++dy) {
                            boolean flag1 = !aboolean[(dx * 16 + dz) * 8 + dy] && (dx < 15 && aboolean[((dx + 1) * 16 + dz) * 8 + dy] || dx > 0 && aboolean[((dx - 1) * 16 + dz) * 8 + dy] || dz < 15 && aboolean[(dx * 16 + dz + 1) * 8 + dy] || dz > 0 && aboolean[(dx * 16 + (dz - 1)) * 8 + dy] || dy < 7 && aboolean[(dx * 16 + dz) * 8 + dy + 1] || dy > 0 && aboolean[(dx * 16 + dz) * 8 + (dy - 1)]);
                            if (flag1 && (dy < 4 || rand.nextInt(2) != 0)) {
                                mutPos.setPosB(
                                        position.getX() + dx,
                                        position.getY() + dy,
                                        position.getZ() + dz
                                );

                                if(selector.getState(mutPos).getMaterial().isSolid())
                                    selector.setCurState(mutPos, BlockIDIndexes.STONE_STATE);
                            }
                        }

            if (this.state.getMaterial() == Material.WATER)
                for (int dx = 0; dx < 16; ++dx)
                    for (int dz = 0; dz < 16; ++dz) {
                        mutPos.setPosB(
                                position.getX() + dx,
                                position.getY() + 4,
                                position.getZ() + dz
                        );

                        if (worldIn.canBlockFreeze(mutPos, false)) {
                            int flag = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 2 | 16 : 2;
                            selector.setBlockState(mutPos, Blocks.ICE.getDefaultState(), flag);
                        }
                    }
            return true;
        }
    }
}