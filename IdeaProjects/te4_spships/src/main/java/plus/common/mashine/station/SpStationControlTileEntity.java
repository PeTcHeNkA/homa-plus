package plus.common.mashine.station;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import plus.common.mashine.inv.EnergyDeviceTileEntity;


public class SpStationControlTileEntity extends EnergyDeviceTileEntity {
    private float shield;

    public SpStationControlTileEntity(int size) {
        super(10);
    }


    @Override
    public void update() {

    }


    @Override
    public int useOnActiveTick() {
        return 10;
    }


    @Override
    public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
        return null;
    }


    @Override
    public String getGuiID() {
        return "";
    }
}
