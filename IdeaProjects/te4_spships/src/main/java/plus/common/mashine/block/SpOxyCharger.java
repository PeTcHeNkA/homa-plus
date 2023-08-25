package plus.common.mashine.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.oxy.SpOxyChargerTileEntity;
import javax.annotation.Nullable;


public class SpOxyCharger extends SpBasicTE {
    protected SpOxyCharger() {
        super("sp_oxy_charger");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SpOxyChargerTileEntity();
    }
}