package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import plus.common.spship.SpShip;


public class SpAccelerator implements ISpAccelerator{
    private final float add;
    private final int energyCast;
    private final float maxBoost;

    public SpAccelerator(float add, int energyCast, float maxBoost) {
        this.add = add;
        this.energyCast = energyCast;
        this.maxBoost = maxBoost;
    }


    @Override
    public float onBoost(SpShip sp, EntityLivingBase entity, float curSpeed) {
        if(sp.takeEnergy(energyCast))
            if ((curSpeed += add) > maxBoost) return maxBoost;

        else if ((curSpeed -= add) < 1)return 1;

        return curSpeed;
    }
}