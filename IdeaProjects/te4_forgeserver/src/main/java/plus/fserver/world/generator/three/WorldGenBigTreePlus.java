package plus.fserver.world.generator.three;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import java.util.Random;


public class WorldGenBigTreePlus extends WorldGenBigTree {
    private final SaplingsGenerator sapGen;
    public WorldGenBigTreePlus(boolean notify, IBlockState log, IBlockState leaf,
                               double heightAttenuation, double branchSlope,
                               double scaleWidth, double leafDensity,
                               int heightLimitLimit, int trunkSize, SaplingsGenerator sapGen) {
        super(notify, log, leaf, heightAttenuation, branchSlope, scaleWidth,
                leafDensity, heightLimitLimit, trunkSize);
        this.sapGen = sapGen;
    }


    @Override
    public void generateSaplings(World worldIn, Random random, BlockPos pos) {
        sapGen.generateSaplings(worldIn, random, pos);
    }
}