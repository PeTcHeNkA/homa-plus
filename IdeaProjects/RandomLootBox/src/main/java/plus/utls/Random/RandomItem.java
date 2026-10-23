package plus.utls.Random;

import org.bukkit.inventory.ItemStack;
import plus.main;

public class RandomItem {

    private ItemStack item;
    public double chance;

    public RandomItem(String rawData){
        String[] dt = rawData.split(";");
        chance = Double.parseDouble(dt[0]);
        item = main.coder.decodeItem(dt[1]);
    }
    public ItemStack getItem(){
        return main.r.nextDouble()<chance?item:null;
    }
}
