package plus.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import static plus.block.Block.TE4BLOCKS_TAB;


public class BlockWall extends net.minecraft.block.BlockWall {
    private final boolean isOpaqueCube;

    public BlockWall(String id, Block parent, String name, boolean isOpaqueCube) {
        super(parent);
        setRegistryName(id+"_wall");
        setTranslationKey(name+"_wall");
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