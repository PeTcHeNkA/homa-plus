package plus.valcirias.classes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.valcirias.BaseValkiria;
import plus.Main;
import java.util.ArrayList;
import java.util.List;


public class ValkiriaKeQuing extends BaseValkiria {

    private final int cdMarker;
    private final int cdTeleport;
    private final int priceMarker;
    private final int priceTeleport;
    private Location trigger = null;

    public ValkiriaKeQuing(){
        super(1);
        cdMarker = (NormalCD/4)*3;
        cdTeleport = NormalCD/4;
        priceMarker = (int) ((NormalPrice/4)*3);
        priceTeleport = (int) NormalPrice/4;
    }


    @Override
    public double getNormalSkillPrice(){
        if(trigger==null)return priceMarker;
        else return priceTeleport;
    }


    @Override
    public int getNormalCD() {
        if(trigger==null)return cdMarker;
        else return cdTeleport;
    }


    @Override
    public void setCombatMode(boolean NewMode){
        if(NewMode) trigger = null;
        CombatMode = NewMode;
    }


    @Override
    public void ChargeAttack(double Damage, double charge){
        if(charge>0.3) {
            charge+=0.7;

            for (Entity target : player.getNearbyEntities(3.2, 3.2, 3.2)) {
                if (target instanceof LivingEntity tg) {
                    tg.damage(Damage * charge, player);
                    tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
                }
            }


            if(trigger != null){
                charge = (charge+0.7) * 1.3;

                for (Entity target : trigger.getNearbyEntities(2.8, 2.8, 2.8)) {
                    if (target instanceof LivingEntity tg) {
                        tg.damage(Damage * charge, player);
                        tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
                    }
                }
            }
        }
    }


    @Override
    public void NormalSkill(double Damage) {
        if(trigger==null) {
            //player.getTargetBlock(null, 7).getLocation().getDirection();
            //BlockIterator itr = new BlockIterator(player.getLocation(), 5.7);
//            Block b = itr.next();
//
//            while (itr.hasNext()) {
//                if (b.getType() != Material.AIR) break;
//                b = itr.next();
//            }
            Location l = player.getLocation();
            System.out.println(l);
            l.setY(l.getY()+1);
            l.add(l.toVector().multiply(5.55));
            trigger = l;
            System.out.println(l);

            LivingEntity[] e = trigger.getNearbyLivingEntities(1.9, 2.1, 1.9).toArray(new LivingEntity[0]);
            if(e.length>0) {
                trigger = e[0].getLocation();
                for (LivingEntity target : e) {
                    target.damage(Damage * 2.5, player);
                    target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation(), 10);
                }
            } else {
                World w = trigger.getWorld();
                double distance = 4.15f;
                System.out.println("---");
                while (distance>0){
                    l = player.getLocation();
                    l.setY(l.getY()+1);
                    l.add(l.toVector().multiply(distance));
                    if(w.getBlockAt(l).getType()==Material.AIR){
                        trigger = l;
                        break;
                    }
                    distance = distance - 1.4;
                }
                if(distance<=0) trigger = player.getLocation();
            }
        } else {
            player.teleport(trigger);
            for (Entity target : trigger.getNearbyEntities(3.1, 3.1, 3.1)) {
                if (target instanceof LivingEntity tg) {
                    tg.damage(Damage * 2, player);
                    tg.getWorld().spawnParticle(Particle.CRIT_MAGIC, tg.getLocation(), 10);
                }
            }
            trigger = null;
        }
    }


    @Override
    public void HeightSkill(double Damage) {

        List<LivingEntity> e = new ArrayList<>();
        for(Entity en:player.getNearbyEntities(4, 4, 4)){
            if(en instanceof LivingEntity le)e.add(le);
        }
        if(e.isEmpty())return;

        double maxDamage = Damage*17;

        int index = 0;
        int delay = 4;

        while (maxDamage>0.01){
            if(index>=e.size())index=0;
            double dm = this.useNormalAttack(null);

            LivingEntity t = e.get(index);
            BukkitTask r = new BukkitRunnable() {
                @Override
                public void run() {
                    t.damage(dm, player);
                }
            }.runTaskLater(Main.instance, delay);
            maxDamage -= dm;
            delay +=4;
            index+=1;
        }
    }
}