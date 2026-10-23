package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;
import java.util.List;


public class ItemOxyFilterPlate extends BasicItem{
    public static final ItemOxyFilterPlate FILTER_PLATE_V1 = new ItemOxyFilterPlate(1000, "oxy_filter_l1");
    public static final ItemOxyFilterPlate FILTER_PLATE_V2 = new ItemOxyFilterPlate(2500, "oxy_filter_l2");

    public ItemOxyFilterPlate(int maxDmg, String name) {
        super(name);
        setMaxDamage(maxDmg+1);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> lore, ITooltipFlag flag) {
        int maxDmg = getMaxDamage()-1;
        int curDmg = stack.getItemDamage()-1;
        addLore(lore, "sp_dur", maxDmg-curDmg, maxDmg);
    }


    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        stack.setItemDamage(1);
    }


    @Override
    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> stacks) {
        if (this.isInCreativeTab(tabs)) {
            ItemStack stack = new ItemStack(this);
            stack.setItemDamage(1);
            stacks.add(stack);
        }
    }
}