package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.Strings;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.utls.MathUtils;
import java.util.List;

import static plus.utls.MathUtils.calcPowerComponent;


public class ItemCapacitor extends TehBasicItem {
    public final int level;
    public final int basicCap;

    protected ItemCapacitor(String name, int lvl, int basicCap) {
        super(name);
        setMaxDamage(100);
        setMaxStackSize(1);
        this.level = lvl;
        this.basicCap = basicCap;
    }


    public int getMaxDur(){
        return 2000;
    }


    public static boolean isCapacitor(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemCapacitor;
    }


    @Override
    public ItemStack create(float damage, float power, int tehProcess) {
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(Strings.TEH_PROC, tehProcess);

        int cap = (int) ((calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProcess)/2)*basicCap);
        int usages = 100*level + (int) (getMaxDur()*(1-damage));

        tag.setInteger(Strings.BASIC_CAP, cap);
        tag.setInteger(Strings.MAX_USAGE, usages*cap);
        tag.setInteger(Strings.MAX_ST_USAGE, usages*cap);
        tag.setInteger(Strings.CUR_CAP, 0);

        stack.setTagCompound(tag);
        stack.setItemDamage(100);

        return stack;
    }


    @SideOnly(Side.CLIENT)
    private float calcPer(float cur, float max){
        return (   (int)((100-((100*cur)/max))*100)   )/100f;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, World world, List<String> lore,
                               ITooltipFlag flagIn) {
        if(isMoreInfo()){
            NBTTagCompound tags = item.getTagCompound();
            if(tags==null)return;
            int mxUsg = tags.getInteger(Strings.MAX_ST_USAGE);
            int bsCap = tags.getInteger(Strings.BASIC_CAP);
            addLore(lore, "teh_prc", tags.getInteger(Strings.TEH_PROC));
            addLore(lore, "teh_dur_per", calcPer(
                    tags.getInteger(Strings.MAX_USAGE), mxUsg)
            );
            addLore(lore, "teh_cur_cap", tags.getInteger(Strings.CUR_CAP),bsCap);
            addLore(lore, "teh_dur_max", mxUsg/bsCap);
        } else {
            addLore(lore, "more_inf");
        }
        addLore(lore, "teh_lvl", level);
    }


    public static class Provider extends ProviderBase{
        private ItemStack item = ItemStack.EMPTY;

        public ItemStack getItem() {
            return item;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }
    }


    public static class LinkedProvider extends ProviderBase{
        private final IItemLink link;

        public LinkedProvider(IItemLink link) {
            this.link = link;
        }

        @Override
        public ItemStack getItem() {
            return link.getItem();
        }

        @Override
        public void setItem(ItemStack item) {
            link.setItem(item);
        }
    }


    public static abstract class ProviderBase implements IEnergyFolder, IItemLink {
        @Override
        public boolean takeEnergy(int amount) {
            int cur = getCurEnergy();
            if(cur>0 && cur>=amount){
                NBTTagCompound tag = getItem().getTagCompound();
                int usages = tag.getInteger(Strings.MAX_USAGE) - amount;
                if(usages<0){
                    setItem(ItemStack.EMPTY);
                    return false;
                } else {
                    tag.setInteger(Strings.MAX_USAGE, usages);
                    usages = cur-amount;
                    setEnergy(usages);
                    updateEnergy(usages, getMaxEnergy());
                    return true;
                }
            } else {
                return false;
            }
        }


        @Override
        public void damage(int amount) {
            NBTTagCompound tag = getItem().getTagCompound();
            int usages = tag.getInteger(Strings.MAX_USAGE) - amount;
            if(usages<0) setItem(ItemStack.EMPTY);
            else tag.setInteger(Strings.MAX_USAGE, usages);
        }


        protected void setEnergy(int amount){
            getItem().getTagCompound().setInteger(Strings.CUR_CAP, amount);
        }


        protected void updateEnergy(float cur, float max){
            getItem().setItemDamage(100-(int) ((99*cur)/max));
        }


        @Override
        public boolean putEnergy(int amount) {
            int max = getMaxEnergy();
            int cur = getCurEnergy();
            if(cur<max){
                int cAmm = cur+amount;
                if(cAmm>=max){
                    setEnergy(max);
                    getItem().setItemDamage(1);
                } else {
                    setEnergy(cAmm);
                    updateEnergy(cAmm, max);
                }
                return true;
            }
            return false;
        }


        @Override
        public int getCurEnergy() {
            NBTTagCompound tag = getItem().getTagCompound();
            if(tag != null){
                return tag.getInteger(Strings.CUR_CAP);
            }
            return 0;
        }


        @Override
        public int getMaxEnergy() {
            NBTTagCompound tag = getItem().getTagCompound();
            if(tag != null){
                return tag.getInteger(Strings.BASIC_CAP);
            }
            return 0;
        }
    }
}