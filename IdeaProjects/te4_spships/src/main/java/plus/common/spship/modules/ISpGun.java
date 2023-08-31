package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import plus.common.spship.SpShip;


public interface ISpGun {
    void shoot(SpShip sp, EntityLivingBase shooter, float yaw, float pith);
    ISpGun EMPTY = (sp, shooter, yaw, pith) -> {};
}