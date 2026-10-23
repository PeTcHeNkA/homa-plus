package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.press.SpPressTileEntity;
import javax.annotation.Nullable;


public class SpPress extends SpBasicTE {
    protected SpPress() {
        super("sp_press");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpPressTileEntity();
    }
}