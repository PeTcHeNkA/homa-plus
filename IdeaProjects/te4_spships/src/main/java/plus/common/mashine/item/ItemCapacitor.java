package plus.common.mashine.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.mashine.inv.IEnergyFolder;
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


    @Override
    public ItemStack create(float damage, float power, int tehProcess) {
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("teh-prc", tehProcess);

        int cap = (int) ((calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProcess)/2)*basicCap);
        int usages = 100*level + (int) (getMaxDur()*(1-damage));

        tag.setInteger("basic-cap", cap);
        tag.setInteger("max-usage", usages*cap);
        tag.setInteger("max-st-usg", usages*cap);
        tag.setInteger("cur-cap", 0);

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
            int mxUsg = tags.getInteger("max-st-usg");
            int bsCap = tags.getInteger("basic-cap");
            addLore(lore, "teh_prc", tags.getInteger("teh-prc"));
            addLore(lore, "teh_dur_per", calcPer(
                    tags.getInteger("max-usage"), mxUsg)
            );
            addLore(lore, "teh_cur_cap", tags.getInteger("cur-cap"),bsCap);
            addLore(lore, "teh_dur_max", mxUsg/bsCap);
        } else {
            addLore(lore, "more_inf");
        }
        addLore(lore, "teh_lvl", level);
    }


    public static class Provider implements IEnergyFolder{
        private ItemStack item = ItemStack.EMPTY;
        private NBTTagCompound tag = null;

        public ItemStack getItem() {
            return item;
        }

        public void setItem(ItemStack item) {
            this.item = item;
            tag = item.getTagCompound();
        }


        @Override
        public boolean takeEnergy(int amount) {
            int cur = getCurEnergy();
            if(cur>0 && cur>=amount){
                int usages = tag.getInteger("max-usage") - amount;
                if(usages<0){
                    setItem(ItemStack.EMPTY);
                    return false;
                } else {
                    tag.setInteger("max-usage", usages);
                    usages = cur-amount;
                    setEnergy(usages);
                    updateEnergy(usages, getMaxEnergy());
                    return true;
                }
            } else {
                return false;
            }
        }


        protected void setEnergy(int amount){
            tag.setInteger("cur-cap", amount);
        }


        protected void updateEnergy(float cur, float max){
            item.setItemDamage(100-(int) ((99*cur)/max));
        }


        @Override
        public boolean putEnergy(int amount) {
            int max = getMaxEnergy();
            int cur = getCurEnergy();
            if(cur<max){
                int cAmm = cur+amount;
                if(cAmm>=max){
                    setEnergy(max);
                    item.setItemDamage(1);
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
            if(tag != null){
                return tag.getInteger("cur-cap");
            }
            return 0;
        }


        @Override
        public int getMaxEnergy() {
            if(tag != null){
                return tag.getInteger("basic-cap");
            }
            return 0;
        }
    }
}
