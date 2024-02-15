package plus.fserver.world.generator.three;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import org.jetbrains.annotations.NotNull;
import plus.fserver.utls.ChunkSelector;
import plus.tson.TsonMap;
import java.util.Random;


public abstract class WorldGenHugeTreesPlus extends WorldGenHugeTrees {
    public static CrownCreator JUNGLE(int delta){
        return (inst, selector, worldIn, pos, width) -> {
            for (int j = -2 - delta; j <= 0; ++j) {
                inst.growLeavesLayerStrict(selector, worldIn, pos.up(j), width + 1 - j);
            }
        };
    }


    public static CrownCreator PINE(int height, float floor){
        boolean customHeight = height != 0;
        return (inst, selector, worldIn, pos, param) -> {
            int i = worldIn.rand.nextInt(5) + (customHeight ? inst.baseHeight : height);
            int j = 0;

            int x = pos.getX(), y = pos.getY()+param, z = pos.getZ();

            for (int k = y - i; k <= y; ++k) {
                int l = y - k;
                int i1 = MathHelper.floor((float) l / (float) i * floor);
                inst.growLeavesLayerStrict(selector, worldIn, new BlockPos(x, k, z), i1 + (l > 0 && i1 == j && (k & 1) == 0 ? 1 : 0));
                j = i1;
            }
        };
    }

    private final CrownCreator creator;
    private final SaplingsGenerator sapGen;

    public WorldGenHugeTreesPlus(boolean notify, int baseHeightIn, int extraRandomHeightIn,
                                 IBlockState wood, IBlockState leaves, CrownCreator creator, SaplingsGenerator generator) {
        super(notify, baseHeightIn, extraRandomHeightIn, wood, leaves);
        this.creator = creator;
        this.sapGen = generator;
    }


    public interface CrownCreator{
        void createCrown(WorldGenHugeTreesPlus inst, ChunkSelector selector, World worldIn, BlockPos pos, int param);
    }


    public static CrownCreator crownCreatorOfTson(TsonMap map){
        if(map==null)return JUNGLE(0);
        String type = map.getOrDefaultStr("type", "jungle");
        int delta = map.getOrDefaultInt("delta", 0);
        switch (type){
            case "j":
            case "jungle": return JUNGLE(delta);
            case "p":
            case "pine": return PINE(delta, map.getOrDefaultFloat("floor", 3.5f));
        }
        return JUNGLE(0);
    }


    protected void createCrown(ChunkSelector selector, World worldIn, BlockPos pos, int width){
        creator.createCrown(this, selector, worldIn, pos, width);
    }


    @Override
    public void generateSaplings(@NotNull World worldIn, @NotNull Random random, @NotNull BlockPos pos) {
        if(sapGen!=null) sapGen.generateSaplings(worldIn, random, pos);
    }


    protected boolean isAirLeaves(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos);
    }


    protected boolean isAirLeaves(ChunkSelector selector, BlockPos pos) {
        IBlockState state = selector.getState(pos);
        return state.getBlock().isAir(state, selector.getWorld(), pos) || state.getBlock().isLeaves(state, selector.getWorld(), pos);
    }
}