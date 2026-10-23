package plus.chest.reward;

import plus.chest.SimpleChest;
import plus.valcirias.data.coder.VItemCoder;
import plus.util.FastRandom;


public class RandomRewardItem implements RandomRewardThing{

    private final String hashOfItem;
    private final double chance;
    private final int min;
    private final int max;


    public RandomRewardItem(String hashOfItem, double chance, String rawRange){
        this(hashOfItem, chance, SimpleChest.parseRange(rawRange));
    }


    public RandomRewardItem(String hashOfItem, double chance, int... range){
        if(range.length>0) {
            this.min = range[0];
            this.max = range.length > 1 ? range[1] : min;
        } else {
            this.min = 1;
            this.max = 1;
        }
        this.hashOfItem = hashOfItem;
        this.chance = chance;
    }


    @Override
    public String toCodedItem(){
        int count = min==max?max:FastRandom.r.nextInt(min, max);
        if(count==1)return null;
        if(count==-1) return FastRandom.r.nextDouble()<chance ? hashOfItem :null;
        else return FastRandom.r.nextDouble() < chance ? VItemCoder.s.charAt(count) + hashOfItem : null;
    }
}