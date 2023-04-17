package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import plus.common.amm.SpBullet;
import plus.common.spship.SpShip;

import java.util.Random;


public class SpGun implements ISpGun{
    private static final Random r = new Random();
    private static int flag = 0;
    public enum AmmType {
        PHYSIC, ENERGY, COMBINED;
        public final int ID;
        AmmType() {
            ID = flag++;
        }
        public static AmmType valueOf(int i){
            return values()[i];
        }
    }

    public final AmmType type;
    public final int modelId;
    public final float power;
    public final float energyCast;

    public SpGun(AmmType type, float power, float damage, float energyCastCoef, int modelId) {
        this.modelId = modelId;

        this.type = type;
        this.power = power;
        this.energyCast = power*damage*energyCastCoef;
    }


    @Override
    public void shoot(SpShip sp, EntityLivingBase shooter, float yaw, float pith) {
        if(sp.takeEnergy(energyCast)) {
            shooter.world.spawnEntity(new SpBullet(shooter.world, modelId, type, calkDmg(sp)).shoot(
                    shooter, yaw, pith, power, 0
            ));
        }
    }


    private float calkDmg(SpShip sp){
        if(r.nextFloat() < sp.critRate){
            return sp.damage;
        } else {
            return sp.damage * sp.critDmg;
        }
    }
}