package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;
import plus.common.spship.item.ItemRegenShield;


public class SpRegenShield implements ITickModule{
    public final int energyCast;
    public final int regenAmount;

    public static final class Builder{
        private int count = 0, energyCast = 0;
        private float regenAmount = 0;

        public void feed(ItemStack item){
            float pow = ItemRegenShield.getPower(item);
            regenAmount += pow;
            energyCast += ItemRegenShield.calcAccEnergyCast(pow);
            ++count;
        }


        public boolean canBuild(){
            return count>0;
        }


        public SpRegenShield complete(float add){
            float d;
            switch (count){
                case 1:
                    d = 1f;break;
                case 2:
                    d = 0.75f;break;
                case 3:
                    d = 0.6f;break;
                default:
                    d = 0.5f;break;
            }
            return new SpRegenShield(energyCast, (int) (add + (regenAmount*d)));
        }
    }


    public SpRegenShield(int energyCast, int regenAmount) {
        this.energyCast = energyCast;
        this.regenAmount = regenAmount;
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
                }
            }
        }
    }
}