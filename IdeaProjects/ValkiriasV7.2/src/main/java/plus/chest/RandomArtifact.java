package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.craft.CraftArtifact;
import plus.util.FastRandom;


public class RandomArtifact implements RandomThing{

    private final double chance;
    private final CraftArtifact item;


    public RandomArtifact(String hashOfItem, double chance){
        this.chance = chance;
        item = new CraftArtifact(hashOfItem);
    }


    @Override
    public ItemStack getItem(){
        if(FastRandom.r.nextDouble()<chance){
            return item.toItem();
        } else return null;

    }
}
