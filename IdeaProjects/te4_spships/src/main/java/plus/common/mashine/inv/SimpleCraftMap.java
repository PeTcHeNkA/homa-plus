package plus.common.mashine.inv;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;


public abstract class SimpleCraftMap extends HashMap<Item, SimpleCraftMap.ItemGen> {
    public SimpleCraftMap(){
        onInit();
    }


    public abstract void onInit();


    public static class ItemGen{
        private final Item item;
        private ItemGen(Item item) {
            this.item = item;
        }
        public Item get() {
            return item;
        }
        public ItemStack create() {
            return new ItemStack(item);
        }
        public int getCount(){
            return 1;
        }
    }


    static class CItemGen extends ItemGen{
        public final int count;
        private CItemGen(Item item, int count) {
            super(item);
            this.count = count;
        }
        @Override
        public ItemStack create() {
            ItemStack result = super.create();
            result.setCount(count);
            return result;
        }
        @Override
        public int getCount() {
            return count;
        }
    }


    public Item getItem(Item key) {
        return super.get(key).get();
    }


    protected void put(Item key, Item result, int count){
        put(key, count==1?new ItemGen(result):new CItemGen(result, count));
    }


    protected void add(Block item, Block result, int count){
        put(Item.getItemFromBlock(item), Item.getItemFromBlock(result), count);
    }


    protected void add(Block item, Item result, int count){
        put(Item.getItemFromBlock(item), result, count);
    }


    protected void add(Item item, Block result, int count){
        put(item, Item.getItemFromBlock(result), count);
    }


    protected void add(Item item, Item result, int count){
        put(item, result, count);
    }


    protected void add(Item item, Item result){
        add(item, result, 1);
    }


    protected void add(Block item, Item result){
        add(item, result, 1);
    }


    protected void add(Item item, Block result){
        add(item, result, 1);
    }


    protected void add(Block item, Block result){
        add(item, result, 1);
    }
}