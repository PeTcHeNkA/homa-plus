package plus.system;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.craft.BetterCraft;
import plus.craft.CraftTable;
import plus.util.ConfigHelper;
import plus.ValkiriaManager;
import java.util.Arrays;


public class CraftModule implements Listener {

    public static CraftTable craftTable;
    private final int[] bettercraftSlots = new int[]{23, 24, 25, 32, 33, 34, 41, 42, 43};

    public static void  Init(){
        if(craftTable == null)craftTable = new CraftTable();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getView().getTitle().contains(ConfigHelper.craftsView)){
            Inventory inv = e.getInventory();
            for (int invSlot : bettercraftSlots) {
                ItemStack item = inv.getItem(invSlot);
                if(item!=null)ValkiriaManager.addOrDropItem((Player) e.getPlayer(),item);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getView().getTitle().contains(ConfigHelper.craftsView)){
            if(e.getClickedInventory().getType() != InventoryType.PLAYER) {
                Player player = (Player) e.getWhoClicked();
                int slot = e.getSlot();
                boolean isCraftSlot = slot == 35;

                if(e.getClick().isShiftClick() && !isCraftSlot){
                    e.setCancelled(true);
                    return;
                }

                String[] hash = e.getView().getTitle().split(":");
                int recept = Integer.parseInt(hash[1]);
                int Shift = Integer.parseInt(hash[2]);

                Inventory inventory = e.getInventory();

                if(slot == 0 || slot == 8){
                    e.setCancelled(true);
//                    ItemStack[] items = new ItemStack[9];
//                    int flag = 0;
//                    for(int invSlot:bettercraftSlots){
//                        items[flag] = inventory.getItem(invSlot);
//                        ++flag;
//                    }
                    if(slot==0){
                        Shift-=1;
                    } else {
                        ++Shift;
                    }
                    craftTable.Open(player, recept, Shift);
                    e.setCancelled(true);
                    //for(ItemStack item:items){
                    //    ValkiriaManager.addOrDropItem(player, item);
                    //}
                    return;
                } else if(slot<8){
                    if(e.getInventory().getItem(slot)!=null){
                        craftTable.Open(player,Shift+slot-1, Shift);
                        //openBetterCraft(player,Shift+slot-1, Shift);
                        e.setCancelled(true);
                        return;
                    }

                }

                if(Arrays.binarySearch(bettercraftSlots, slot) < 0){
                    if(isCraftSlot){
                        //System.out.println("nen");
                        if((e.getCursor()== null || e.getCursor().getType() == Material.AIR) &&!( inventory.getItem(slot)==null || inventory.getItem(slot).getType() == Material.AIR ) ) {
                            int index = 0;

                            for (int invSlot : bettercraftSlots) {
                                ItemStack selectedItem = inventory.getItem(invSlot);
                                if(selectedItem!= null){
                                    ItemStack item1 = inventory.getItem(CraftTable.receptSlots[index]);
                                    if(item1==null)continue;
                                    int newAmount = selectedItem.getAmount()-item1.getAmount();
                                    if(newAmount>0){
                                        selectedItem.setAmount(newAmount);
                                        inventory.setItem(invSlot, selectedItem);
                                    } else{
                                        inventory.setItem(invSlot, null);
                                    }
                                }
                                ++index;
                            }
                            ValkiriaManager.UpdateInvWithCD(player);

                        } else {
                            e.setCancelled(true);
                        }
                    }else {
                        e.setCancelled(true);
                    }
                    return;
                }

                if (recept > -1) {
                    BetterCraft craft = craftTable.Crafts[recept];// ConfigHelper.Crafts[recept];
                    ItemStack[] items = new ItemStack[9];
                    int flag = 0;
                    for(int invSlot:bettercraftSlots){
                        if(invSlot==slot) items[flag] = e.getCursor();
                        else items[flag] = inventory.getItem(invSlot);
                        ++flag;
                    }
                    if(craft.isCraft(items)){
                        inventory.setItem(35,craft.getResult());
                    } else {
                        inventory.setItem(35, null);
                    }
                    ValkiriaManager.UpdateInvWithCD(player);
                } else {
                    e.setCancelled(true);
                }
            } else {
                if(e.getClick().isShiftClick()){
                    e.setCancelled(true);
                }
            }
        }
    }
}