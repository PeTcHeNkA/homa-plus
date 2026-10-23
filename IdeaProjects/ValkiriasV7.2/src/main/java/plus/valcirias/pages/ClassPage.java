package plus.valcirias.pages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class ClassPage {

    protected ItemStack BigIco;
    private final Inventory inventory;

    public ClassPage(ClassSet set, ItemStack bigIco, ItemStack info, int index, ItemStack bg, ItemStack[] IcoClasses){
        this.BigIco = bigIco;

        inventory = Bukkit.createInventory(null,45, "classconfirm:"+set.getName()+String.format(":%s", index));
        inventory.setItem(9, IcoClasses[0]);
        inventory.setItem(18,  IcoClasses[1]);
        inventory.setItem(0,  IcoClasses[2]);
        inventory.setItem(27,  IcoClasses[3]);
        inventory.setItem(24, info);
        inventory.setItem(29, bigIco);
        inventory.setItem(36, bg);
    }


    public  void Open(Player player){
        player.openInventory(inventory);
    }
}
