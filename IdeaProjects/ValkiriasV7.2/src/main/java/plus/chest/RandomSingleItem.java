package plus.chest;

import plus.Main;


public class RandomSingleItem extends SimpleRandomItem{

    public RandomSingleItem(String hashOfItem, double chance, int count){
        super(Main.generator.buildStandartItem(hashOfItem), chance);
        item.setAmount(count);
    }
}