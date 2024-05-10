package plus.fserver.world.generator.three;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.server.elca.ChunkSelector;
import java.util.Random;


public class WorldGenMegaJungle5 extends WorldGenMegaJungle4 {
    public WorldGenMegaJungle5(boolean notify, int baseHeightIn, int extraRandomHeightIn, IBlockState wood, IBlockState leaves, int croneWidth, CrownCreator crone, SaplingsGenerator generator) {
        super(notify, baseHeightIn, extraRandomHeightIn, wood, leaves, croneWidth, crone, generator);
    }


    @Override
    public boolean generate(@NotNull World worldIn, @NotNull Random rand, @NotNull BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        int i = this.getHeight(rand);
        if (!this.ensureGrowable(selector, worldIn, position, i)) {
            return false;
        } else {
            this.createCrown(selector, worldIn, position.up(i), croneWidth==-1?i:croneWidth);

            for (int j = position.getY() + i - 2 - rand.nextInt(4); j > position.getY() + i / 2; j -= 2 + rand.nextInt(4)) {
                float f = rand.nextFloat() * ((float)Math.PI * 2F);
                float sin = MathHelper.sin(f), cos = MathHelper.cos(f);

                int k = position.getX() + (int)(0.5F + cos * 4.0F);
                int l = position.getZ() + (int)(0.5F + sin * 4.0F);

                for (int i1 = 0; i1 < 5; ++i1) {
                    k = position.getX() + (int)(1.5F + cos * (float)i1);
                    l = position.getZ() + (int)(1.5F + sin * (float)i1);
                    this.setBlockAndNotifyAdequately(selector, new BlockPos(k, j - 3 + i1 / 2, l), this.woodMetadata);
                }

                int j2 = 1 + rand.nextInt(2);
                int j1 = j;

                for (int k1 = j - j2; k1 <= j1; ++k1) {
                    int l1 = k1 - j1;
                    this.growLeavesLayer(selector, new BlockPos(k, k1, l), 1 - l1);
                }
            }

            for (int i2 = -2; i2 < i; ++i2) {
                BlockPos blockpos = position.up(i2);

                if (this.isAirLeaves(selector, blockpos)) {
                    this.setBlockAndNotifyAdequately(selector, blockpos, this.woodMetadata);
                    if (i2 > 0) {
                        this.placeVine(selector, rand, blockpos.west(), BlockVine.EAST);
                        this.placeVine(selector, rand, blockpos.north(), BlockVine.SOUTH);
                    }
                }

                if (i2 < i - 1) {
                    BlockPos blockpos1 = blockpos.east();

                    if (this.isAirLeaves(selector, blockpos1)) {
                        this.setBlockAndNotifyAdequately(selector, blockpos1, this.woodMetadata);

                        if (i2 > 0) {
                            this.placeVine(selector, rand, blockpos1.east(), BlockVine.WEST);
                            this.placeVine(selector, rand, blockpos1.north(), BlockVine.SOUTH);
                            this.placeVine(selector, rand, blockpos1.south(), BlockVine.NORTH);
                        }
                    }

                    BlockPos blockpos2 = blockpos.south();

                    if (this.isAirLeaves(selector, blockpos2)) {
                        this.setBlockAndNotifyAdequately(selector, blockpos2, this.woodMetadata);
                        if (i2 > 0) {
                            this.placeVine(selector, rand, blockpos2.south(), BlockVine.NORTH);
                        }
                    }

                    BlockPos blockpos3 = blockpos.north();

                    if (this.isAirLeaves(selector, blockpos3)) {
                        this.setBlockAndNotifyAdequately(selector, blockpos3, this.woodMetadata);

                        if (i2 > 0) {
                            this.placeVine(selector, rand, blockpos3.north(), BlockVine.SOUTH);
                        }
                    }

                    BlockPos blockpos4 = blockpos.west();

                    if (this.isAirLeaves(selector, blockpos4)) {
                        this.setBlockAndNotifyAdequately(selector, blockpos4, this.woodMetadata);

                        if (i2 > 0) {
                            this.placeVine(selector, rand, blockpos4.west(), BlockVine.EAST);
                            this.placeVine(selector, rand, blockpos4.north(), BlockVine.SOUTH);
                            this.placeVine(selector, rand, blockpos4.south(), BlockVine.NORTH);
                        }
                    }
                }
            }
            return true;
        }
    }
}