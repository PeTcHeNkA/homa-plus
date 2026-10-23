package plus.block;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import plus.chest.Chest;
import plus.util.ConfigHelper;
import plus.system.ModuleLoader;

public class LootBox implements InteractableBlock{

    protected int speedRegen;
    protected int power = 100;

    protected Chest chest;
    protected Inventory lotBox;

    public LootBox(String key, int speedRegen){
        //this.key = key;
        this.speedRegen = speedRegen;
        chest = ModuleLoader.ChestC.getChest(key); //chest = ConfigHelper.getChest(key);
        lotBox = chest.getRandomChest();
    }

    @Override
    public void regen() {
        if(power<100)power+=speedRegen;
    }

    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        Inventory inv = open();
        if(inv.isEmpty()){
            e.getPlayer().spigot().sendMessage(ConfigHelper.empetyChest);
        }
        else {
            e.getPlayer().openInventory(inv);
        }
    }

    public Inventory open(){
        if(power>=100){
            power -= 100;
            lotBox = chest.getRandomChest();
        }
        return lotBox;
    }
}
