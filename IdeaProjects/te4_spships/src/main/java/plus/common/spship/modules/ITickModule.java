package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.spship.SpShip;


public interface ITickModule {
    void onTick(SpShip sp);


    static ITickModule getOrNullTickModule(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)return null;
        int[] ints = tag.getIntArray("stm");
        if(ints.length==0)return null;

        switch (ints[0]){
            case 0:
                return new SpRegenShield(
                           ints[1]/100f,
                          ints[2]/100f,
                        ints[3]/100f
                );
            default:
                return null;
        }
    }

}