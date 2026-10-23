package plus.valcirias.classes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import plus.valcirias.BaseValkiria;
import plus.Main;


public class ValkiriaUeMia extends BaseValkiria {

    public ValkiriaUeMia(){
        super(2);
    }


    private int unlockSpeed = 0;
    @Override
    public void setCombatMode(boolean NewMode){
        if(NewMode) unlockSpeed = 0;
        CombatMode = NewMode;
    }


    @Override
    public double NormalAttack(double charge){
        return attack *0.75;
    }


    @Override
    public void ChargeAttack(double Damage, double charge){
        if(unlockSpeed>0){
            Shoot(Damage*(1.2+charge/2));
            unlockSpeed-=1;
        } else {
            if (charge > 0.32) {
                Shoot((charge + 0.2) * Damage);
            }
        }
    }


    private void Shoot(double damage){
        Snowball ball = player.launchProjectile(Snowball.class,
                player.getLocation().getDirection().normalize().multiply(2)
        );
        ball.setMetadata("Damage", new FixedMetadataValue(Main.instance, damage));
    }


    @Override
    public void NormalSkill(double Damage) {
        unlockSpeed+=8;
        Location location = player.getLocation();
        location.setY(location.getY()+1.7);
        if(unlockSpeed>12){
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 45);
            if(unlockSpeed>15){
                unlockSpeed-=8;

                for(Entity target:player.getNearbyEntities(2.8, 2.8, 2.8)){
                    if(target instanceof LivingEntity){
                        ((LivingEntity) target).damage(Damage*1.85, player);
                        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 15);
                    }
                }

                Vector vec = player.getLocation().getDirection().normalize();
                vec.setX(-vec.getX());
                vec.setY(-vec.getY());
                vec.setZ(-vec.getZ());
                player.setVelocity(vec.multiply(1.6));
            }
        } else {
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 20);
        }
    }


    @Override
    public void HeightSkill(double Damage) {
        unlockSpeed = 15;
        double dirY = -player.getLocation().getDirection().normalize().getY();

        for(Entity ent:player.getNearbyEntities(4.5, 4.5, 4.5)){
            if(ent instanceof LivingEntity target){
                Vector direction = target.getLocation().getDirection();
                direction.setX(-direction.getX());
                direction.setZ(-direction.getZ());
                direction.setY(dirY);
                target.setVelocity(direction.multiply(1.4));
                target.damage(Damage*3.5);
                target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation(), 22);
            }
        }
    }
}