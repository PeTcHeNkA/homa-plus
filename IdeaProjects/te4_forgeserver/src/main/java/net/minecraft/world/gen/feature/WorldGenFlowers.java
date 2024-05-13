package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;


public class WorldGenFlowers extends WorldGenerator {
    private BlockFlower flower;
    private IBlockState state;

    @Keep
    public WorldGenFlowers(BlockFlower block, BlockFlower.EnumFlowerType type) {
        this.setGeneratedBlock(block, type);
    }


    @Keep
    public void setGeneratedBlock(BlockFlower block, BlockFlower.EnumFlowerType type) {
        this.flower = block;
        this.state = block.getDefaultState().withProperty(block.getTypeProperty(), type);
    }


    @Override
    public boolean generate(World world, Random random, BlockPos position) {
        ChunkSelector selector = new ChunkSelector(world);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int num = 0; num < 64; ++num) {
            FastRandom.INST.setRandom8x8x4(pos, position);

            if (pos.getY() > 0 && pos.getY() < 255 && selector.isAirBlock(pos)) {
                pos.setY(pos.getY()-1);
                if(BlockBush.canGrowFlower(flower, selector.getCurState(pos)))
                    selector.setCurStateFast(pos.getX(), pos.getY()+1, pos.getZ(), state);
            }
        }
        return true;
    }
}
