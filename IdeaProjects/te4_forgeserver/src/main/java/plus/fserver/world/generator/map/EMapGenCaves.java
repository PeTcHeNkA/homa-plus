package plus.fserver.world.generator.map;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.GenSession;
import java.util.Random;


public class EMapGenCaves extends EMapGenBase {
    protected static final IBlockState BLK_LAVA = Blocks.LAVA.getDefaultState();
    protected static final IBlockState BLK_AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState BLK_SANDSTONE = Blocks.SANDSTONE.getDefaultState();
    protected static final IBlockState BLK_RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();

    protected void addRoom(GenSession session, long seed, int num2, int num3, ChunkPrimer primer,
                           double d1, double d2, double d3) {
        this.addTunnel(session, seed, num2, num3, primer, d1, d2, d3, 1.0F + session.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }


    protected void addTunnel(GenSession session, long seed, int num1, int num2, ChunkPrimer primer,
                             double di1, double di2, double di3, float fi1, float fi2, float fi3,
                             int num3, int num4, double di4) {
        double d0 = num1 * 16 + 8;
        double d1 = num2 * 16 + 8;
        float f = 0.0F;
        float f1 = 0.0F;
        Random random = new Random(seed);

        if (num4 <= 0) {
            int i = this.range * 16 - 16;
            num4 = i - random.nextInt(i / 4);
        }

        boolean flag2 = false;

        if (num3 == -1) {
            num3 = num4 / 2;
            flag2 = true;
        }
        int j = random.nextInt(num4 / 2) + num4 / 4;

        for (boolean flag = random.nextInt(6) == 0; num3 < num4; ++num3) {
            double d2 = 1.5D + (double)(MathHelper.sin((float)num3 * (float)Math.PI / (float)num4) * fi1);
            double d3 = d2 * di4;
            float f2 = MathHelper.cos(fi3);
            float f3 = MathHelper.sin(fi3);
            di1 += MathHelper.cos(fi2) * f2;
            di2 += f3;
            di3 += MathHelper.sin(fi2) * f2;

            if (flag) {
                fi3 = fi3 * 0.92F;
            } else {
                fi3 = fi3 * 0.7F;
            }

            fi3 = fi3 + f1 * 0.1F;
            fi2 += f * 0.1F;
            f1 = f1 * 0.9F;
            f = f * 0.75F;
            f1 = f1 + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f = f + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag2 && num3 == j && fi1 > 1.0F && num4 > 0) {
                this.addTunnel(session, random.nextLong(), num1, num2, primer, di1, di2, di3, random.nextFloat() * 0.5F + 0.5F, fi2 - ((float)Math.PI / 2F), fi3 / 3.0F, num3, num4, 1.0D);
                this.addTunnel(session, random.nextLong(), num1, num2, primer, di1, di2, di3, random.nextFloat() * 0.5F + 0.5F, fi2 + ((float)Math.PI / 2F), fi3 / 3.0F, num3, num4, 1.0D);
                return;
            }

            if (flag2 || random.nextInt(4) != 0) {
                double d4 = di1 - d0;
                double d5 = di3 - d1;
                double d6 = num4 - num3;
                double d7 = fi1 + 2.0F + 16.0F;

                if (d4 * d4 + d5 * d5 - d6 * d6 > d7 * d7) {
                    return;
                }

                if (di1 >= d0 - 16.0D - d2 * 2.0D && di3 >= d1 - 16.0D - d2 * 2.0D && di1 <= d0 + 16.0D + d2 * 2.0D && di3 <= d1 + 16.0D + d2 * 2.0D) {
                    int k2 = MathHelper.floor(di1 - d2) - num1 * 16 - 1;
                    int k = MathHelper.floor(di1 + d2) - num1 * 16 + 1;
                    int l2 = MathHelper.floor(di2 - d3) - 1;
                    int l = MathHelper.floor(di2 + d3) + 1;
                    int i3 = MathHelper.floor(di3 - d2) - num2 * 16 - 1;
                    int i1 = MathHelper.floor(di3 + d2) - num2 * 16 + 1;

                    if (k2 < 0) {
                        k2 = 0;
                    }

                    if (k > 16) {
                        k = 16;
                    }

                    if (l2 < 1) {
                        l2 = 1;
                    }

                    if (l > 248) {
                        l = 248;
                    }

                    if (i3 < 0) {
                        i3 = 0;
                    }

                    if (i1 > 16) {
                        i1 = 16;
                    }

                    boolean flag3 = false;

                    for (int j1 = k2; !flag3 && j1 < k; ++j1) {
                        for (int k1 = i3; !flag3 && k1 < i1; ++k1) {
                            for (int l1 = l + 1; !flag3 && l1 >= l2 - 1; --l1) {
                                if (l1 < 256) {
                                    if (isOceanBlock(primer, j1, l1, k1, num1, num2)) {
                                        flag3 = true;
                                    }
                                    if (l1 != l2 - 1 && j1 != k2 && j1 != k - 1 && k1 != i3 && k1 != i1 - 1) {
                                        l1 = l2;
                                    }
                                }
                            }
                        }
                    }

                    if (!flag3) {

                        for (int j3 = k2; j3 < k; ++j3) {
                            double d10 = ((double)(j3 + num1 * 16) + 0.5D - di1) / d2;

                            for (int i2 = i3; i2 < i1; ++i2) {
                                double d8 = ((double)(i2 + num2 * 16) + 0.5D - di3) / d2;
                                boolean flag1 = false;

                                if (d10 * d10 + d8 * d8 < 1.0D) {
                                    for (int j2 = l; j2 > l2; --j2) {
                                        double d9 = ((double)(j2 - 1) + 0.5D - di2) / d3;

                                        if (d9 > -0.7D && d10 * d10 + d9 * d9 + d8 * d8 < 1.0D) {
                                            IBlockState iblockstate1 = primer.getBlockState(j3, j2, i2);
                                            IBlockState iblockstate2 = MoreObjects.firstNonNull(primer.getBlockState(j3, j2 + 1, i2), BLK_AIR);

                                            if (isTopBlock(primer, session, j3, j2, i2))
                                            {
                                                flag1 = true;
                                            }

                                            digBlock(session, primer, j3, j2, i2, flag1, iblockstate1, iblockstate2);
                                        }
                                    }
                                }
                            }
                        }
                        if (flag2) {
                            break;
                        }
                    }
                }
            }
        }
    }


