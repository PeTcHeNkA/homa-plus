package plus.common.spship.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import java.util.List;


public final class ItemAccelerator extends ItemModule {
    public static final ItemAccelerator SP_ACCELERATOR_L1 = new ItemAccelerator(1, 4800);
    public static final ItemAccelerator SP_ACCELERATOR_L2 = new ItemAccelerator(2, 7200);
    public static final ItemAccelerator SP_ACCELERATOR_L3 = new ItemAccelerator(3, 11400);
    public static final ItemAccelerator SP_ACCELERATOR_L4 = new ItemAccelerator(4, 17500);

    ItemAccelerator(int lvl, int usages) {
        super("accelerator_l" + lvl, lvl, 3, usages);
    }


    public static float getPower(ItemStack itemStack){
        return getPower(itemStack.getTagCompound());
    }


    public static float getPower(NBTTagCompound tags){
        return 50 + (100 * tags.getFloat(Strings.POWER));
    }


    public static int calcAccEnergyCast(float pow){
        float finSp = pow/12;
        return (int) (finSp*finSp/5);
    }


    @Override
    @SideOnly(Side.CLIENT)
    void addMoreInf(NBTTagCompound tags, List<String> lore) {
        float pow = getPower(tags);
        addLore(lore, "accelerator_speed", round(pow));
        addLore(lore,"energy_cast", calcAccEnergyCast(pow));
    }
}