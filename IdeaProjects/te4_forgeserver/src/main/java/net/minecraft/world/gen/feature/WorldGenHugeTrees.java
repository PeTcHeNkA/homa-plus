package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import plus.fserver.api.Keep;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public abstract class WorldGenHugeTrees extends WorldGenAbstractTree {
    protected final int baseHeight;
    protected final IBlockState woodMetadata;
    protected final IBlockState leavesMetadata;
    protected int extraRandomHeight;

    @Keep
    public WorldGenHugeTrees(boolean notify, int baseHeightIn, int extraRandomHeightIn, IBlockState woodMetadataIn, IBlockState leavesMetadataIn) {
        super(notify);
        this.baseHeight = baseHeightIn;
        this.extraRandomHeight = extraRandomHeightIn;
        this.woodMetadata = woodMetadataIn;
        this.leavesMetadata = leavesMetadataIn;
    }


    @Keep
    protected int getHeight(Random rand) {
        int i = rand.nextInt(3) + this.baseHeight;

        if (this.extraRandomHeight > 1) {
            i += rand.nextInt(this.extraRandomHeight);
        }
        return i;
    }


    private boolean isSpaceAt(ChunkSelector selector, World world, BlockPos leavesPos, int height) {
        boolean flag = true;

        if (leavesPos.getY() >= 1 && leavesPos.getY() + height + 1 <= 256) {
            for (int i = 0; i <= 1 + height; ++i) {
                int j = 2;

                if (i == 0) {
                    j = 1;
                }
                else if (i >= 1 + height - 2) {
                    j = 2;
                }

                for (int k = -j; k <= j && flag; ++k) {
                    for (int l = -j; l <= j && flag; ++l) {
                        if (leavesPos.getY() + i < 0 || leavesPos.getY() + i >= 256 ||
                                !this.isReplaceable(selector, world, leavesPos.add(k, i, l))) {
                            flag = false;
                        }
                    }
                }
            }

            return flag;
        }
        else {
            return false;
        }
    }


    @Keep
    protected boolean ensureDirtsUnderneath(ChunkSelector selector, BlockPos pos, World worldIn) {
        BlockPos blockpos = pos.down();
        IBlockState state = selector.getState(blockpos);
        boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, blockpos, EnumFacing.UP, ((BlockSapling)Blocks.SAPLING));

        if (isSoil && pos.getY() >= 2) {
            this.onPlantGrow(selector, blockpos, pos);
            this.onPlantGrow(selector, blockpos.east(), pos);
            this.onPlantGrow(selector, blockpos.south(), pos);
            this.onPlantGrow(selector, blockpos.south().east(), pos);
            return true;
        } else {
            return false;
        }
    }


    @Keep
    protected boolean ensureGrowable(World worldIn, Random rand, BlockPos treePos, int height) {
        ChunkSelector selector = ElcaThread.cache(worldIn);
        return this.isSpaceAt(selector, worldIn, treePos, height) &&
                this.ensureDirtsUnderneath(selector, treePos, worldIn);
    }


    @Keep
    protected boolean ensureGrowable(ChunkSelector selector, World world, BlockPos treePos, int height) {
        return this.isSpaceAt(selector, world, treePos, height) &&
                this.ensureDirtsUnderneath(selector, treePos, world);
    }


    @Keep
    public void growLeavesLayerStrict(World worldIn, BlockPos layerCenter, int width) {
        growLeavesLayerStrict(ElcaThread.cache(worldIn), layerCenter, width);
    }


    @Keep
    public void growLeavesLayerStrict(ChunkSelector selector, BlockPos layerCenter, int width) {
        int i = width * width;
        boolean notify = needNotify();

        for (int j = -width; j <= width + 1; ++j) {
            for (int k = -width; k <= width + 1; ++k) {
                int l = j - 1;
                int i1 = k - 1;

                if (j * j + k * k <= i || l * l + i1 * i1 <= i || j * j + i1 * i1 <= i || l * l + k * k <= i) {
                    BlockPos blockpos = layerCenter.add(j, 0, k);
                    IBlockState state = selector.getState(blockpos);

                    if (state.getBlock().isAir(state, selector, blockpos) || state.getBlock().isLeaves(state, selector, blockpos)) {
                        setBlockAndNotifyAdequately(selector, blockpos, this.leavesMetadata, notify);
                    }
                }
            }
        }
    }


    @Keep
    protected void growLeavesLayer(World worldIn, BlockPos layerCenter, int width) {
        growLeavesLayer(ElcaThread.cache(worldIn), layerCenter, width);
    }


    @Keep
    protected void growLeavesLayer(ChunkSelector selector, BlockPos layerCenter, int width) {
        WorldServer worldIn = selector.getWorld();
        int i = width * width;
        boolean notify = needNotify();

        for (int j = -width; j <= width; ++j) {
            for (int k = -width; k <= width; ++k) {
                if (j * j + k * k <= i) {
                    BlockPos blockpos = layerCenter.add(j, 0, k);
                    IBlockState state = selector.getState(blockpos);

                    if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                        setBlockAndNotifyAdequately(selector, blockpos, this.leavesMetadata, notify);
                    }
                }
            }
        }
    }


    @Keep
    protected void onPlantGrow(ChunkSelector selector, BlockPos pos, BlockPos source) {
        IBlockState state = selector.getState(pos);
        state.getBlock().onPlantGrow(state, selector.getWorld(), pos, source);
    }
}