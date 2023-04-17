package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.spship.SPM;
import plus.common.spship.SpShip;

public interface ISpGun {
    void shoot(SpShip sp, EntityLivingBase shooter, float yaw, float pith);
    ISpGun EMPTY = (sp, shooter, yaw, pith) -> {};


    static ISpGun ofItemStack(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)return EMPTY;
        int[] ints = tag.getIntArray("spg");
        if(ints.length==0)return EMPTY;

        return new SpGun(
                SpGun.AmmType.valueOf(ints[0]),
                SPM.getIOf(ints, 1),
                SPM.getIOf(ints, 2),
                SPM.getIOf(ints, 3),
                SPM.getIOfI(ints, 4)
        );
    }
}