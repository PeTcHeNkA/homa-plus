package plus.fserver.world.generator.future;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CWorldGenerator;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.math.FastRandom;
import java.util.Random;


public class WorldGenStalactite extends CWorldGenerator {
    private final IBlockState headState;
    private final IBlockState tailState;
    private final Material canGrowAt;

    public WorldGenStalactite(){
        this(Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE_WALL.getDefaultState(), Material.ROCK);
    }

    public WorldGenStalactite(IBlockState headState, IBlockState tailState, Material material) {
        this.headState = headState;
        this.tailState = tailState;
        this.canGrowAt = material;
    }


    @Override
    public boolean generate(World worldIn, ChunkSelector selector, Random rand, BlockPos start) {
        if(start.getY() < 1 || start.getY() > 255)return false;
        BlockPos.MutableBlockPos shifted = new BlockPos.MutableBlockPos();
        for(int num = 0; num < 32; ++num) {
            FastRandom.INST.setRandom8x8x4(shifted, start);
            shifted.setY(rand.nextInt() & 255);

            while (growStalactite(selector, rand, shifted) && ++num < 32)
                FastRandom.INST.setRandom8x8x4(
                        shifted,
                        start = new BlockPos(shifted.getX(), shifted.getY(), shifted.getZ())
                );
        }
        return true;
    }


    private boolean growStalactite(ChunkSelector selector, Random rand, BlockPos.MutableBlockPos pos) {
        if (selector.isAirBlock(pos))
            for (int limit = 10, start = pos.getY(), curY = start; curY < 255 && limit-- > 0; curY++) {
                pos.setY(curY);
                IBlockState state;
                if ((state = selector.getCurState(pos)).getBlock().getMaterial(state) == canGrowAt) {
                    start = curY;

                    int headSize = rand.nextInt() & 3 + 1,
                        tailSize = rand.nextInt() & 3 + 1;

                    gen:{
                        while (curY-- > 0 && headSize-- > 0) {
                            pos.setY(curY);
                            if (selector.isCurAir(pos))
                                selector.setCurStateFast(pos.getX(), curY, pos.getZ(), headState);
                            else break gen;
                        }
                        while (curY-- > 0 && tailSize-- > 0) {
                            pos.setY(curY);
                            if (selector.isCurAir(pos))
                                selector.setCurStateFast(pos.getX(), curY, pos.getZ(), tailState);
                            else break gen;
                        }
                    }
                    pos.setY(start);
                    return true;
                }
            }
        return false;
    }
}