package plus.common.mashine.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.utls.MathUtils;
import static plus.utls.MathUtils.calcPowerComponent;


public class TehBasicItem extends BasicItem implements ISimplePowerCraftable {

    protected TehBasicItem(String name) {
        super(name);
    }


    @Override
    public ItemStack create(int shift, int tehProcess){
        float sd = normal(shift);
        return create(shift, sd, 1f - sd, tehProcess);
    }


    public static float normal(int shift){
        return shift/104f + 0.03f;
    }


    public static float getPower(ItemStack item){
        return item.getTagCompound().getFloat(Strings.POWER);
    }


    public static int getTehProcess(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)return -1;
        if(!tag.hasKey(Strings.TEH_PROC))return -1;
        return tag.getInteger(Strings.TEH_PROC);
    }


    public static float calcDamageCoef(ItemStack item){
        return ((float) item.getItemDamage()) / item.getMaxDamage();
    }


    @Override
    public ItemStack create(int shift, float power, float damage, int tehProcess){
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort(Strings.TEH_PROC, (short) tehProcess);
        tag.setFloat(Strings.POWER,
                (float) (calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProcess)/2)
        );
        stack.setTagCompound(tag);
        if(damage<1) {
            int maxDmg = getMaxDamage();
            int dmg = (int) (maxDmg * damage);
            if(dmg>=maxDmg)dmg = maxDmg;
            stack.setItemDamage(dmg);
        }
        return stack;
    }
}