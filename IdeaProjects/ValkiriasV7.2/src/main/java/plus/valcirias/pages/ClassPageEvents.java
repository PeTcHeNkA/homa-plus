package plus.valcirias.pages;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import plus.Main;


public class ClassPageEvents implements Listener {

    private final ClassManager m;
    public ClassPageEvents(ClassManager classManager) {
        this.m = classManager;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getView().getTitle().contains("classpage:")){
            Player player = (Player) e.getWhoClicked();
            e.setCancelled(true);
            int sl = e.getSlot();
            ClassSet set = m.getClassSet(e.getView().getTitle().split(":")[1]);
            try {
                if (sl == 3 || sl == 4 || sl == 9 || sl == 12 || sl == 13 || sl == 21 || sl == 22 || sl == 30 || sl == 31) {
                    set.getPage(0).Open(player);
                    return;
                }
                if (sl == 18 || sl == 5 || sl == 6 || sl == 14 || sl == 15 || sl == 23 || sl == 24 || sl == 32 || sl == 33) {
                    set.getPage(1).Open(player);
                    return;
                }
                if (sl == 0 || sl == 1 || sl == 2 || sl == 10 || sl == 11 || sl == 19 || sl == 20 || sl == 28 || sl == 29) {
                    set.getPage(2).Open(player);
                    return;
                }
                if (sl == 27 || sl == 7 || sl == 8 || sl == 16 || sl == 17 || sl == 25 || sl == 26 || sl == 34 || sl == 35) {
                    set.getPage(3).Open(player);
                    return;
                }
                if (sl == 36) {
                    player.closeInventory();
                }
            }catch (NullPointerException ignored){}
            return;
        }
        if(e.getView().getTitle().contains("classconfirm:")){
            Player player = (Player) e.getWhoClicked();
            String[] args = e.getView().getTitle().split(":");
            ClassSet set = m.getClassSet(args[1]);
            e.setCancelled(true);
            int sl = e.getSlot();
            try {
                switch (sl) {
                    case 0:
                        set.getPage(2).Open(player);
                        break;
                    case 9:
                        set.getPage(0).Open(player);
                        break;
                    case 18:
                        set.getPage(1).Open(player);
                        break;
                    case 27:
                        set.getPage(3).Open(player);
                        break;
                    case 36:
                        set.Open(player);
                        break;
                    default:
                        if (sl > 36) {
                            Main.floader.ChangeValkiriaType(player, set.getClassOf(Integer.parseInt(args[2])));
                            player.closeInventory();
                        }
                        break;
                }
            } catch (NullPointerException ignored){}
        }
    }
}