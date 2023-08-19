package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import plus.common.spship.SpShip;


public interface ISpAccelerator {
    float onBoost(SpShip sp, EntityLivingBase entity, float curSpeed);
}