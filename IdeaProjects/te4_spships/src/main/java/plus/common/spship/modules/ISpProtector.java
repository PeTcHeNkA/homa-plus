package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import plus.common.spship.SPM;
import plus.common.spship.SpShip;


public interface ISpProtector {
    float onDamage(SpShip sp, DamageSource src, float amount);
    ISpProtector EMPTY = (sp, src, amount) -> amount;

    static ISpProtector ofItemStack(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)return EMPTY;
        int[] ints = tag.getIntArray("spg");
        if(ints.length==0)return EMPTY;

        return new SpProtector(
                SPM.getIOf(ints, 1),
                SPM.getIOf(ints, 1),
                SPM.getIOf(ints, 2),
                SPM.getIOf(ints, 3)
        );
    }
}
