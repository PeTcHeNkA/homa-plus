package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.lincker.SpCraftTileEntity;


public class SpCraftTable extends SpBasicTE {
    protected SpCraftTable() {
        super("sp_craft_table");
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpCraftTileEntity();
    }
}