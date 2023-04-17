package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.capicator.SpCapacitorTileEntity;
import javax.annotation.Nullable;


public class SpCapacitor extends SpBasicTE {
    protected SpCapacitor() {
        super("sp_capacitor");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpCapacitorTileEntity();
    }
}