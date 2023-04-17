package plus.common.spship.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.mashine.item.TehBasicItem;
import plus.common.spship.SPM;
import java.util.List;


public class ItemModule extends TehBasicItem {
    public static ItemModule SP_MODULE = new ItemModule("addon", 0);
    public static ItemModule SP_GUN    = new ItemModule("gun",   1);
    public static ItemModule SP_REGEN  = new ItemModule("shield",2);

    public final int type;

    private ItemModule(String name, int type){
        super("sp_module_"+name);
        this.type = type;
    }


    public boolean isSelfType(ItemStack item){
        return this == item.getItem();
    }


    public static boolean isSpModule(ItemStack item){
        return item.getItem().getClass() == ItemModule.class;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World worldIn, List<String> lore,
                               ITooltipFlag flagIn) {
        NBTTagCompound tags = item.getTagCompound();
        if(tags==null)return;

        addLore(lore, "slot", tags.getByte("s"));
        addLore(lore, "sp_dur", getMaxDamage()-item.getItemDamage(), getMaxDamage());

        int[] data = SPM.read(item);

        if(!SPM.isEmpty(data)) {
            if (isMoreInfo()) {
                addLore(lore, "teh_prc", tags.getInteger("teh-prc"));

                addInf(lore, data, "damage",        SPM.DAMAGE      );
                addInf(lore, data, "damage_pr",     SPM.DAMAGE_PR   );
                addInf(lore, data, "max_shield",    SPM.MAX_SHIELD  );
                addInf(lore, data, "max_shield_pr", SPM.DAMAGE_PR   );
                addInf(lore, data, "speed",         SPM.SPEED       );
                addInf(lore, data, "speed_pr",      SPM.SPEED_PR    );
                addInf(lore, data, "crit_rate",     SPM.CRIT_RATE   );
                addInf(lore, data, "crit_dmg",      SPM.CRIT_DMG    );
                addInf(lore, data, "regen_shield",  SPM.REGEN_SHIELD);
                addInf(lore, data, "mass",          SPM.MASS        );
            } else {
                addLore(lore, "more_inf");
            }
        }
    }


    public static ItemStack create(int... params){
        ItemStack item = new ItemStack(SP_MODULE);
        SPM.V spm = new SPM.V();
        spm.add(params);
        SPM.write(item, params);


        return item;
    }
}