package plus.valcirias.classes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import plus.valcirias.BaseValkiria;


public class ValkiriaSword extends BaseValkiria {

    private static final double chargeBoost = getClassVar("charge_boost", 1.75);
    private static final double chargeRange = getClassVar("charge_range", 3.2);
    private static final double heightBoost = getClassVar("height_boost", 3.75);
    private static final double heightRange = getClassVar("height_range", 4.2);

    private int countBuff = 0;

    public ValkiriaSword(){
        super(1);
    }


    @Override
    public double NormalAttack(double damage){
        double finalDamage = damage;
        if(countBuff>0){
            finalDamage = damage*chargeBoost;
            countBuff -= 1;
        }
        return finalDamage;
    }


    @Override
    public void setCombatMode(boolean NewMode){
        if(NewMode) countBuff = 0;
        CombatMode = NewMode;
    }


    @Override
    public void ChargeAttack(double Damage, double charge){
        if(charge>0.3) {

            double finalCharge;
            if(countBuff>3){
                finalCharge = (charge+0.7) * 1.3;
                countBuff-=3;
            } else {
                finalCharge = charge+0.7;
            }
            finalCharge = Damage * finalCharge;
            for (Entity target : player.getNearbyEntities(chargeRange, chargeRange, chargeRange)) {
                if (target instanceof LivingEntity tg) {
                    tg.damage(finalCharge, player);
                    tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
                }
            }
        }
    }


    @Override
    public void NormalSkill(double Damage) {
        countBuff+=5;
        Location location = player.getLocation();
        location.setY(location.getY()+1.7);
        if(countBuff>7){
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 45);
            if(countBuff==12){
                player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.5));
                countBuff=2;
            } else countBuff=7;
        } else {
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 20);
        }
    }


    @Override
    public void HeightSkill(final double Damage) {
        int countParticles = (int) (Damage/2);
        for(Entity target:player.getNearbyEntities(heightRange, heightRange, heightRange)){
            if(target instanceof LivingEntity t){
                t.damage(Damage*heightBoost, player);
                target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation(), countParticles);
            }
        }
    }
}