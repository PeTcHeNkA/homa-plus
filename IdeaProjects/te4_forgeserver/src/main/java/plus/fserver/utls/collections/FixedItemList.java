package plus.fserver.utls.collections;

import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;


public class FixedItemList implements Iterable<ItemStack>{
    private final ItemStack[] items;

    public FixedItemList(int size) {
        this.items = new ItemStack[size];
        Arrays.fill(items, ItemStack.EMPTY);
    }

    public ItemStack get(int i){
        return items[i];
    }


    public int size(){
        return items.length;
    }


    public ItemStack set(int i, ItemStack stack){
        ItemStack prev = items[i];
        items[i] = stack == null?ItemStack.EMPTY:stack;
        return prev;
    }


    public void fSet(int i, ItemStack stack){
        items[i] = stack == null?ItemStack.EMPTY:stack;
    }


    public void clear(){
        for (int i=0;i<items.length;i++) items[i] = ItemStack.EMPTY;
    }


    @NotNull
    @Override
    public Iterator<ItemStack> iterator() {
        return new ObjectArrayIterator<>(items);
    }


    @Override
    public Spliterator<ItemStack> spliterator() {
        return Spliterators.spliterator(items, 0);
    }
}