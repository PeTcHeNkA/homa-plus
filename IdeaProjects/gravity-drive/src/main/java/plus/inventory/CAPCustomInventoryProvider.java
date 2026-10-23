package plus.inventory;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
//import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CAPCustomInventoryProvider implements ICapabilitySerializable<NBTBase> {

    //Инициализация КАПы с помощью аннотации
    @CapabilityInject(ICAPCustomInventory.class)
    public static final Capability<ICAPCustomInventory> INVENTORY_CAP = null;

    private ICAPCustomInventory instance = INVENTORY_CAP.getDefaultInstance();

    //Метод что осуществляет проверку на наличие КАПы
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == INVENTORY_CAP;
    }

    //Метод что осуществляет доступ к КАПе
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == INVENTORY_CAP ? INVENTORY_CAP.<T> cast(this.instance) : null;
    }

    //Метод инициации сохранения информации о инвентаре в НБТ
    @Override
    public NBTBase serializeNBT() {
        return INVENTORY_CAP.getStorage().writeNBT(INVENTORY_CAP, this.instance, null);
    }

    //Метод инициации чтения информации о инвентаре из НБТ
    @Override
    public void deserializeNBT(NBTBase nbt) {
        INVENTORY_CAP.getStorage().readNBT(INVENTORY_CAP, this.instance, null, nbt);
    }

}