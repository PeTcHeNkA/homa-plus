package plus.common.spship.modules;

import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.item.IItemCapacitor;
import plus.common.mashine.item.ItemCapacitor;

import static plus.common.spship.SpShip.SLOTS;


public class SpPowerSystem implements IEnergyFolder{
    private final IEnergyFolder all;
    private final IEnergyFolder parent;

    public SpPowerSystem(IEnergyFolder all, IEnergyFolder parent) {
        this.all = all;
        this.parent = parent;
    }

    class VrCell extends IEnergyFolder.Sell{
        public VrCell(int cur, int max) {
            super(cur, max);
        }

        @Override
        public void damage(int amount) {
            parent.damage(amount);
        }
    }


    public static final class Builder{
        IEnergyFolder[] folders = new IEnergyFolder[SLOTS.length];
        int count = 0;

        public void feed(SpInvProxy proxy, int slot){
            if(IItemCapacitor.isCapacitor(proxy.getItem(slot))) {
                folders[count] = new ItemCapacitor.LinkedProvider(proxy.link(slot));
                ++count;
            }
        }


        public IEnergyFolder complete(int vrSize){
            IEnergyFolder sell = new Sell(0, vrSize);
            if(count==0)return sell;
            if(folders.length != count){
                IEnergyFolder[] folders0 = new IEnergyFolder[count];
                System.arraycopy(folders, 0, folders0, 0, count);
                folders = folders0;
            }
            IEnergyFolder sys = new IEnergyFolder.Combined(folders);
            return new SpPowerSystem(new IEnergyFolder.Combined(sys, sell), sys);
        }
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


