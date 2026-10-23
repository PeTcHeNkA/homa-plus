package com.github.magiccheese1.damageindicator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import plus.util.FastRandom;


import java.util.ArrayList;
import java.util.List;

public class DamageIndicator {


    public static void Summon(Player attacker, Location location1, double damage) throws InterruptedException {
        Location location;
        byte attempts = 0;
        do {

            ++attempts;
            location = location1.add(FastRandom.r.nextDouble(0.0D, 2.0D) - 1.0D, 1.0D, FastRandom.r.nextDouble(0.0D, 2.0D) - 1.0D);
            if (attempts > 7) {
                location = location1;
                break;
            }
        } while(!location.getBlock().isPassable());

        Object indicatorEntity = Main.pm.buildEntityArmorStand(location, String.format(Main.DAMAGE_FORMAT, damage));
        Object entitySpawnPacket = Main.pm.buildEntitySpawnPacket(indicatorEntity);
        Object entityMetadataPacket = Main.pm.buildEntityMetadataPacket(indicatorEntity, true);

        List<Player> targets = new ArrayList<>();
        if(Main.VIEW_DISTANCE>-1) {
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                if (recipient.getLocation().distance(location1) < Main.VIEW_DISTANCE) {
                    targets.add(recipient);
                }
            }
        }
        if(!targets.contains(attacker))targets.add(attacker);
        //targets.remove(attacker);
        for(Player p:targets){
            Main.pm.sendPacket(entitySpawnPacket, p);
            Main.pm.sendPacket(entityMetadataPacket, p);
        }


        Thread.sleep(1750);
        Object entityDestroyPacket = Main.pm.buildEntityDestroyPacket(indicatorEntity);

        for (Player recipient : targets) {
            Main.pm.sendPacket(entityDestroyPacket, recipient);
        }
    }
}
