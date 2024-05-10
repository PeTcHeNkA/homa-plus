package plus.fserver.world.generator.three;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.server.elca.ChunkSelector;
import java.util.Random;


public class WorldGenMegaPineTreeScaled extends WorldGenMegaPineTree4{
    private final int[] woodMapX;
    private final int[] woodMapZ;

    public WorldGenMegaPineTreeScaled(boolean notify, float range, int baseHeight,
                                      int extraRandomHeight, IBlockState wood,
                                      IBlockState leaves, CrownCreator creator,
                                      SaplingsGenerator sapGen) {
        super(notify, baseHeight, extraRandomHeight, wood, leaves, creator, sapGen);

        float rangeSq = range*range;
        IntArrayList woodX = new IntArrayList((int) rangeSq);
        IntArrayList woodZ = new IntArrayList((int) rangeSq);
        for (int sx = (int) -range; sx<=range; sx++){
            int sxSq = sx*sx;
            for (int sz = (int) -range; sz<=range; sz++){
                if(sxSq + sz*sz < rangeSq){
                    woodX.add(sx);
                    woodZ.add(sz);
                }
            }
        }
        woodMapX = woodX.toIntArray();
        woodMapZ = woodZ.toIntArray();
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
                    this.setBlockAndNotifyAdequately(selector, pos, this.woodMetadata);
                }
                if (j < i - 1) {
                    for (int cursor=0;cursor<woodMapX.length;cursor++){
                        pos = position.add(woodMapX[cursor], j, woodMapZ[cursor]);
                        if (isAirLeaves(selector, pos)) {
                            this.setBlockAndNotifyAdequately(selector, pos, this.woodMetadata);
                        }
                    }
                }
            }
            return true;
        }
    }
}