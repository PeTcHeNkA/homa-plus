package plus.craft;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.util.ConfigHelper;
import plus.Main;
import plus.util.cfg.Config;
import java.util.ArrayList;
import java.util.List;


public class CraftTable {

    public final BetterCraft[] Crafts;
    public static final int[] receptSlots = new int[]{18, 19, 20, 27, 28, 29, 36, 37, 38};

    public CraftTable(){
        Config cItems = Main.instance.getOrCreateYandeReConfig("rpg_crafts.yml");
        ConfigurationSection keys1 = cItems.getConfigurationSection("crafts");

        if(keys1 != null) {
            String[] keys = keys1.getKeys(false).toArray(new String[0]);
            List<BetterCraft> cr = new ArrayList<>();
            for (String key : keys) {
                try {
                    cr.add(
                            new BetterCraft(
                                    cItems.getStringList("crafts."+key+".items"),
                                    cItems.getString("crafts."+key+".result"))
                    );
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            Crafts = cr.toArray(new BetterCraft[0]);

        } else {
            Crafts = new BetterCraft[0];
        }
    }


    public void Open(Player player, int craftId, int Shift){
        if(craftId<-1)craftId = -1;
        if(Shift<0)Shift = 0;
        Inventory inv = Bukkit.createInventory(null,45, String.format("%s:%s:%s", ConfigHelper.craftsView, craftId, Shift));
        int length = Crafts.length;
        ItemStack[] receptList = new ItemStack[7];
        int flag = Shift;

        for(int i = 0; i<7;i++){
            if(Shift+i<length){

                receptList[i] = Crafts[flag].viewResult();
            } else break;
            ++flag;
        }
        flag = 1;
        for(ItemStack recept:receptList){
            inv.setItem(flag, recept);
            ++flag;
        }

        if(craftId > -1){
            inv.setItem(22, ConfigHelper.betterCraftGuiRecpt);
            BetterCraft craft = Crafts[craftId];
            inv.setItem(30, craft.viewResult());
            ItemStack[] receptItems = craft.getNeedItems();
            flag = 0;
            for (int slot:receptSlots){
                inv.setItem(slot, receptItems[flag]);
                ++flag;
            }
        } else {
            inv.setItem(22, ConfigHelper.betterCraftGuiNull);
        }
        player.openInventory(inv);
    }
}
