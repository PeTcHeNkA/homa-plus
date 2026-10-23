package plus.valcirias.classes;

import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import plus.valcirias.BaseValkiria;
import plus.Main;


public class ValkiriaShooter extends BaseValkiria {

    private static final double additionalCharge = getClassVar("additional_charge", 0.25);
    private static final double heightBoost = getClassVar("height_boost", 3.75);
    private static final double heightRange = getClassVar("height_range", 4.2);


    public ValkiriaShooter(){
        super(2);
    }


    @Override
    public double NormalAttack(double charge){
        return attack *0.7;
    }


    @Override
    public void ChargeAttack(double Damage, double charge){
        if(charge>0.35){
            Shoot((charge+additionalCharge)*Damage);
        }
    }


    private void Shoot(double damage){
        Snowball ball = player.launchProjectile(Snowball.class,
                player.getLocation().getDirection().normalize().multiply(2)
                );
        ball.setMetadata("Damage", new FixedMetadataValue(Main.instance, damage));
        ball.setMetadata("Damager", new FixedMetadataValue(Main.instance, player));
    }


    @Override
    public void NormalSkill(double Damage) {

        for(Entity target:player.getNearbyEntities(3.2, 3.2, 3.2)){
            if(target instanceof LivingEntity){
                ((LivingEntity) target).damage(Damage*2.25, player);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 10);
            }
        }

        Vector vec = player.getLocation().getDirection().normalize();
        vec.setX(-vec.getX());
        vec.setY(-vec.getY());
        vec.setZ(-vec.getZ());
        player.setVelocity(vec.multiply(1.5));
    }


    @Override
    public void HeightSkill(double Damage) {
        double dirY = -player.getLocation().getDirection().normalize().getY();
        double finalDamage = Damage*heightBoost;

        for(Entity ent:player.getNearbyEntities(heightRange, heightRange, heightRange)){
            if(ent instanceof LivingEntity target){
                Vector direction = target.getLocation().getDirection();
                direction.setX(-direction.getX());
                direction.setZ(-direction.getZ());
                direction.setY(dirY);
                target.setVelocity(direction.multiply(1.2));
                target.damage(finalDamage, player);
                target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation(), 15);
            }
        }
    }
}