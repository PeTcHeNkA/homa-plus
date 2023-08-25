package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.spship.SpShip;


public interface ITickModule {
    void onTick(SpShip sp);


    static ITickModule getOrNullTickModule(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)return null;
        int[] ints = tag.getIntArray(Strings.SP_TICK_MODULE);
        if(ints.length==0)return null;

        switch (ints[0]){
            case 0:
                return new SpRegenShield(
                          ints[2],
                        ints[3]
                );
            default:
                return null;
        }
    }

}