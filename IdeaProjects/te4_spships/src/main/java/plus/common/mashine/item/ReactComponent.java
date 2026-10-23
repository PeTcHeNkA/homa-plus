package plus.common.mashine.item;


import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;

import java.util.List;


public class ReactComponent extends TehBasicItem {
    public static final ReactComponent ENERGY_L1 = RC("react_energy", 1, 8750);
    public static final ReactComponent ENERGY_L2 = RC("react_energy", 2, 14400);
    public static final ReactComponent ENERGY_L3 = RC("react_energy", 3, 19600);
    public static final ReactComponent ENERGY_L4 = RC("react_energy", 4, 25200);

    public enum TYPE{CORE, COMPONENT, BOOSTER}
    public final int level;


    ReactComponent(String name, int lvl, int usages){
        super(name);
        setMaxDamage(usages);
        setMaxStackSize(1);
        this.level = lvl;
    }

    static ReactComponent RC(String name, int lvl, int usages){
        return new ReactComponent(name+"_l"+lvl, lvl, usages);
    }


    public static boolean isReactCore(ItemStack item){
        if(ReactComponent.isReactComponent(item)){
            ReactComponent rc = (ReactComponent) item.getItem();
            return rc.getType() == ReactComponent.TYPE.CORE;
        }
        return false;
    }


    public static boolean isReactNoCore(ItemStack item){
        if(ReactComponent.isReactComponent(item)){
            ReactComponent rc = (ReactComponent) item.getItem();
            return rc.getType() != ReactComponent.TYPE.CORE;
        }
        return false;
    }


    public static boolean isReactComponent(ItemStack item){
        return item.getItem() instanceof ReactComponent;
    }


    public TYPE getType(){
        return TYPE.COMPONENT;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        if(isMoreInfo()){
            NBTTagCompound tags = item.getTagCompound();
            if(tags==null)return;
            addLore(lore, "teh_prc", tags.getShort(Strings.TEH_PROC));
            addLore(lore, "teh_pow", round(tags.getFloat(Strings.POWER)+level/2f));
            addLore(lore, "sp_dur", getMaxDamage()-item.getItemDamage(), getMaxDamage());
        } else {
            addLore(lore, "more_inf");
        }
        addLore(lore, "teh_lvl", level);
    }
}