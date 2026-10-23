package plus.valcirias.classes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import plus.valcirias.BaseValkiria;

public class ValkiriaBraid extends BaseValkiria {

    private static final double chargeBoost = getClassVar("charge_boost", 1.35);
    private static final double boostedNormalAtk = getClassVar("boosted_normal_atk", 1.55);
    private static final double boostedNormalAtkRange = getClassVar("boosted_normal_atk_range", 1.55);
    private static final double chargeBasicRange = getClassVar("charge_basic_range", 2.8);
    private static final double chargeLvlRange = getClassVar("charge_lvl_range", 1.2);
    private static final double heightBoost = getClassVar("height_boost", 3.25);
    private static final double heightRange = getClassVar("height_range", 2.4);
    private static final double heightHeal = getClassVar("height_heal", 0.15);

    private int countFullAttack = 0;

    public ValkiriaBraid() {
        super(3);
    }

    @Override
    public void setCombatMode(boolean NewMode){
        if(NewMode) countFullAttack = 0;
        CombatMode = NewMode;
    }

    @Override
    public double useNormalAttack(EntityDamageByEntityEvent e){
        float c = player.getAttackCooldown();
        double dmg = (r.nextDouble()< criticalRate ? attack *criticalPower: attack);
        if(c>0.95){
            if(countFullAttack<3){
                countFullAttack+=1;
                return BoostedNormalAttack(e, dmg*boostedNormalAtk);
            }
        } else {
            countFullAttack = 0;
        }
        return NormalAttack(c*c*c*dmg);

    }

    @Override
    public double NormalAttack(double damage){
        return damage;
    }


    private double BoostedNormalAttack(EntityDamageByEntityEvent e, double dmg) {
        state = VState.CAST_SKULL;
        for (Entity target : e.getEntity().getNearbyEntities(boostedNormalAtkRange,boostedNormalAtkRange,boostedNormalAtkRange)) {
            if (target instanceof LivingEntity tg) {
                tg.damage(dmg, player);
                tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
            }
        }
        state = VState.NONE;
        return dmg;
    }


    @Override
    public void ChargeAttack(double Damage, double charge){
        if(charge>0.3) {
            double finalRange = countFullAttack*chargeLvlRange+chargeBasicRange;
            countFullAttack = 0;
            double finalCharge = (1+charge)*chargeBoost;
            for (Entity target : player.getNearbyEntities(finalRange,finalRange,finalRange)) {
                if (target instanceof LivingEntity tg) {
                    tg.damage(finalCharge, player);
                    tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
                }
            }
        }
    }


    @Override
    public void NormalSkill(double Damage) {
        Location location = player.getLocation();
        location.setY(location.getY()+1.7);
        if(countFullAttack > 2){
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 45);
            player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.5));
        } else {
            countFullAttack = 3;
            ChargeAttack(Damage, 1.5);
        }
        countFullAttack = 3;
    }


    @Override
    public void HeightSkill(final double Damage) {
        shield = shield + maxShield*heightHeal;
        if(shield>maxShield){
            shield = maxShield;
        }
        int countParticles = (int) (Damage/2);
        for(Entity target:player.getNearbyEntities(heightRange, heightRange, heightRange)){
            if(target instanceof LivingEntity t){
                t.damage(Damage*heightBoost, player);
                target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation(), countParticles);
            }
        }
    }
}