package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;

import javax.annotation.Nullable;
import java.util.List;


public class QuantificatorItem extends BasicItem implements IMultiModel {
    public QuantificatorItem() {
        super("quantificator");
        this.setHasSubtypes(true);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public String[] getModels() {
        return new String[]{"physic", "energy", "plasm", "shock"};
    }


    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey()+"."+getType(stack.getMetadata());
    }


    public static String getType(int meta){
        switch (meta){
            default:
                return "physic";
            case 1:
                return "energy";
            case 2:
                return "plasm";
            case 3:
                return "shock";
        }
    }


    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
            items.add(new ItemStack(this, 1, 2));
            items.add(new ItemStack(this, 1, 3));
        }
    }


    @Override
    public void addInformation(ItemStack item, @Nullable World worldIn, List<String> lore, ITooltipFlag flagIn) {
        String type = getType(item.getMetadata());
        addLore(lore, "quant_lore_base");
        addLore(lore, "quant_lore_1_"+type);
        addLore(lore, "quant_lore_2_"+type);
    }
}