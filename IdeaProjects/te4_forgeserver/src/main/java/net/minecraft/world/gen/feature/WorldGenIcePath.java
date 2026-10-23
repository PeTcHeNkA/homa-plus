package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenIcePath extends CWorldGenerator {
    private final IBlockState state;
    private final int basePathWidth;

    @Keep
    public WorldGenIcePath(int width) {
        this.state = Blocks.PACKED_ICE.getDefaultState();
        this.basePathWidth = width;
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(pos);
        while(selector.isAirBlock(mut) && mut.getY() > 2) {
            mut.setY(mut.getY()-1);
        }
        if (canPlaceAt(selector.getCurState(mut).getBlock())) {
            int factor = rand.nextInt(this.basePathWidth - 2) + 2;
            pos = mut;
            for(int posX = pos.getX() - factor; posX <= pos.getX() + factor; ++posX) {
                for(int posZ = pos.getZ() - factor; posZ <= pos.getZ() + factor; ++posZ) {
                    int deltaX = posX - pos.getX();
                    int deltaZ = posZ - pos.getZ();
                    if (deltaX * deltaX + deltaZ * deltaZ <= factor * factor) {
                        for(int posY = pos.getY() - 1; posY <= pos.getY() + 1; ++posY) {
                            if (canReplace(selector.getBlockState(posX, posY, posZ))) {
                                selector.setStateFast(posX, posY, posZ, state);
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    protected boolean canPlaceAt(Block block){
        return block == Blocks.SNOW;
    }


    protected boolean canReplace(IBlockState state){
        Block block1 = state.getBlock();
        return block1 == Blocks.DIRT || block1 == Blocks.SNOW || block1 == Blocks.ICE;
    }
}