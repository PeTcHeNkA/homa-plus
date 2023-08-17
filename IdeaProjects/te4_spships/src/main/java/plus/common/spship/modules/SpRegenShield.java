package plus.common.spship.modules;

import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;


public class SpRegenShield implements ITickModule{

    private final float reqEnergy;
    private final float energyCast;
    private final float regenAmount;


    public SpRegenShield(float reqEnergy, float energyCast, float regenAmount) {
        this.reqEnergy = reqEnergy;
        this.energyCast = energyCast;
        this.regenAmount = regenAmount;
    }


    @Override
    public void onTick(SpShip sp) {
        float energyPerc = calcEnrgPercent(sp);
        if(energyPerc>reqEnergy){
            if(sp.takeEnergy(energyCast)){
                PlayerSpship sp0 = (PlayerSpship) sp.owner;
                float ns = sp0.getSpShield() + reqEnergy;
                if(ns<sp.maxShield)ns = sp.maxShield;
                sp0.setSpShield(ns);
            }
        }
    }


    private static float calcEnrgPercent(SpShip sp){
        return sp.energy/sp.maxEnergy;
    }
}
