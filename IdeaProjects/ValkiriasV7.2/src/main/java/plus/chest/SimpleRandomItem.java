package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.util.FastRandom;


public class SimpleRandomItem implements RandomThing{

    protected final ItemStack item;
    protected final double chance;

    public SimpleRandomItem(ItemStack item, double chance){
        this.item = item;
        this.chance = chance;
    }


    @Override
    public ItemStack getItem(){
        if(FastRandom.r.nextDouble()<chance) return item.clone();
        else return null;
    }
}
