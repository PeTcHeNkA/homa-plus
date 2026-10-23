package plus.valcirias.combat;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.MetadataValue;
import plus.region.TerrianChecker;
import plus.valcirias.BaseValkiria;
import plus.valcirias.data.UserFloader;
import plus.valcirias.Valkiria;
import java.util.List;


public class ValkiriasCombat implements Listener {

    protected final UserFloader folder;
    protected final TerrianChecker checker;

    public ValkiriasCombat(UserFloader folder, TerrianChecker checker) {
        this.folder = folder;
        this.checker = checker;
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(EntityDamageByEntityEvent e){
//        if(e.getEntity() instanceof ItemFrame){
//            e.setCancelled(true);
//            return;
//        }
        //if(e.isCancelled())return;
        if(e.getDamager() instanceof Player){
            Valkiria valkiria = folder.getValkiria(e.getDamager().getName());
            if(valkiria.CombatMode()){
                if(valkiria.getState() != BaseValkiria.VState.CAST_SKULL) {
                    e.setDamage(valkiria.useNormalAttack(e));
                }
            } else {
                valkiria.toggleCombatModeWithCheck();
                e.setCancelled(true);
                return;
            }
        } else if(e.getDamager() instanceof Projectile) {
            List<MetadataValue> meta1 = e.getDamager().getMetadata("Damage");
            List<MetadataValue> meta2 = e.getDamager().getMetadata("Damager");
            if(!meta1.isEmpty()) {
                if (e.getEntity() instanceof LivingEntity entity) {
                    entity.setNoDamageTicks(0);
                    if(!meta2.isEmpty()) {
                        e.setCancelled(true);
                        entity.damage((Double) meta1.get(0).value(), (Player) meta2.get(0).value());
                        return;
                    } else {
                        e.setDamage((Double) meta1.get(0).value());
                    }
                } else {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        //if(!e.isCancelled()) {
            Thread t = new Thread(() -> {
                if (e.getDamager() instanceof Player p) {
                    EffectDamage.summon(p, e.getEntity().getLocation(), e.getDamage());
                } else if (e.getDamager() instanceof Projectile ball) {
                    if (ball.getShooter() instanceof Player p) {
                        EffectDamage.summon(p, e.getEntity().getLocation(), e.getDamage());
                    }
                }
            });
            t.start();
        //}
    }


    @EventHandler
    public void damageTrigger(EntityDamageEvent e){
        if(e.getEntity() instanceof Player player){
            Valkiria valkiria = folder.getValkiria(e.getEntity().getName());

            if(e instanceof EntityDamageByEntityEvent){
                Entity damager = ((EntityDamageByEntityEvent) e).getDamager();
                if(damager instanceof Player dm){
                    if(!checker.inPvpTerritory(player) || !checker.inPvpTerritory(dm) ){
                        e.setCancelled(true);
                        return;
                    }
                    if(valkiria.getParty() != null && valkiria.getParty().isParty(dm)){
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            valkiria.damage(e.getDamage());
        }
    }
}
