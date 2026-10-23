package plus.utls.Random;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.main;
import plus.utls.Te4hnoConfig;

import java.util.ArrayList;
import java.util.List;

public class RandomInv {

    public Inventory edit;
    List<RandomItem> items = new ArrayList<>();

    public RandomInv(){
        Import();
    }

    public ItemStack[] getRandomContent(int MaxSize){
        ItemStack[] ri = new ItemStack[MaxSize];

        try {
            int index = 0;
            for(ItemStack i:ri){
                ri[index] = items.get(index).getItem();
                index+=1;
            }
        } catch (IndexOutOfBoundsException e){}
        return ri;
    }

    public void Export(){
        if(edit==null)return;

        Te4hnoConfig c = main.config;
        List<String> items = new ArrayList<>();
        for(ItemStack i:edit.getContents()){
            if(i != null && i.getType() != Material.AIR)
            items.add("1.0;"+main.coder.codeItem(i));
        }
        main.config.set("chest", items);
    }

    public void Import(){
        items.clear();
        for(String rd:main.config.getStringList("chest")) {
            items.add(new RandomItem(rd));
        }
    }
}
