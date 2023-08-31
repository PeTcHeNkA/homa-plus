package plus.common.amm;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;


public class SpShootDamage extends EntityDamageSource {
    public final float physDmg, enrgDmg;
    public SpShootDamage(Entity damageSourceEntityIn, float physDmg, float enrgDmg) {
        super("sp_s", damageSourceEntityIn);
        this.physDmg = physDmg;
        this.enrgDmg = enrgDmg;
    }
}