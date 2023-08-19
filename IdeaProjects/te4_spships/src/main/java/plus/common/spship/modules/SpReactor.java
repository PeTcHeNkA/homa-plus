package plus.common.spship.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import plus.common.Strings;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.common.mashine.item.ReactComponent;
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


    private SpReactor(IItemLink[] links, int coreLvl) {
        this.links = links;
        this.coreLvl = coreLvl;
        checkItems();
    }


    public static SpReactor build(SpShip spShip, EntityPlayer player, int coreLvl){
        IItemLink[] links = scanReact(player.inventory);
        if(links == null)return null;
        return new SpReactor(links, coreLvl);
    }


    private static IItemLink[] scanReact(IInventory inv){
        IItemLink[] links = new IItemLink[SLOTS.length];
        int count = 0;
        for(int slot:SLOTS){
            ItemStack item = inv.getStackInSlot(slot);
            if(ReactComponent.isReactNoCore(item)){
                links[count] = new FInvItemLink(inv, slot, item);
                ++count;
            }
        }
        if(count==0)return null;
        if(links.length != count){
            IItemLink[] links0 = new IItemLink[count];
            System.arraycopy(links, 0, links0, 0, count);
            return links0;
        }
        return links;
    }


    @Override
    public void onTick(SpShip sp) {
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
            int cur = sp.getFolder().getCurEnergy();
            int max = sp.getFolder().getMaxEnergy();
            int maxTake = max-cur;
            if(cur>0){
                if(maxTake>energy)maxTake = (int) energy;
                sp.getFolder().putEnergy(maxTake);
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
            genSpeed = 0;
        } else {
            genSpeed = coreLvl * summPow * 20;
            maxPower = genSpeed * 2;
        }
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