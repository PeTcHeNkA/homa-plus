package plus.common.spship.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import java.util.List;
import plus.common.mashine.item.QuantificatorItem;


public class ItemGun extends ItemModule{
    public static final ItemGun SP_GUN_L1 = new ItemGun(1, 1200 );
    public static final ItemGun SP_GUN_L2 = new ItemGun(2, 1500 );
    public static final ItemGun SP_GUN_L3 = new ItemGun(3, 1800 );
    public static final ItemGun SP_GUN_L4 = new ItemGun(4, 2100 );


    ItemGun(int lvl, int usages) {
        super("gun_l"+lvl, lvl, 1, usages);
    }


    public static final class Info {
        public int type, lvl, enrgCast;
        public float physDmg, enrgDmg;

        private void addTypeBoost(){
            switch (type){
                case 0:
                    physDmg *= 1.45f;
                    break;
                case 1:
                    enrgDmg *= 1.45f;
                    break;
                case 2:
                    physDmg *= 1.25f;
                    enrgDmg *= 1.25f;
                    break;
                case 3:
                    break;
            }
        }
    }


    @Override//NOT USE THIS
    public ItemStack create(int shift, float power, float damage, int tehProcess){
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort(Strings.TEH_PROC, (short) tehProcess);
        stack.setTagCompound(tag);
        if(damage<1) {
            int maxDmg = getMaxDamage();
            int dmg = (int) (maxDmg * damage);
            if(dmg>=maxDmg)dmg = maxDmg;
            stack.setItemDamage(dmg);
        }
        return stack;
    }


    public static void writeInfo(ItemStack item, Info gun){
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null){
            tag = new NBTTagCompound();
            item.setTagCompound(tag);
        }
        tag.setIntArray(Strings.GUN_DATA, new int[]{
                gun.type,
                (int) (gun.physDmg * 100),
                (int) (gun.enrgDmg * 100),
                gun.enrgCast
        });
    }


    public static Info scanInfo(ItemStack item){
        NBTTagCompound tag = item.getTagCompound();
        Info gun = new Info();
        gun.lvl = ((ItemGun)item.getItem()).lvl;
        int[] gunData = tag.getIntArray(Strings.GUN_DATA);

        gun.type = gunData[0];
        gun.physDmg = gunData[1]/100f;
        gun.enrgDmg = gunData[2]/100f;
        gun.enrgCast = gunData[3];

        gun.addTypeBoost();
        return gun;
    }


    @Override
    void addBaseInf(ItemStack item, List<String> lore) {
        addInfo(scanInfo(item), lore);
    }


    @Override
    public String getTranslationKey(ItemStack item) {
        NBTTagCompound tag = item.getTagCompound();
        int type0 = 0;
        if(tag!=null){
            int[] gunData = tag.getIntArray(Strings.GUN_DATA);
            if(gunData.length>0) type0 = gunData[0];
        }
        return super.getTranslationKey()+"."+QuantificatorItem.getType(type0);
    }


    private void addInfo(Info info, List<String> lore){
        addLore(lore, "phys_dmg", round(info.physDmg));
        addLore(lore, "enrg_dmg", round(info.enrgDmg));
    }


    @Override
    void addMoreInf(ItemStack item, List<String> lore) {
        Info info = scanInfo(item);
        String type = QuantificatorItem.getType(info.type);
        addLore(lore, "gun_type_"+type);
        addInfo(info, lore);
        addLore(lore, "energy_cast", info.enrgCast);
    }
}