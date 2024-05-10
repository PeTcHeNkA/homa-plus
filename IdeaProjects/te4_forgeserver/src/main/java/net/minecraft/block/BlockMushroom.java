package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenerator;
import plus.fserver.server.elca.ChunkSelector;


public class BlockMushroom extends BlockBush implements IGrowable {
    protected static final AxisAlignedBB MUSHROOM_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

    protected BlockMushroom() {
        this.setTickRandomly(true);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return MUSHROOM_AABB;
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(25) == 0) {
            int i = 5;
            ChunkSelector selector = new ChunkSelector(worldIn);
            for (BlockPos blockpos :
                    BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
                if (selector.getState(blockpos).getBlock() == this) {
                    --i;

                    if (i <= 0) {
                        return;
                    }
                }
            }

            BlockPos blockpos1 = pos.add(
                    rand.nextInt(3) - 1,
                    rand.nextInt(2) - rand.nextInt(2),
                    rand.nextInt(3) - 1
            );

            for (int k = 0; k < 4; ++k) {
                if (selector.isAirBlock(blockpos1) && this.canBlockStay(worldIn, blockpos1, this.getDefaultState())) {
                    pos = blockpos1;
                }
                blockpos1 = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
            }

            if (selector.isAirBlock(blockpos1) && this.canBlockStay(worldIn, blockpos1, this.getDefaultState())) {
                selector.setBlockState(blockpos1, this.getDefaultState(), 2);
            }
        }
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && this.canBlockStay(worldIn, pos, this.getDefaultState());
    }


    @Override
    protected boolean canSustainBush(IBlockState state)
    {
        return state.isFullBlock();
    }


    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            Chunk chunk = worldIn.getChunk(pos);
            IBlockState iblockstate = chunk.getBlockState(pos.getX(), pos.getY()-1, pos.getZ());

            if (iblockstate.getBlock() == Blocks.MYCELIUM) {
                return true;
            } else if (iblockstate.getBlock() == Blocks.DIRT
                    && iblockstate.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL) {
                return true;
            } else {
                return chunk.getLightSubtracted(pos,0) < 13 && iblockstate.getBlock().canSustainPlant(iblockstate, worldIn, pos.down(), EnumFacing.UP, this);
            }
        } else {
            return false;
        }
    }


    public boolean generateBigMushroom(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        worldIn.setBlockToAir(pos);
        WorldGenerator worldgenerator = null;

        if (this == Blocks.BROWN_MUSHROOM) {
            worldgenerator = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
        } else if (this == Blocks.RED_MUSHROOM) {
            worldgenerator = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
        }
        if (worldgenerator != null && worldgenerator.generate(worldIn, rand, pos)) {
            return true;
        } else {
            worldIn.setBlockState(pos, state, 3);
            return false;
        }
    }


    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }


    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return (double)rand.nextFloat() < 0.4D;
    }


    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        this.generateBigMushroom(worldIn, pos, state, rand);
    }
}