package plus.mythic;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobLootDropEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plus.Main;
import plus.valcirias.Valkiria;


public class MythicExpBridge implements Listener {


    @EventHandler
    public void onMythicExp(MythicMobLootDropEvent e){
        if(e.getKiller() instanceof Player){
            Valkiria valkiria = Main.floader.getValkiria(e.getKiller().getName());

            if(e.getExp()>0){
                valkiria.addExperience(e.getExp());
                //e.getKiller().sendMessage("You get " + e.getExp() + " exp!");
            }
            e.setExp(0);
        }
    }
}