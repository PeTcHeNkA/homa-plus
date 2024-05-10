package plus.fserver.world.generator.three;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.server.elca.ChunkSelector;
import java.util.Random;


public class WorldGenMegaPineTree4 extends WorldGenHugeTreesPlus{
    public WorldGenMegaPineTree4(boolean notify, int baseHeight, int extraRandomHeight, IBlockState wood, IBlockState leaves, CrownCreator creator, SaplingsGenerator sapGen) {
        super(notify, baseHeight, extraRandomHeight, wood, leaves, creator, sapGen);
    }


    @Override
    public boolean generate(@NotNull World worldIn, @NotNull Random rand, @NotNull BlockPos position) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        int i = this.getHeight(rand);
        if (!this.ensureGrowable(selector, worldIn, position, i)) {
            return false;
        } else {
            this.createCrown(selector, worldIn, position, i);

            for (int j = -2; j < i; ++j) {
                BlockPos pos;
                if (isAirLeaves(selector, pos = position.up(j))) {
                    this.setBlockAndNotifyAdequately(worldIn, pos, this.woodMetadata);
                }

                if (j < i - 1) {
                    if (isAirLeaves(selector, pos = position.add(1, j, 0))) {
                        this.setBlockAndNotifyAdequately(worldIn, pos, this.woodMetadata);
                    }

                    if (isAirLeaves(selector, pos = position.add(1, j, 1))) {
                        this.setBlockAndNotifyAdequately(worldIn, pos, this.woodMetadata);
                    }

                    if (isAirLeaves(selector, pos = position.add(0, j, 1))) {
                        this.setBlockAndNotifyAdequately(worldIn, pos, this.woodMetadata);
                    }
                }
            }
            return true;
        }
    }
}