package plus.common.spship.item;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import plus.utls.MathUtils;
import java.util.List;
import static plus.utls.MathUtils.calcPowerComponent;


public class ItemProtector extends ItemModule{
    //TODO сбалансировать к-во использований
    public static final ItemProtector SP_PROTECTOR_L1 = new ItemProtector(1, 4800);
    public static final ItemProtector SP_PROTECTOR_L2 = new ItemProtector(2, 7200);
    public static final ItemProtector SP_PROTECTOR_L3 = new ItemProtector(3, 11400);
    public static final ItemProtector SP_PROTECTOR_L4 = new ItemProtector(4, 17500);

    ItemProtector(int lvl, int usages) {
        super("protect_l"+lvl, lvl, 4, usages);
    }


    public static int calcAccEnergyCoef(float pow){
        float finSp = pow*0.29f;
        return 1+(int) ((finSp*finSp)*0.5f);
    }


    @Override
    public ItemStack create(int shift, float power, float damage, int tehProcess){
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort(Strings.TEH_PROC, (short) tehProcess);

        float finPower = (float) (calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProcess)/2);
        switch (lvl){
            case 2: finPower *= 1.2f;break;
            case 3: finPower *= 1.4f;break;
            case 4: finPower *= 1.6f;break;
        }
        float energyPow = normal(shift);
        float physicPow = 1f - energyPow;
        energyPow *= finPower * 30;
        physicPow *= finPower * 30;

        tag.setIntArray(Strings.SP_MODULE_INTS, new int[]{
                calcAccEnergyCoef(physicPow),
                calcAccEnergyCoef(energyPow),
                1+(int) physicPow,
                1+(int) energyPow
        });

        stack.setTagCompound(tag);
        if(damage<1) {
            stack.setItemDamage((int) (getMaxDamage() * damage));
        }
        return stack;
    }


    @Override
    @SideOnly(Side.CLIENT)
    void addMoreInf(ItemStack item, List<String> lore) {
        int[] ints = item.getTagCompound().getIntArray(Strings.SP_MODULE_INTS);
        if(ints.length<4)return;
        int physEnrgCoef = ints[0];
        int enrgEnrgCoef = ints[1];
        float physDmgCoef = ints[2];
        float enrgDmgCoef = ints[3];

        addLore(lore, "protector_phys", (int)physDmgCoef, physEnrgCoef);
        addLore(lore, "protector_enrg", (int)enrgDmgCoef, enrgEnrgCoef);
    }
}