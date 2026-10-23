package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.common.mashine.item.ReactComponent;
import plus.common.spship.BaseSystem;
import plus.common.spship.SpShip;
import static plus.common.spship.SpShip.SLOTS;


public class SpReactor implements IEnergyFolder, ITickModule {
    private int boostFlag = 0;
    private float maxPower;
    private float genSpeed;
    private float energy;
    private byte ignoreTicks = 0;
    private final IItemLink[] links;
    private final int coreLvl;
    private final float baseSpeed;


    private SpReactor(IItemLink[] links, int coreLvl, float baseSpeed) {
        this.links = links;
        this.coreLvl = coreLvl;
        this.baseSpeed = baseSpeed;
        checkItems();
    }


    public static final class Builder{
        private IItemLink[] links = new IItemLink[SLOTS.length];
        private int count = 0;

        public void feed(SpInvProxy proxy, int slot){
            if(ReactComponent.isReactNoCore(proxy.getItem(slot))){
                links[count] = proxy.link(slot);
                ++count;
            }
        }


        public boolean canBuild(){
            return count>0;
        }


        public SpReactor complete(int coreLvl, float baseSpeed){
            if(count==0)links = new IItemLink[0];
            else if(links.length != count){
                IItemLink[] links0 = new IItemLink[count];
                System.arraycopy(links, 0, links0, 0, count);
                links = links0;
            }
            return new SpReactor(links, coreLvl, baseSpeed);
        }
    }


    @Override
    public void onTick(BaseSystem sp) {
        if (ignoreTicks++ == 21) {
            if (energy < maxPower && genSpeed > 0) {
                for (IItemLink link : links) {
                    ItemStack rci = link.getItem();
                    rci.setItemDamage(rci.getItemDamage() + boostFlag);
                    if (rci.getItemDamage() > rci.getMaxDamage()) {
                        link.setItem(ItemStack.EMPTY);
                        checkItems();
                    }
                }
                energy += genSpeed;
            }
            int cur = sp.getPowerSystem().getCurEnergy();
            int max = sp.getPowerSystem().getMaxEnergy();
            int maxTake = max-cur;
            if(cur<max){
                if(maxTake>energy)maxTake = (int) energy;
                sp.getPowerSystem().putEnergy(maxTake);
                energy -= maxTake;
            }
            ignoreTicks = 0;
        }
    }


    private void checkItems() {
        boostFlag = 1;
        float summPow = 0;
        for (IItemLink link : links) {
            ItemStack rci = link.getItem();
            NBTTagCompound tags = rci.getTagCompound();
            if (rci.isEmpty() || tags == null) continue;
            ReactComponent rc = (ReactComponent) rci.getItem();

            if (rc.getType() == ReactComponent.TYPE.COMPONENT) {
                summPow = summPow + (tags.getFloat(Strings.POWER) + (rc.level / 2f));
            } else {
                ++boostFlag;
            }
        }
        if (summPow == 0) {
            genSpeed = baseSpeed;
        } else {
            genSpeed = baseSpeed + (coreLvl * summPow * 20);
            maxPower = genSpeed * 2;
        }
    }


    public float getGenSpeed() {
        return genSpeed;
    }


    @Override
    public boolean takeEnergy(int amount) {
        if (amount < energy) {
            energy -= amount;
            return true;
        }
        return false;
    }


    @Override
    public boolean putEnergy(int amount) {
        return false;
    }


    @Override
    public int getCurEnergy() {
        if (energy < maxPower) {
            return (int) energy;
        } else {
            return (int) maxPower;
        }
    }


    @Override
    public int getMaxEnergy() {
        return (int) maxPower;
    }
}