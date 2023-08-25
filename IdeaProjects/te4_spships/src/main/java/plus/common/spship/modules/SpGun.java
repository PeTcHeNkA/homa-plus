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

    private final AmmType type;
    private final int modelId;
    private final float power;
    private final int energyCast;
    private final int cd;


    public SpGun(AmmType type, float power, float damage, float energyCastCoef, int modelId, int cd) {
        this.modelId = modelId;
        this.type = type;
        this.power = power;
        this.cd = cd;
        this.energyCast = (int) (power*damage*energyCastCoef);
    }


    @Override
    public void shoot(SpShip sp, EntityLivingBase shooter, float yaw, float pith) {
        if(sp.takeEnergy(energyCast)) {
            shooter.world.spawnEntity(new SpBullet(shooter.world, modelId, type, calkDmg(sp)).shoot(
                    shooter, yaw, pith, power, 0
            ));
        }
    }


    public int getCoolDown() {
        return cd;
    }


    private float calkDmg(SpShip sp){
        if(r.nextFloat() < sp.critRate){
            return sp.damage;
        } else {
            return sp.damage * sp.critDmg;
        }
    }
}