package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;


public class ItemOxyBallon extends TehBasicItem{
    protected ItemOxyBallon(int lvl, int maxSize) {
        super("oxy_ballon_l"+lvl);
        setMaxDamage(maxSize+1);
        setMaxStackSize(1);
    }


    public static boolean isOxyBallon(ItemStack item){
        return item.getItem() instanceof ItemOxyBallon;
    }


    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        stack.setItemDamage(this.getMaxDamage());
    }


    public static int getOxy(ItemStack item){
        return item.getItemDamage();
    }


    public static void setOxy(ItemStack item, int oxy){
        item.setItemDamage(oxy);
    }


    public static int getMaxOxy(ItemStack item){
        return item.getMaxDamage()-1;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        addLore(lore, "cur_oxy", getMaxDamage()-item.getItemDamage(), getMaxDamage()-1);
    }


    @Override
    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> stacks) {
        if (this.isInCreativeTab(tabs)) {
            ItemStack stack = new ItemStack(this);
            stack.setItemDamage(1);
            stacks.add(stack);
            stack = new ItemStack(this);
            stack.setItemDamage(getMaxDamage());
            stacks.add(stack);
        }
    }
}