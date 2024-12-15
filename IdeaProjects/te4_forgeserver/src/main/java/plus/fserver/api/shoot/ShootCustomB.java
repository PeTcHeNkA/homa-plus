package plus.fserver.api.shoot;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class ShootCustomB extends ShootCustom{
    @Override
    public boolean onHitBlock(ChunkSelector acc, Entity s) {
        BlockPos pos;
        IBlockState state = acc.getState(pos = pos());
        WorldServer world = acc.getWorld();
        Block block;

        Material material = (block = state.getBlock()).getMaterial(state);

        l1:{
            if(material == Material.LEAVES || material == Material.GLASS || material == Material.SNOW || material == Material.ICE) {
                acc.setBlockState(pos, BlockIDIndexes.AIR_STATE);
                break l1;
            }

            if(block == Blocks.PACKED_ICE){
                acc.setBlockState(pos, Blocks.ICE.getDefaultState());
                return (curTargets-=2) < 0;
            }

            if(block == Blocks.STONE){
                if(FastRandom.INST.nextBoolean()){
                    acc.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                }
                return false;
            }

            if(block == Blocks.COBBLESTONE){
                if(FastRandom.INST.nextInt(4)==1){
                    acc.setBlockState(pos, Blocks.GRAVEL.getDefaultState());
                }
                return false;
            }

            if (block == Blocks.TNT) {
                Blocks.TNT.onPlayerDestroy(world, pos, state.withProperty(BlockTNT.EXPLODE, true));
                break l1;
            }

            if(block == Blocks.PLANKS){
                IBlockState stairs = chooseRandomStairs(state);
                acc.setBlockState(pos, stairs);
                return false;
            }

            if(block instanceof BlockStairs){
                IBlockState parent = ((BlockStairs) block).getParent();
                if(parent.getBlock() == Blocks.PLANKS){
                    IBlockState plate = chooseRandomPlate(parent);
                    acc.setBlockState(pos, plate);
                    break l1;
                }
            }

            if(block == Blocks.WOODEN_SLAB){
                if(FastRandom.INST.nextBoolean())
                    return (curTargets-=2) < 0;
            }
            return false;
        }
        return --curTargets < 0;
    }


    private static IBlockState chooseRandomStairs(IBlockState parent){
        BlockPlanks.EnumType val = parent.getValue(BlockPlanks.VARIANT);
        Block stairs;

        switch (val){
            case OAK:
                stairs = Blocks.OAK_STAIRS;break;
            case BIRCH:
                stairs = Blocks.BIRCH_STAIRS;break;
            case ACACIA:
                stairs = Blocks.ACACIA_STAIRS;break;
            case JUNGLE:
                stairs = Blocks.JUNGLE_STAIRS;break;
            case SPRUCE:
                stairs = Blocks.SPRUCE_STAIRS;break;
            case DARK_OAK:
                stairs = Blocks.DARK_OAK_STAIRS;break;
            default:
                return parent;
        }
        ImmutableList<IBlockState> states = stairs.getBlockState().getValidStates();

        return states.get(FastRandom.INST.nextInt(states.size()));
    }


    private static IBlockState chooseRandomPlate(IBlockState parent){
        BlockPlanks.EnumType val = parent.getValue(BlockPlanks.VARIANT);

        IBlockState state = Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockPlanks.VARIANT, val);

        return state.withProperty(BlockSlab.HALF, FastRandom.INST.nextBoolean() ?
                        BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
    }


    @Override
    public boolean onEntityAttacked(DamageSource src, Entity entity) {
        boolean result = super.onEntityAttacked(src, entity);
        if(result) {
            int curPow = super.curTargets;
            int max = super.maxTargets;
            if (curPow == max) {
                AxisAlignedBB aabb = getAABB();
                entity.motionX += ((aabb.maxX - aabb.minX) * 0.5);
                entity.motionY += ((aabb.maxY - aabb.minY) * 0.5);
                entity.motionZ += ((aabb.maxZ - aabb.minZ) * 0.5);
            }
        }
        return result;
    }
}