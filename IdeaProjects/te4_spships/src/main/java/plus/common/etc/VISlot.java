package plus.common.etc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public class VISlot extends VSlot{
    private final ItemStack item;
    public VISlot(ItemStack item, int index, int xPosition, int yPosition) {
        super(null, index, xPosition, yPosition);
        this.item = item;
    }


    @Override
    public ItemStack getStack() {
        return item;
    }


    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        return ItemStack.EMPTY;
    }


    @Override
    public void onSlotChanged() {}


    @Override
    public ItemStack decrStackSize(int amount) {
        return ItemStack.EMPTY;
    }


    @Override
    public void putStack(ItemStack stack) {}
}