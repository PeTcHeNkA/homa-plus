package net.minecraft.block.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;


public class BlockPistonStructureHelper {
    private final World world;
    private final BlockPos pistonPos;
    private final BlockPos blockToMove;
    private final EnumFacing moveDirection;
    private final List<BlockPos> toMove = new ArrayList<>();
    private final List<BlockPos> toDestroy = new ArrayListE6<>();
    private final IBlockEAccess access;

    public BlockPistonStructureHelper(World worldIn, BlockPos posIn, EnumFacing pistonFacing, boolean extending) {
        this.world = worldIn;
        this.pistonPos = posIn;
        if (extending) {
            this.moveDirection = pistonFacing;
            this.blockToMove = posIn.offset(pistonFacing);
        } else {
            this.moveDirection = pistonFacing.getOpposite();
            this.blockToMove = posIn.offset(pistonFacing, 2);
        }
        access = new ChunkSelector(world);
    }

    public BlockPistonStructureHelper(World worldIn, IBlockEAccess access, BlockPos posIn, EnumFacing pistonFacing, boolean extending) {
        this.world = worldIn;
        this.pistonPos = posIn;
        if (extending) {
            this.moveDirection = pistonFacing;
            this.blockToMove = posIn.offset(pistonFacing);
        } else {
            this.moveDirection = pistonFacing.getOpposite();
            this.blockToMove = posIn.offset(pistonFacing, 2);
        }
        this.access = access;
    }


    public boolean canMove() {
        this.toMove.clear();
        this.toDestroy.clear();
        IBlockState iblockstate = access.getBlockState(this.blockToMove);

        if (!BlockPistonBase.canPush(iblockstate, this.world, this.blockToMove, this.moveDirection, false, this.moveDirection)) {
            if (iblockstate.getPushReaction() == EnumPushReaction.DESTROY) {
                this.toDestroy.add(this.blockToMove);
                return true;
            } else {
                return false;
            }
        } else if (!this.addBlockLine(this.blockToMove, this.moveDirection)) {
            return false;
        } else {
            IBlockState state;
            BlockPos blockpos;
            for (int i = 0; i < this.toMove.size(); ++i) {
                state = access.getBlockState(blockpos = this.toMove.get(i));
                if (state.getBlock().isStickyBlock(state) && this.addBranchingBlocks(blockpos))
                    return false;
            }
            return true;
        }
    }


    private boolean addBlockLine(BlockPos origin, EnumFacing facing) {
        IBlockState iblockstate = access.getBlockState(origin);
        Block block = iblockstate.getBlock();

        if (iblockstate.getBlock().isAir(iblockstate, this.world, origin)) {
            return true;
        } else if (!BlockPistonBase.canPush(iblockstate, this.world, origin, this.moveDirection, false, facing)) {
            return true;
        } else if (origin.equals(this.pistonPos)) {
            return true;
        } else if (this.toMove.contains(origin)) {
            return true;
        } else {
            int i = 1;

            if (i + this.toMove.size() > 12) {
                return false;
            } else {
                while (block.isStickyBlock(iblockstate)) {
                    BlockPos blockpos = origin.offset(this.moveDirection.getOpposite(), i);
                    iblockstate = access.getBlockState(blockpos);
                    block = iblockstate.getBlock();

                    if (iblockstate.getBlock().isAir(iblockstate, this.world, blockpos) || !BlockPistonBase.canPush(iblockstate, this.world, blockpos, this.moveDirection, false, this.moveDirection.getOpposite()) || blockpos.equals(this.pistonPos)) {
                        break;
                    }

                    ++i;

                    if (i + this.toMove.size() > 12) {
                        return false;
                    }
                }

                int i1 = 0;

                for (int j = i - 1; j >= 0; --j) {
                    this.toMove.add(origin.offset(this.moveDirection.getOpposite(), j));
                    ++i1;
                }

                int j1 = 1;

                IBlockState state;
                BlockPos pos1;
                BlockPos pos2;

                while (true) {
                    int k = this.toMove.indexOf(pos1 = origin.offset(this.moveDirection, j1));

                    if (k > -1) {
                        this.reorderListAtCollision(i1, k);

                        for (int l = 0; l <= k + i1; ++l) {
                            state = access.getBlockState(pos2 = this.toMove.get(l));
                            if (state.getBlock().isStickyBlock(state) && this.addBranchingBlocks(pos2))
                                return false;
                        }
                        return true;
                    }

                    iblockstate = access.getBlockState(pos1);

                    if (iblockstate.getBlock().isAir(iblockstate, this.world, pos1)) {
                        return true;
                    }

                    if (!BlockPistonBase.canPush(iblockstate, this.world, pos1, this.moveDirection, true, this.moveDirection) || pos1.equals(this.pistonPos)) {
                        return false;
                    }

                    if (iblockstate.getPushReaction() == EnumPushReaction.DESTROY) {
                        this.toDestroy.add(pos1);
                        return true;
                    }

                    if (this.toMove.size() >= 12) {
                        return false;
                    }

                    this.toMove.add(pos1);
                    ++i1;
                    ++j1;
                }
            }
        }
    }


    private void reorderListAtCollision(int min, int max) {
        List<BlockPos> list = this.toMove.subList(0, max);
        List<BlockPos> list1 = this.toMove.subList(this.toMove.size() - min, this.toMove.size());
        List<BlockPos> list2 = this.toMove.subList(max, this.toMove.size() - min);
        this.toMove.clear();
        this.toMove.addAll(list);
        this.toMove.addAll(list1);
        this.toMove.addAll(list2);
    }


    private boolean addBranchingBlocks(BlockPos fromPos) {
        for (EnumFacing enumfacing : EnumFacing.VALUES)
            if (enumfacing.getAxis() != this.moveDirection.getAxis()
                    && !this.addBlockLine(fromPos.offset(enumfacing), enumfacing)) return true;
        return false;
    }


    public List<BlockPos> getBlocksToMove() {
        return this.toMove;
    }


    public List<BlockPos> getBlocksToDestroy() {
        return this.toDestroy;
    }
}