package plus.utls.Coder;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import plus.main;
import plus.utls.LootBox.LootBox;

public class BaseWriter {
    public void Write(String value){
        if(value.contains("u")){
            LootBox.TestFor(value);
        }
    }
}
