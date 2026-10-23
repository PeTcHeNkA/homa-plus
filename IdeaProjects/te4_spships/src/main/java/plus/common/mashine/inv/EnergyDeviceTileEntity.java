package plus.common.mashine.inv;

import plus.common.block.SpBasicTE;


public abstract class EnergyDeviceTileEntity extends DeviceTileEntity implements IEnergyDependent, ITickEnergyUsage{
    protected IEnergyFolder energyFolder = null;
    public EnergyDeviceTileEntity(int size) {
        super(size);
    }


    public boolean useEnergyIfCan(){
        if(energyFolder == null){
            SpBasicTE.SCANNER.doScan(world, pos);
            if(energyFolder == null){
                energyFolder = IEnergyFolder.EMPTY;
                return false;
            }
        }
        int amount = (int) (speed*useOnActiveTick());
        return energyFolder.takeEnergy(amount);
    }


    @Override
    public void setEnergyFolder(IEnergyFolder folder) {
        this.energyFolder = folder;
    }
}