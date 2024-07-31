package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.api.IBlockEAccess;


public class BlockFrostedIce extends BlockIce {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public BlockFrostedIce() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return (Integer)state.getValue(AGE);
    }


    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, MathHelper.clamp(meta, 0, 3));
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if ((rand.nextInt(3) == 0 || this.countNeighbors(world, pos) < 4) && world.getLightFromNeighbors(pos) > 11 - state.getValue(AGE) - state.getLightOpacity()) {
            this.slightlyMelt(world, pos, state, rand, true);
        } else {
            world.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        }
    }


    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos pos2) {
        if (block == this) {
            int count = this.countNeighbors(world, pos);
            if (count < 2) {
                this.turnIntoWater(world, pos);
            }
        }
    }


    private int countNeighbors(World world, BlockPos pos) {
        int count = 0;
        IBlockEAccess access = world.access();
        BlockPos.MutableBlockPos mut = world.localTempPos;
        for (EnumFacing facing : EnumFacing.VALUES) {
            mut.setPosB(
                    pos.getX() + facing.getXOffset(),
                    pos.getY() + facing.getYOffset(),
                    pos.getZ() + facing.getZOffset()
            );
            if (access.getBlockState(mut).getBlock() == this) {
                ++count;
                if (count >= 4) {
                    return count;
                }
            }
        }
        return count;
    }


    protected void slightlyMelt(World world, BlockPos pos, IBlockState state, Random rand, boolean flag) {
        int age = state.getValue(AGE);
        if (age < 3) {
            world.access().setBlockState(pos, state.withProperty(AGE, age + 1), 2);
            world.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        } else {
            this.turnIntoWater(world, pos);
            if (flag) {
                BlockPos.MutableBlockPos mut = world.localTempPos;
                IBlockEAccess access = world.access();
                for (EnumFacing facing : EnumFacing.VALUES) {
                    mut.setPosB(
                            pos.getX() + facing.getXOffset(),
                            pos.getY() + facing.getYOffset(),
                            pos.getZ() + facing.getZOffset()
                    );
                    IBlockState state1 = access.getBlockState(mut);
                    if (state1.getBlock() == this) {
                        this.slightlyMelt(world, mut, state1, rand, false);
                    }
                }
            }
        }
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }


    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return ItemStack.EMPTY;
    }
}