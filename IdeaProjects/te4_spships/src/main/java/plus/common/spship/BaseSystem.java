package plus.common.spship;

import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.common.spship.modules.ITickModule;
import plus.common.spship.modules.SpProtector;


public abstract class BaseSystem {
    public IEnergyFolder powerSystem;
    public SpProtector protectGen = new SpProtector(0,0,0,0, new IItemLink[0]);
    public ITickModule[] tickModules;

    public void onTick(){
        for(ITickModule module: tickModules){
            module.onTick(this);
        }
    }


    public void damageLinkedItems(IItemLink[] links, int dmg){
        int fdmg;
        for (IItemLink link : links) {
            ItemStack item = link.getItem();
            fdmg = item.getItemDamage() + dmg;
            if (fdmg > item.getMaxDamage()) {
                link.setItem(ItemStack.EMPTY);
                calcParams();
            } else {
                item.setItemDamage(fdmg);
            }
        }
    }


    public boolean onDamage(DamageSource src, float amount){
        float shield = getShield();
        shield = shield - protectGen.onDamage(this, src, amount);
        if(shield <= 0){
            setShield(0);
            return false;
        } else {
            setShield(shield);
        }
        return true;
    }


    public void calcParams(){}


    public abstract float getMaxShield();


    public abstract float getShield();


    public abstract void setShield(float shield);


    public IEnergyFolder getPowerSystem(){
        return powerSystem;
    }


    public boolean takeEnergy(int amount){
        return powerSystem.takeEnergy(amount);
    }
}