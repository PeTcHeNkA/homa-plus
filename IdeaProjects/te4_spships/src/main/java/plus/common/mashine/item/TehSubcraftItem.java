package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;


public class TehSubcraftItem extends TehBasicItem {
    public static TehSubcraftItem MICRO_NRM   = new TehSubcraftItem("circuit");
    public static TehSubcraftItem MICRO_CPU   = new TehSubcraftItem("circuit_cpu");
    public static TehSubcraftItem MICRO_ADDON = new TehSubcraftItem("circuit_addon");


    protected TehSubcraftItem(String name){
        this(name, 100);
    }


    protected TehSubcraftItem(String name, int maxDur) {
        super(name);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        if(isMoreInfo()){
            NBTTagCompound tags = item.getTagCompound();
            if(tags==null)return;
            addLore(lore, "teh_prc", tags.getInteger("teh-prc"));
            addLore(lore, "teh_pow", round(tags.getFloat("power")));
            addLore(lore, "sp_dur", getMaxDamage()-item.getItemDamage(), getMaxDamage());
        } else {
            addLore(lore, "more_inf");
        }
    }
}