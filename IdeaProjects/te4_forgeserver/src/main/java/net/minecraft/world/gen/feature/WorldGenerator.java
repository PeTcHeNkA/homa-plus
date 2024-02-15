package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import plus.fserver.utls.ChunkSelector;


public abstract class WorldGenerator {
    private final boolean doBlockNotify;

    public WorldGenerator() {
        this(false);
    }

    public WorldGenerator(boolean notify) {
        this.doBlockNotify = notify;
    }


    public abstract boolean generate(World worldIn, Random rand, BlockPos position);


    public void setDecorationDefaults() {}


    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position){
        return generate(worldIn, rand, position);
    }


    protected void setBlockAndNotifyAdequately(World worldIn, BlockPos pos, IBlockState state) {
        if (this.doBlockNotify) {
            worldIn.setBlockState(pos, state, 3);
        } else {
            int flag = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 2| 16 : 2; //Forge: With bit 5 unset, it will notify neighbors and load adjacent chunks.
            worldIn.setBlockState(pos, state, flag);
        }
    }


    protected void setBlockAndNotifyAdequately(ChunkSelector selector, BlockPos pos, IBlockState state) {
        if (this.doBlockNotify) {
            selector.setBlockState(pos, state, 3);
        } else {
            selector.setBlockState(pos, state, ForgeModContainer.fixVanillaCascading ? 2| 16 : 2);
        }
    }
}