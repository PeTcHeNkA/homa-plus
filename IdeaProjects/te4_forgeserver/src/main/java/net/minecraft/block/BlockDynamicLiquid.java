package net.minecraft.block;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class BlockDynamicLiquid extends BlockLiquid {
    @Keep
    int adjacentSourceBlocks;

    protected BlockDynamicLiquid(Material materialIn) {
        super(materialIn);
    }


    private void placeStaticBlock(ChunkSelector selector, BlockPos pos, IBlockState currentState) {
        selector.setBlockState(pos, getStaticBlock(this.material).getDefaultState().withProperty(LEVEL, currentState.getValue(LEVEL)));
    }


    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isAreaLoaded(pos, this.getSlopeFindDistance(worldIn))) return; // Forge: avoid loading unloaded chunks
        int level = state.getValue(LEVEL);
        int num2 = 1;

        if (this.material == Material.LAVA && !worldIn.provider.doesWaterVaporize()) {
            num2 = 2;
        }
        int tickRate = this.tickRate(worldIn);
        ChunkSelector selector = new ChunkSelector(worldIn);

        if (level > 0) {
            int num = -100;
            this.adjacentSourceBlocks = 0;

            for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
                num = this.checkAdjacentBlock(selector, pos.offset(enumfacing), num);
            }

            int i1 = num + num2;

            if (i1 >= 8 || num < 0) {
                i1 = -1;
            }

            int depth = this.getDepth(selector.getState(pos.up()));

            if (depth >= 0) {
                if (depth >= 8) {
                    i1 = depth;
                } else {
                    i1 = depth + 8;
                }
            }

            if (this.adjacentSourceBlocks >= 2 && ForgeEventFactory.canCreateFluidSource(worldIn, pos, state, this.material == Material.WATER)) {
                IBlockState iblockstate = selector.getState(pos.down());

                if (iblockstate.getMaterial().isSolid()) {
                    i1 = 0;
                } else if (iblockstate.getMaterial() == this.material && iblockstate.getValue(LEVEL) == 0) {
                    i1 = 0;
                }
            }

            if (this.material == Material.LAVA && level < 8 && i1 < 8 && i1 > level && rand.nextInt(4) != 0) {
                tickRate *= 4;
            }
            if (i1 == level) {
                this.placeStaticBlock(selector, pos, state);
            } else {
                level = i1;
                if (i1 < 0) {
                    selector.setBlockState(pos, BlockIDIndexes.AIR_STATE, 3);
                } else {
                    selector.setBlockState(pos, state.withProperty(LEVEL, i1));
                    worldIn.scheduleUpdate(pos, this, tickRate);
                    worldIn.notifyNeighborsOfStateChange(pos, this, false);
                }
            }
        } else {
            this.placeStaticBlock(selector, pos, state);
        }
        BlockPos down = pos.down();
        if(down.getY()<1)return;

        IBlockState state1 = selector.getState(down);

        if (this.canFlowInto(state1)) {
            if (this.material == Material.LAVA && worldIn.getBlockState(down).getMaterial() == Material.WATER) {
                selector.setBlockState(
                        down,
                        ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, down, pos, BlockIDIndexes.STONE_STATE)
                );
                this.triggerMixEffects(worldIn, down);
                return;
            }

            if (level >= 8) {
                this.tryFlowInto(selector, worldIn, down, state1, level);
            } else {
                this.tryFlowInto(selector, worldIn, down, state1, level + 8);
            }
        } else if (level >= 0 && (level == 0 || this.isBlocked(state1))) {
            Set<EnumFacing> set = this.getPossibleFlowDirections(selector, pos);
            int k1 = level + num2;

            if (level >= 8) {
                k1 = 1;
            }
            if (k1 >= 8) {
                return;
            }

            for (EnumFacing facing : set) {
                this.tryFlowInto(selector, worldIn, pos.offset(facing),
                        selector.getState(pos.offset(facing)), k1);
            }
        }
    }


    private void tryFlowInto(ChunkSelector selector, World worldIn, BlockPos pos, IBlockState state, int level) {
        if (this.canFlowInto(state)) {
            if (state.getMaterial() != Material.AIR) {
                if (this.material == Material.LAVA) {
                    this.triggerMixEffects(worldIn, pos);
                } else {
                    if (state.getBlock() != Blocks.SNOW_LAYER) //Forge: Vanilla has a 'bug' where snowballs don't drop like every other block. So special case because ewww...
                        state.getBlock().dropBlockAsItem(worldIn, pos, state, 0);
                }
            }
            selector.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, level), 3);
        }
    }


    private int getSlopeDistance(ChunkSelector selector, BlockPos pos, int distance, EnumFacing calculateFlowCost) {
        int i = 1000;
        BlockPos blockpos;
        IBlockState iblockstate;
        for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
            if (enumfacing != calculateFlowCost) {
                blockpos = pos.offset(enumfacing);
                iblockstate = selector.getState(blockpos);

                if (!this.isBlocked(iblockstate) && (iblockstate.getMaterial() != this.material || iblockstate.getValue(LEVEL) > 0)) {
                    if (!this.isBlocked(selector.getCurState(blockpos.down()))) {
                        return distance;
                    }

                    if (distance < this.getSlopeFindDistance(selector.getWorld())) {
                        int j = this.getSlopeDistance(selector, blockpos, distance + 1, enumfacing.getOpposite());

                        if (j < i) {
                            i = j;
                        }
                    }
                }
            }
        }
        return i;
    }


    private int getSlopeFindDistance(World worldIn) {
        return this.material == Material.LAVA && !worldIn.provider.doesWaterVaporize() ? 2 : 4;
    }


    private Set<EnumFacing> getPossibleFlowDirections(ChunkSelector selector, BlockPos pos) {
        int i = 1000;
        Set<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);
        BlockPos blockpos;
        IBlockState iblockstate;
        for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
            blockpos = pos.offset(enumfacing);
            iblockstate = selector.getState(blockpos);

            if (!this.isBlocked(iblockstate) && (iblockstate.getMaterial() != this.material || iblockstate.getValue(LEVEL) > 0)) {
                int j;
                if (this.isBlocked(selector.getState(blockpos.down()))) {
                    j = this.getSlopeDistance(selector, blockpos, 1, enumfacing.getOpposite());
                } else {
                    j = 0;
                }
                if (j < i) {
                    set.clear();
                }
                if (j <= i) {
                    set.add(enumfacing);
                    i = j;
                }
            }
        }
        return set;
    }


    private boolean isBlocked(IBlockState state) {
        Block block = state.getBlock(); //Forge: state must be valid for position

        if (block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS && !(block instanceof BlockDoor)){
            Material mat = state.getMaterial();
            return mat == Material.PORTAL || mat == Material.STRUCTURE_VOID || mat.blocksMovement();
        } else {
            return true;
        }
    }


    protected int checkAdjacentBlock(ChunkSelector selector, BlockPos pos, int currentMinLevel) {
        int num = this.getDepth(selector.getState(pos));
        if (num < 0) {
            return currentMinLevel;
        } else {
            if (num == 0) {
                ++this.adjacentSourceBlocks;
            }
            if (num >= 8) {
                num = 0;
            }
            return currentMinLevel >= 0 && num >= currentMinLevel ? currentMinLevel : num;
        }
    }


    private boolean canFlowInto(IBlockState state) {
        Material material = state.getMaterial();
        return material != this.material && material != Material.LAVA && !this.isBlocked(state);
    }


    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!this.checkForMixing(world, pos, state)) {
            world.scheduleUpdate(pos, this, this.tickRate(world));
        }
    }
}