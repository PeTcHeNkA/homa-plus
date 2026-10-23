package plus.block.teleport;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import plus.valcirias.Valkiria;
import plus.Main;


public class GuiEvents implements Listener {

    private static GuiEvents I;

    public static GuiEvents getInstance(){
        if(I==null){
            I = new GuiEvents();
            Bukkit.getPluginManager().registerEvents(I, Main.instance);
        }
        return I;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!e.getView().getTitle().startsWith("tpgui:")){
            return;
        }
        if(e.getClickedInventory()==null)return;
        e.setCancelled(true);

        if(e.getSlot()==45){
            e.getWhoClicked().closeInventory();
        } else {
            String[] hs = e.getView().getTitle().split(":");
            GuiTeleport gt = GuiTeleport.hash.get(hs[1]);
            if (e.getSlot() > 45) {
                Valkiria v = Main.floader.getValkiria(e.getWhoClicked().getName());
                VisualLocation vl = gt.locations[Integer.parseInt(hs[3])];
                if(v.getShield()>vl.price) {
                    vl.teleport(v);
                    //v.toPlayer().teleport(vl.loc);
                    e.getWhoClicked().spigot().sendMessage(GuiTeleport.msgTeleportProcessing);
                    v.damage(vl.price);
                } else {
                    v.toPlayer().spigot().sendMessage(GuiTeleport.msgLittleEnergy);
                }
            } else {
                int shift = Integer.parseInt(hs[2]);
                if(e.getClickedInventory().getItem(e.getSlot()) != null) {
                    if (e.getSlot() == 9) e.getWhoClicked().openInventory(gt.generateInventory(shift, shift - 1));
                    else if (e.getSlot() == 18) e.getWhoClicked().openInventory(gt.generateInventory(shift, shift));
                    else if (e.getSlot() == 27) e.getWhoClicked().openInventory(gt.generateInventory(shift, shift + 1));
                }
                else {
                    int selected = Integer.parseInt(hs[3]);
                    if (e.getSlot() == 0) e.getWhoClicked().openInventory(gt.generateInventory(shift - 1, selected));
                    else if (e.getSlot() == 36)  e.getWhoClicked().openInventory(gt.generateInventory(shift + 1, selected));
                }
            }
        }
    }
}