package plus.common.spship.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.item.ItemCapacitor;
import plus.common.spship.SpShip;
import static plus.common.spship.SpShip.SLOTS;


public class SpPowerSystem implements IEnergyFolder{
    private final IEnergyFolder all;
    private final IEnergyFolder parent;

    class VrCell extends IEnergyFolder.Sell{
        public VrCell(int cur, int max) {
            super(cur, max);
        }

        @Override
        public void damage(int amount) {
            parent.damage(amount);
        }
    }


    public SpPowerSystem(SpShip spShip, EntityPlayer player, int vrSize) {
        IEnergyFolder sys = scanSys(spShip, player);
        if(sys != null) {
            all = new IEnergyFolder.Combined(parent = sys, new Sell(vrSize));
        } else {
            all = parent = new Sell(vrSize);
        }
    }


    private static IEnergyFolder scanSys(SpShip spShip, EntityPlayer player){
        InventoryPlayer inv = player.inventory;
        IEnergyFolder[] folders = new IEnergyFolder[SLOTS.length];
        int count = 0;
        for(int slot:SLOTS){
            ItemStack item = inv.getStackInSlot(slot);
            if(ItemCapacitor.isCapacitor(item)){
                folders[count] = new ItemCapacitor.LinkedProvider(new FInvItemLink(inv, slot, item));
                ++count;
            }
        }
        if(count==0)return null;

        if(folders.length != count){
            IEnergyFolder[] folders0 = new IEnergyFolder[count];
            System.arraycopy(folders, 0, folders0, 0, count);
            return new IEnergyFolder.Combined(folders0);
        }
        return new IEnergyFolder.Combined(folders);
    }


    @Override
    public boolean takeEnergy(int amount) {
        return all.takeEnergy(amount);
    }


    @Override
    public boolean putEnergy(int amount) {
        return all.putEnergy(amount);
    }


    @Override
    public int getCurEnergy() {
        return all.getCurEnergy();
    }


    @Override
    public int getMaxEnergy() {
        return all.getMaxEnergy();
    }


    @Override
    public void damage(int amount) {
        parent.damage(amount);
    }
}


