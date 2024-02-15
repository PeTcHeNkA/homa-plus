package net.minecraft.block;

import net.minecraft.block.state.IBlockState;


public class BlockUtils {
    public static boolean canGrowFlower(BlockBush block, IBlockState state){
        return block.canSustainBush(state);
    }
}