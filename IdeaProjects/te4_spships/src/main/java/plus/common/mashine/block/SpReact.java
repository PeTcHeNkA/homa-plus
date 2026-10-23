package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.react.SpReactTileEntity;


public class SpReact extends SpBasicTE {
    protected SpReact() {
        super("sp_react");
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpReactTileEntity();
    }
}