package plus.chest.reward;

import plus.chest.SimpleChest;
import plus.valcirias.Valkiria;
import plus.util.cfg.Config;
import plus.util.FastRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RewardRandomBox implements RewardBox{

    protected List<RandomRewardThing> items = new ArrayList<>();
    protected final int min;
    protected final int max;
    protected String server = null;

    public RewardRandomBox(Config c, String name, boolean isPath){
        this(
                c.getOrSetStringList( isPath ? name:"reward_boxes." + name + ".items", Arrays.asList("example", "example")),
                c.getOrSetString(isPath ? name:"reward_boxes." + name + ".count", "e")
        );
        String s = c.getOrSetString("reward_boxes." + name + ".server", "none");
        server = s.equalsIgnoreCase("none")?c.getOrSetString("server_to_redirect", "main"):s;
    }


    public RewardRandomBox(Config c, String name){
        this(c, name, false);
    }


    public RewardRandomBox(List<String> hashOfItem, String rawRange){
        this(hashOfItem, SimpleChest.parseRange(rawRange));
    }


    public RewardRandomBox(List<String> hashOfiItems, int... range){
        if(range.length>0) {
            this.min = range[0];
            this.max = range.length > 1 ? range[1] : min;
        } else {
            this.min = 0;
            this.max = hashOfiItems.size();
        }
        for(String hashOfItem:hashOfiItems){
            String[] args = hashOfItem.split(":");
            items.add(new RandomRewardItem(args[2], Double.parseDouble(args[0]), Integer.parseInt(args[1])));
        }
    }


    //@Override
    protected String getRandomLootCode(){

        StringBuilder s = new StringBuilder();
        int count = 0;
        for(RandomRewardThing item:items){
            String itemCode = item.toCodedItem();
            if(count>max)break;
            if(itemCode != null){
                s.append(itemCode).append(";");
                ++count;
            }
        }

        while (count<min){
            String itemCode = items.get(FastRandom.r.nextInt(items.size())).toCodedItem();
            if(itemCode != null){
                s.append(itemCode).append(";");
                ++count;
            }
        }
        return s.toString();
    }


    @Override
    public void reward(Valkiria v){
        String s1 = v.getReward();
        if(s1 == null){
            v.setReward(getRandomLootCode());
        } else {
            if(s1.charAt(s1.length()-1)==';'){
                s1 = s1+getRandomLootCode();
            } else {
                s1 = s1+";"+getRandomLootCode();
            }
            v.setReward(s1);
        }
    }


    @Override
    public String getRedirectServer() {
        return server;
    }
}