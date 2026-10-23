package net.minecraft.world.gen.feature;

import net.minecraft.block.state.IBlockState;
import plus.space.Constants;


public class WorldGenHugeTreesPatch {
    public static IBlockState getLeavesState(WorldGenHugeTrees inst){
        if(Constants.CLIENT)return null;
        return inst.leavesMetadata;
    }


    public static IBlockState getWoodState(WorldGenHugeTrees inst){
        if(Constants.CLIENT)return null;
        return inst.woodMetadata;
    }
}