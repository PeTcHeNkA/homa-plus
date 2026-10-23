package plus.common.spship.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import plus.common.mashine.item.TehBasicItem;
import plus.utls.MathUtils;
import java.util.List;
import static plus.utls.MathUtils.calcPowerComponent;


public class ItemSpArmor extends TehBasicItem {
    public static ItemSpArmor SP_ARMOR = new ItemSpArmor();
    protected ItemSpArmor() {
        super("sp_armor");
    }


    @Override
    public ItemStack create(int shift, int tehProcess) {
        return this.create(shift, tehProcess, -1);
    }


    public ItemStack create(int shift, int tehProcess, int color) {
        float sd = normal(shift);
        return create(sd, 1f - sd, tehProcess, color);
    }


    @Override
    public ItemStack create(int shift, float power, float damage, int tehProcess) {
        return create(power, damage, tehProcess, -1);
    }


    public ItemStack create(float power, float damage, int tehProcess, int color) {
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort(Strings.TEH_PROC, (short) tehProcess);
//        tag.setFloat(Strings.POWER,
//                (float) (calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProcess)/2)
//        );
        stack.setTagCompound(tag);
//        if(damage<1) {
//            stack.setItemDamage((int) (getMaxDamage() * damage));
//        }
        tag.setInteger("cmd", color);
        return stack;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        if(isMoreInfo()){
            NBTTagCompound tags = item.getTagCompound();
            if(tags==null)return;
            addLore(lore, "teh_prc", tags.getInteger("teh-prc"));
            //addLore(lore, "teh_pow", round(tags.getFloat("power")));
            //addLore(lore, "sp_dur", getMaxDamage()-item.getItemDamage(), getMaxDamage());
        } else {
            addLore(lore, "more_inf");
        }
    }
}