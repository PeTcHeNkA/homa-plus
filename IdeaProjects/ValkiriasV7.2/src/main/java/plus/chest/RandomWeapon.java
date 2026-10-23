package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.craft.CraftWeapon;
import plus.util.FastRandom;


public class RandomWeapon implements RandomThing{

    private final double chance;
    private final CraftWeapon item;


    public RandomWeapon(String hashOfItem, double chance){
        this.chance = chance;
        item = new CraftWeapon(hashOfItem);
    }


    @Override
    public ItemStack getItem(){
        if(FastRandom.r.nextDouble()<chance){
            return item.toItem();
        } else return null;

    }
}
