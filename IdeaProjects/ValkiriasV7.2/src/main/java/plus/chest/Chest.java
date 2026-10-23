package plus.chest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.util.FastRandom;

import java.util.List;


public class Chest extends SimpleChest{

    protected final String display;
    protected final int uid;
    protected byte size = 27;


    public Chest(String display, int size, List<String> hashOfiItems, String rawCount){
        this(display, size, hashOfiItems, parseRange(rawCount));
    }


    public Chest(String display, int size, List<String> hashOfiItems, int... count){
        super(hashOfiItems, count);
        uid = FastRandom.r.nextInt(32768);
        if(size>0) this.size = (byte) size;
        this.display = display==null?"":display;
    }


    public Inventory getRandomChest(){
        Inventory inventory = Bukkit.createInventory(null, size, display);

        for(ItemStack i:dropContent()){
            int slot = FastRandom.r.nextInt(size);
            if(inventory.getItem(slot)==null || inventory.getItem(slot).getType()== Material.AIR){
                inventory.setItem(slot, i);
            } else {
                if(inventory.firstEmpty()>-1) inventory.setItem(inventory.firstEmpty(), i);
                else break;
            }
        }
        return inventory;
    }
}