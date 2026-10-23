package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.Main;
import plus.util.FastRandom;


public class RandomItem extends SimpleRandomItem{

    private final int min;
    private final int max;

    public RandomItem(String hashOfItem, double chance, int min, int max){
        super(Main.generator.buildStandartItem(hashOfItem), chance);
        this.min = min;
        this.max = max;
    }


    @Override
    public ItemStack getItem(){
        if(FastRandom.r.nextDouble()<chance){
            ItemStack result = item.clone();
            result.setAmount(FastRandom.r.nextInt(min, max));
            return result;
        } else return null;
    }
}