    protected boolean canReplaceBlock(IBlockState p_175793_1_, IBlockState p_175793_2_)
    {
        if (p_175793_1_.getBlock() == Blocks.STONE)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.DIRT)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.GRASS)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.HARDENED_CLAY)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.STAINED_HARDENED_CLAY)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.SANDSTONE)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.RED_SANDSTONE)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.MYCELIUM)
        {
            return true;
        }
        else if (p_175793_1_.getBlock() == Blocks.SNOW_LAYER)
        {
            return true;
        }
        else
        {
            return (p_175793_1_.getBlock() == Blocks.SAND || p_175793_1_.getBlock() == Blocks.GRAVEL) && p_175793_2_.getMaterial() != Material.WATER;
        }
    }


    @Override
    protected void recursiveGenerate(World world, GenSession session, int num1, int num2, int num3, int num4, ChunkPrimer primer) {
        int i = session.rand.nextInt(session.rand.nextInt(session.rand.nextInt(15) + 1) + 1);

        if (session.rand.nextInt(7) != 0) {
            i = 0;
        }

        for (int j = 0; j < i; ++j) {
            double d0 = num1 * 16 + session.rand.nextInt(16);
            double d1 = session.rand.nextInt(session.rand.nextInt(120) + 8);
            double d2 = num2 * 16 + session.rand.nextInt(16);
            int k = 1;

            if (session.rand.nextInt(4) == 0) {
                this.addRoom(session, session.rand.nextLong(), num3, num4, primer, d0, d1, d2);
                k += session.rand.nextInt(4);
            }

            for (int l = 0; l < k; ++l) {
                float f = session.rand.nextFloat() * ((float)Math.PI * 2F);
                float f1 = (session.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = session.rand.nextFloat() * 2.0F + session.rand.nextFloat();

                if (session.rand.nextInt(10) == 0) {
                    f2 *= session.rand.nextFloat() * session.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.addTunnel(session, session.rand.nextLong(), num3, num4, primer, d0, d1, d2, f2, f, f1, 0, 0, 1.0D);
            }
        }
    }


    protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
        Block block = data.getBlockState(x, y, z).getBlock();
        return block== Blocks.FLOWING_WATER || block == Blocks.WATER;
    }


    private boolean isExceptionBiome(Biome biome) {
        if (biome == Biomes.BEACH) return true;
        return biome == Biomes.DESERT;
    }


    private boolean isTopBlock(ChunkPrimer data, GenSession session, int x, int y, int z) {
        Biome biome = session.biomesForGeneration[x + z * 16];
        IBlockState state = data.getBlockState(x, y, z);
        return (isExceptionBiome(biome) ? state.getBlock() == Blocks.GRASS : state == biome.topBlock);
    }


    protected void digBlock(GenSession session, ChunkPrimer data, int x, int y, int z, boolean foundTop, IBlockState state, IBlockState up) {
        Biome biome = session.biomesForGeneration[x + z * 16];

        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;

        if (this.canReplaceBlock(state, up) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock()) {
            if (y - 1 < 10) {
                data.setBlockState(x, y, z, BLK_LAVA);
            } else {
                data.setBlockState(x, y, z, BLK_AIR);
                if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                    data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
                }
            }
        }
    }
}
