package plus.fserver.utls.collections;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.jetbrains.annotations.NotNull;
import java.util.*;


public class FixedItemList implements Iterable<ItemStack>{
    private final ItemStack[] items;

    public FixedItemList(int size) {
        this.items = new ItemStack[size];
        Arrays.fill(items, ItemStack.EMPTY);
    }

    public ItemStack get(int i){
        return items[i];
    }


    public ItemStack[] raw(){
        return items;
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


    public static ItemStack getAndSplit(FixedItemList stacks, int index, int amount) {
        ItemStack[] items = stacks.items;
        return index >= 0 && index < items.length && !items[index].isEmpty() && amount > 0 ? items[index].splitStack(amount) : ItemStack.EMPTY;
    }


    public static ItemStack getAndRemove(FixedItemList stacks, int index) {
        return index >= 0 && index < stacks.size() ? stacks.set(index, ItemStack.EMPTY) : ItemStack.EMPTY;
    }


    public static void loadAllItems(NBTTagCompound tag, FixedItemList list) {
        NBTTagList nbttaglist = tag.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < list.size())
                list.fSet(j, new ItemStack(nbttagcompound));
        }
    }


    public static NBTTagCompound saveAllItems(NBTTagCompound tag, FixedItemList list) {
        return saveAllItems(tag, list, true);
    }


    public static NBTTagCompound saveAllItems(NBTTagCompound tag, FixedItemList list, boolean saveEmpty) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);

            if (!itemstack.isEmpty()) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setByteU("Slot", (byte)i);
                itemstack.writeToNBT(nbt);
                nbttaglist.appendTag(nbt);
            }
        }

        if (!nbttaglist.isEmpty() || saveEmpty) {
            tag.setTag("Items", nbttaglist);
        }
        return tag;
    }
}