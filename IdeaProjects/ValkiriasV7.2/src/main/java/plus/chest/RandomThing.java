package plus.chest;

import org.bukkit.inventory.ItemStack;

public interface RandomThing {
    ItemStack getItem();


    static RandomThing buildRandomItem(String hashOfItem, double chance, int min, int max){
        if(min==max)return new RandomSingleItem(hashOfItem, chance, min);
        else return new RandomItem(hashOfItem, chance, min, max);
    }
}