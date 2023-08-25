package plus.common.spship.modules;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IItemLink;


public class SpInvProxy {
    public final ItemStack[] items;
    public final int[] slots;
    public final IInventory inv;
    private final IItemLink[] links;

    public SpInvProxy(IInventory inv, int size) {
        this.items = new ItemStack[size];
        this.slots = new int[size];
        this.links = new IItemLink[size];
        this.inv = inv;
    }


    public int size(){
        return items.length;
    }


    public IItemLink link(int slot){
        IItemLink link = links[slot];
        if(link==null)
            link = links[slot] = new FInvItemLink(inv, slots[slot], items[slot]);
        return link;
    }


    public ItemStack getItem(int slot){
        return items[slot];
    }


    public void setItem(int slot, ItemStack item){
        inv.setInventorySlotContents(slots[slot], items[slot] = item);
    }


    public ItemStack initItem(int slot, int index){
        return items[slot] = inv.getStackInSlot(slots[slot] = index);
    }
}
