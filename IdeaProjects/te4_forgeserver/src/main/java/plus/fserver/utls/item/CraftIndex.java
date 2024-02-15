package plus.fserver.utls.item;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import java.util.Arrays;


public class CraftIndex {
    private final Item[] items;
    private final int hash;
    public CraftIndex(InventoryCrafting inv){
        items = new Item[inv.getSizeInventory()];
        int hash = 0;
        Item item;
        for (int i=0;i<items.length;i++){
            items[i] = item = inv.getStackInSlot(i).getItem();
            if(item != Items.AIR){
                hash = hash * 31 + item.getId();
            }
        }
        this.hash = hash;
    }


    @Override
    public int hashCode() {
        return hash;
    }


    public boolean isEmpty(){
        return hash == 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        CraftIndex that = (CraftIndex) o;
        return Arrays.equals(items, that.items);
    }
}