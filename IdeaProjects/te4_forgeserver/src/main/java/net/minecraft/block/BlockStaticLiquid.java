package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import plus.fserver.utls.ChunkSelector;


public class BlockStaticLiquid extends BlockLiquid {
    protected BlockStaticLiquid(Material materialIn) {
        super(materialIn);
        this.setTickRandomly(false);

        if (materialIn == Material.LAVA) {
            this.setTickRandomly(true);
        }
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        if (!this.checkForMixing(worldIn, selector, pos, state)) {
            BlockDynamicLiquid blockdynamicliquid = getFlowingBlock(this.material);
            selector.setBlockState(pos,
                    blockdynamicliquid.getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), 2
            );
            worldIn.scheduleUpdate(pos, blockdynamicliquid, this.tickRate(worldIn));
        }
    }


//    private void updateLiquid(World worldIn, ChunkSelector selector, BlockPos pos, IBlockState state) {
//        BlockDynamicLiquid blockdynamicliquid = getFlowingBlock(this.material);
//        selector.setBlockState(pos, blockdynamicliquid.getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), 2);
//        worldIn.scheduleUpdate(pos, blockdynamicliquid, this.tickRate(worldIn));
//    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.material == Material.LAVA) {
            if (worldIn.getGameRules().getBoolean("doFireTick")) {
                int i = rand.nextInt(3);
                ChunkSelector selector = new ChunkSelector(worldIn);
                if (i > 0) {
                    BlockPos blockpos = pos;

                    for (int j = 0; j < i; ++j) {
                        blockpos = blockpos.add(
                                rand.nextInt(3) - 1,
                                1,
                                rand.nextInt(3) - 1
                        );

                        if (blockpos.getY() >= 0 && blockpos.getY() < worldIn.getHeight() && !worldIn.isBlockLoaded(blockpos)) {
                            return;
                        }

                        IBlockState block = selector.getState(blockpos);

                        if (block.getBlock().isAir(block, worldIn, blockpos)) {
                            if (this.isSurroundingBlockFlammable(worldIn, selector, blockpos)) {
                                selector.setBlockState(blockpos, ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, blockpos, pos, Blocks.FIRE.getDefaultState()), 3);
                                return;
                            }
                        } else if (block.getMaterial().blocksMovement()) {
                            return;
                        }
                    }
                } else {
                    BlockPos temp;
                    for (int k = 0; k < 3; ++k) {
                        BlockPos blockpos1 = pos.add(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1);

                        if (blockpos1.getY() >= 0 && blockpos1.getY() < 256 && !worldIn.isBlockLoaded(blockpos1)) {
                            return;
                        }

                        if (selector.isAirBlock(temp = blockpos1.up()) && this.getCanBlockBurn(worldIn, selector, blockpos1)) {
                            selector.setBlockState(temp, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, temp, pos, Blocks.FIRE.getDefaultState()), 3);
                        }
                    }
                }
            }
        }
    }


    protected boolean isSurroundingBlockFlammable(World worldIn, BlockPos pos) {
        return isSurroundingBlockFlammable(worldIn, new ChunkSelector(worldIn), pos);
    }


    protected boolean isSurroundingBlockFlammable(World worldIn, ChunkSelector selector, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (this.getCanBlockBurn(worldIn, selector, pos.offset(enumfacing))) {
                return true;
            }
        }
        return false;
    }


    private boolean getCanBlockBurn(World worldIn, ChunkSelector selector, BlockPos pos) {
        return (pos.getY() < 0 || pos.getY() >= 256 || worldIn.isBlockLoaded(pos)) && selector.getState(pos).getMaterial().getCanBurn();
    }
}