package plus.common.spship.modules;

import net.minecraft.util.DamageSource;
import plus.common.amm.SpShootDamage;
import plus.common.spship.SpShip;


public class SpProtector implements ISpProtector {
    private final float physicDmgCoef;
    private final int physicEnrgCoef;
    private final float energyDmgCoef;
    private final int energyEnrgCoef;


    public SpProtector(float physicDmgCoef, int physicEnrgCoef, float energyDmgCoef, int energyEnrgCoef) {
        this.physicDmgCoef = physicDmgCoef;
        this.physicEnrgCoef = physicEnrgCoef;
        this.energyDmgCoef = energyDmgCoef;
        this.energyEnrgCoef = energyEnrgCoef;
    }


    @Override
    public final float onDamage(SpShip sp, DamageSource src, float amount){
        if(src instanceof SpShootDamage){
            return onDamage(sp, (SpShootDamage) src, amount);
        } else {
            return 0;
        }
    }


    public float onDamage(SpShip sp, SpShootDamage src, float damage){
        switch (src.getType()){
            case PHYSIC:
                if(sp.takeEnergy(physicEnrgCoef)){
                    return damage*physicDmgCoef;
                }
            case ENERGY:
                if(sp.takeEnergy(energyEnrgCoef)){
                    return damage*energyDmgCoef;
                }
            case COMBINED:
                if(sp.takeEnergy((energyEnrgCoef + physicEnrgCoef)>>1)){
                    return damage*((physicDmgCoef+energyDmgCoef)/2f);
                }
        }
        return damage;
    }
}