package plus.chest;

import org.bukkit.inventory.ItemStack;
import plus.Main;
import plus.util.cfg.Sector;
import plus.util.FastRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SimpleChest {

    protected final List<RandomThing> items = new ArrayList<>();
    protected final int min;
    protected final int max;


    public SimpleChest(Sector c, String name, boolean isPath){
        this(
                c.getOrSetStringList( isPath ? name+".items":"loot_tables." + name + ".items", Arrays.asList("example", "example")),
                parseRange(c.getOrSetString(isPath ? name+".count":"loot_tables." + name + ".count", "e"))
        );
    }


    public SimpleChest(Sector c, String name){
        this(c, name, false);
    }


    public static int[] parseRange(String count){
        if(count.equalsIgnoreCase("e"))return new int[0];
        int Min;
        int Max;
        if(count.contains("-")){
            String[] cs = count.split("-");
            Min = cs[0].equals("*")?Integer.MIN_VALUE:Integer.parseInt(cs[0]);
            Max = cs[1].equals("*")?Integer.MIN_VALUE:Integer.parseInt(cs[1]);
        } else {
            Min = Integer.parseInt(count);
            Max = Min;
        }
        return new int[]{Min, Max};
    }


    public SimpleChest(List<String> hashItems, int...range){
        if(range.length>0) {
            this.min = range[0];
            this.max = range.length > 1 ? range[1] : min;
        } else {
            this.min = 0;
            this.max = hashItems.size();
        }

        for(String hashOfItem:hashItems){
            String[] args = hashOfItem.split(":");

            if(args[0].equalsIgnoreCase("W")) {
                items.add(new RandomWeapon(args[3], Double.parseDouble(args[1])));
            } else if (args[0].equalsIgnoreCase("A")) {
                items.add(new RandomArtifact(args[3], Double.parseDouble(args[1])));

            } else if (args[0].equalsIgnoreCase("E")) {
                items.add(new SimpleRandomItem(Main.floader.bd.itemCoder.encode(args[3]), Double.parseDouble(args[1])));
            } else{
                int minItems;
                int maxItems;

                if(args[2].contains("-")) {
                    String[] hash = args[2].split("-");
                    minItems = Integer.parseInt(hash[0]);
                    maxItems = Integer.parseInt(hash[1]);
                } else {
                    minItems = Integer.parseInt(args[2]);
                    maxItems = minItems;
                }
                if(minItems>maxItems){
                    int i = minItems;
                    minItems = maxItems;
                    maxItems = i;
                }
                items.add(RandomThing.buildRandomItem(args[3], Double.parseDouble(args[1]), minItems, maxItems));
            }
        }
    }


    public ItemStack[] dropContent(){
        List<ItemStack> drop = new ArrayList<>();

        for(RandomThing thing:items){
            if(drop.size()>=max)break;
            ItemStack i = thing.getItem();
            if(i != null)drop.add(i);
        }
        while (drop.size()<min){
            ItemStack i = items.get(FastRandom.r.nextInt(items.size())).getItem();
            if(i != null) drop.add(i);
        }
        return drop.toArray(new ItemStack[0]);
    }
}