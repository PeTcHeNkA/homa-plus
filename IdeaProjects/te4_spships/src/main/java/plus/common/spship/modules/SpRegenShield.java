package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;
import plus.common.spship.item.ItemRegenShield;

import static plus.common.spship.modules.SpAccelerator.*;


public class SpRegenShield implements ITickModule{
    public final int energyCast;
    public final int regenAmount;
    private final IItemLink[] links;

    public static final class Builder{
        private int count = 0, energyCast = 0;
        private float regenAmount = 0;
        private final IItemLink[] links;

        public Builder(){
            links = new IItemLink[SpShip.SLOTS.length];
        }


        public void feed(SpInvProxy proxy, int slot){
            links[count] = proxy.link(slot);
            float pow = ItemRegenShield.getPower(links[count].getItem());
            regenAmount += pow;
            energyCast += ItemRegenShield.calcAccEnergyCast(pow);
            ++count;
        }


        public boolean canBuild(){
            return count>0;
        }


        public SpRegenShield complete(float add){
            return new SpRegenShield(energyCast, (int) (add + (regenAmount*coefCount(count))), trimTo(links, count));
        }
    }


    public SpRegenShield(int energyCast, int regenAmount, IItemLink[] links) {
        this.energyCast = energyCast;
        this.regenAmount = regenAmount;
        this.links = links;
    }


    @Override
    public void onTick(SpShip sp) {
        PlayerSpship sp0 = (PlayerSpship) sp.owner;
        float ns = sp0.getSpShield();
        if(ns<sp.maxShield) {
            IEnergyFolder ps = sp.getPowerSystem();
            if (((float) ps.getMaxEnergy() / ps.getCurEnergy()) > 0.2) {

                if (sp.takeEnergy(energyCast)) {

                    ns += regenAmount;
                    if (ns > sp.maxShield) ns = sp.maxShield;
                    sp0.setSpShield(ns);

                    damage(links, sp, 1);
                }
            }
        }
    }
}