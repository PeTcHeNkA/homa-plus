package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public abstract class WorldGenerator {
    private final boolean doBlockNotify;

    @Keep
    public WorldGenerator() {
        this(false);
    }

    @Keep
    public WorldGenerator(boolean notify) {
        this.doBlockNotify = notify;
    }


    @Keep
    public abstract boolean generate(World worldIn, Random rand, BlockPos position);


    @Keep
    public void setDecorationDefaults() {}


    @Keep
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos position){
        return generate(worldIn, rand, position);
    }


    @Keep
    protected void setBlockAndNotifyAdequately(World worldIn, BlockPos pos, IBlockState state) {
        if (this.doBlockNotify) {
            worldIn.setBlockState(pos, state, 3);
        } else {
            int flag = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 2| 16 : 2; //Forge: With bit 5 unset, it will notify neighbors and load adjacent chunks.
            worldIn.setBlockState(pos, state, flag);
        }
    }


    @Keep
    protected void setBlockAndNotifyAdequately(ChunkSelector selector, BlockPos pos, IBlockState state) {
        if (this.doBlockNotify) {
            selector.setBlockState(pos, state, 3);
        } else {
            selector.setBlockState(pos, state, ForgeModContainer.fixVanillaCascading ? 2| 16 : 2);
        }
    }
}