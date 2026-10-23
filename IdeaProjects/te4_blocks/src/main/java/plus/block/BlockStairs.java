package plus.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static plus.block.Block.TE4BLOCKS_TAB;


public class BlockStairs extends net.minecraft.block.BlockStairs {
    private final boolean isOpaqueCube;

    public BlockStairs(String id, Block parent, String name, boolean isOpaqueCube) {
        super(parent.getDefaultState());
        setRegistryName(id+"_stairs");
        setTranslationKey(name+"_stairs");
        this.isOpaqueCube = isOpaqueCube;

        if(!isOpaqueCube) {
            setLightOpacity(0);
            setLightLevel(0);
        }
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isOpaqueCube;
    }
}