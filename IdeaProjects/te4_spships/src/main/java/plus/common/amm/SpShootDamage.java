package plus.common.amm;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import plus.common.spship.modules.SpGun;


public class SpShootDamage extends EntityDamageSource {
    private final SpGun.AmmType type;
    public SpShootDamage(Entity damageSourceEntityIn, SpGun.AmmType type) {
        super("sp_s", damageSourceEntityIn);
        this.type = type;
    }


    public SpGun.AmmType getType(){
        return type;
    }
}