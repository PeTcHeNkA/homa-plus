package plus.main;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plus.main.spship.EEntitySkeleton;

public class Events implements Listener{
    @EventHandler
    public void onMove(EntityMoveEvent e){
        if(e.getEntity() instanceof EEntitySkeleton ent){
            e.setCancelled(true);
            System.out.println("NEN");
        }
    }
}
