package plus.common.spship.item;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import java.util.List;


public class ItemRegenShield extends ItemModule{
    public static final ItemModule SP_REGEN_L1 = new ItemRegenShield(1, 3400 );
    public static final ItemModule SP_REGEN_L2 = new ItemRegenShield(2, 5200 );
    public static final ItemModule SP_REGEN_L3 = new ItemRegenShield(3, 6800 );
    public static final ItemModule SP_REGEN_L4 = new ItemRegenShield(4, 8500 );


    ItemRegenShield(int lvl, int usages) {
        super("shield_l"+lvl, lvl, 2, usages);
    }


    public static float getPower(ItemStack itemStack){
        return getPower(itemStack.getTagCompound());
    }


    public static float getPower(NBTTagCompound tags){
        return 5 + (10 * tags.getFloat(Strings.POWER));
    }


    public static int calcAccEnergyCast(float pow){
        float finSp = pow/2;
        return (int) (finSp*finSp/2);
    }


    @Override
    @SideOnly(Side.CLIENT)
    void addMoreInf(ItemStack item, List<String> lore) {
        float pow = getPower(item);
        addLore(lore, "shield_gen_speed", round(pow));
        addLore(lore,"energy_cast", calcAccEnergyCast(pow));
    }
}