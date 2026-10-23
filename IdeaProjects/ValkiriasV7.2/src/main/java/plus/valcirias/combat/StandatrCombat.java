package plus.valcirias.combat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import plus.Main;
import plus.util.ConfigHelper;
import plus.valcirias.Valkiria;


public class StandatrCombat implements Listener {


    @EventHandler
    public void inventory(InventoryClickEvent e){
        Valkiria valkiria = Main.floader.getValkiria(e.getWhoClicked().getName());
        if(valkiria.CombatMode()){
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void hold(PlayerItemHeldEvent e){
        Valkiria valkiria = Main.floader.getValkiria(e.getPlayer());
        if(valkiria.CombatMode()){
            int swapTo = e.getNewSlot();
            e.setCancelled(true);

            if(swapTo == 7){
                if(e.getPlayer().getCooldown(Material.ARROW) == 0){
                    double newHealth = e.getPlayer().getHealth() - valkiria.getNormalSkillPrice();
                    if(newHealth<=0) {
                        if (valkiria.isCriticalEnergy()) {
                            valkiria.useNormalSkill();
                            valkiria.damage(valkiria.getMaxShield()+1);
                            e.getPlayer().setCooldown(Material.ARROW, valkiria.getNormalCD());
                            valkiria.setCriticalEnergy(false);
                        } else {
                            valkiria.setCriticalEnergy(true);
                            e.getPlayer().spigot().sendMessage(ConfigHelper.messageNextUsageKill);
                        }
                    } else {
                        valkiria.useNormalSkill();
                        e.getPlayer().setCooldown(Material.ARROW, valkiria.getNormalCD());
                        e.getPlayer().setHealth(newHealth);
                        valkiria.setCriticalEnergy(false);
                    }
                }
            }
            if(swapTo == 8){
                if(e.getPlayer().getCooldown(Material.ENDER_PEARL) == 0){
                    double newHealth = e.getPlayer().getHealth() - valkiria.getHeightSkillPrice();
                    if(newHealth<=0){
                        if(valkiria.isCriticalEnergy()){
                            valkiria.useHeightSkill();
                            valkiria.damage(valkiria.getMaxShield()+1);
                            e.getPlayer().setCooldown(Material.ENDER_PEARL, valkiria.getHightCD());
                            valkiria.setCriticalEnergy(false);
                        } else {
                            valkiria.setCriticalEnergy(true);
                            e.getPlayer().spigot().sendMessage(ConfigHelper.messageNextUsageKill);
                        }
                    } else {
                        valkiria.useHeightSkill();
                        e.getPlayer().setHealth(newHealth);
                        e.getPlayer().setCooldown(Material.ENDER_PEARL, valkiria.getHightCD());
                        valkiria.setCriticalEnergy(false);
                    }
                }
            }

        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void ChargeAttack(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
            Valkiria valkiria = Main.floader.getValkiria(e.getEntity().getName());
            if(valkiria.CombatMode())
                valkiria.useChargeAttack(e.getForce());
            else valkiria.toggleCombatModeWithCheck();
        }
    }


    @EventHandler
    public void onCombatDrop(PlayerDropItemEvent e){
        if(Main.floader.getValkiria(e.getPlayer()).CombatMode()){
            e.setCancelled(true);
            e.getPlayer().spigot().sendMessage(ConfigHelper.messageDisableCombat);
        }
    }


    @EventHandler
    public void onDrop(EntityDeathEvent e){
        e.setDroppedExp(0);
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        Main.floader.getValkiria(e.getPlayer()).UpdateShield();
    }


    @EventHandler
    public void onRegen(EntityRegainHealthEvent e){
        if(e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED){
            Valkiria valkiria = Main.floader.getValkiria(e.getEntity().getName());
            valkiria.onRegen(e);
        }
    }
}