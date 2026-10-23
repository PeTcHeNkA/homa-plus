package plus.fserver.world.generator.three;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.jetbrains.annotations.NotNull;
import java.util.Random;


public class WorldGenTreesPlus extends WorldGenTrees {
    private final SaplingsGenerator sapGen;
    public WorldGenTreesPlus(boolean notify, int minTreeHeightIn,
                             IBlockState woodMeta, IBlockState metaLeaves, boolean growVines,
                             SaplingsGenerator sapGen
                              ) {
        super(notify, minTreeHeightIn, woodMeta, metaLeaves, growVines);
        this.sapGen = sapGen;
    }


    @Override
    public void generateSaplings(@NotNull World worldIn, @NotNull Random random, @NotNull BlockPos pos) {
        if(sapGen!=null) sapGen.generateSaplings(worldIn, random, pos);
    }
}