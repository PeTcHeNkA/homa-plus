package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.furnace.SpFurnaceTileEntity;


public class SpFurnace extends SpBasicTE {
    protected SpFurnace() {
        super("sp_furnace");
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpFurnaceTileEntity();
    }
}