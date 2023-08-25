package plus.common.spship.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import plus.common.mashine.item.TehBasicItem;
import plus.common.spship.SPM;
import java.util.List;


public class ItemModule extends TehBasicItem {
    public static final ItemModule SP_MODULE  = new ItemModule("addon",1,0, 0    );
    public static final ItemModule SP_GUN     = new ItemModule("gun",  1,1, 4800 );

    public final int type;
    public final int lvl;

    ItemModule(String name, int lvl, int type, int usages){
        super("sp_module_"+name);
        if(usages>0) setMaxDamage(usages);
        setMaxStackSize(1);
        this.lvl = lvl;
        this.type = type;
    }


    public boolean isSelfType(ItemStack item){
        return this == item.getItem();
    }


    public static boolean isSpModule(ItemStack item){
        return item.getItem().getClass() == ItemModule.class;
    }


    @SideOnly(Side.CLIENT)
    void addMoreInf(NBTTagCompound tags, List<String> lore){}


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World worldIn, List<String> lore,
                               ITooltipFlag flagIn) {
        NBTTagCompound tags = item.getTagCompound();
        if(tags!=null) {
            if (isMoreInfo()) {
                addMoreInf(tags, lore);
                addSPM(item, tags, lore);
                addLore(lore, "teh_lvl", lvl);
                if(getMaxDamage()>0)
                    addLore(lore, "sp_dur", getMaxDamage() - item.getItemDamage(), getMaxDamage());
            } else {
                addLore(lore, "module_help_a" + type);
                addLore(lore, "module_help_b" + type);
                addLore(lore, "teh_lvl", lvl);
                if(getMaxDamage()>0)
                    addLore(lore, "sp_dur", getMaxDamage() - item.getItemDamage(), getMaxDamage());
                addLore(lore, "more_inf");
            }
        }
    }


    public static boolean addSPM(ItemStack item, NBTTagCompound tags, List<String> lore){
        int[] data = SPM.read(item);
        if(!SPM.isEmpty(data)){
            addLore(lore, "teh_prc", tags.getInteger(Strings.TEH_PROC));
            addInf(lore, data, "damage", SPM.DAMAGE);
            addInf(lore, data, "damage_pr", SPM.DAMAGE_PR);
            addInf(lore, data, "max_shield", SPM.MAX_SHIELD);
            addInf(lore, data, "max_shield_pr", SPM.DAMAGE_PR);
            addInf(lore, data, "speed", SPM.SPEED);
            addInf(lore, data, "speed_pr", SPM.SPEED_PR);
            addInf(lore, data, "crit_rate", SPM.CRIT_RATE);
            addInf(lore, data, "crit_dmg", SPM.CRIT_DMG);
            addInf(lore, data, "regen_shield", SPM.REGEN_SHIELD);
            addInf(lore, data, "mass", SPM.MASS);
            return true;
        }
        return false;
    }


    public static ItemStack create(int... params){
        ItemStack item = new ItemStack(SP_MODULE);
        SPM.V spm = new SPM.V();
        spm.add(params);
        SPM.write(item, params);
        return item;
    }
}
