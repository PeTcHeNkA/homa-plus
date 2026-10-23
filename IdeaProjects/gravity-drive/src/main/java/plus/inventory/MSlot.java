package plus.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import javax.annotation.Nullable;


public class MSlot extends Slot {

    //Это по сути копитя обычного ванильного слота

    private final EntityPlayer thePlayer;
    private int removeCount;

    public MSlot(EntityPlayer player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition){
        super(inventoryIn, slotIndex, xPosition, yPosition);
        this.thePlayer = player;
    }

    /* Может ли даный стак быть положен в этот слот. Здесь могут быть проверки, например если вы хотите
        чтоб в слот нельзя было положить яблоко, проверяете равен ли предмет в стаке яблоку, если да, то
        возвращаем false */
    public boolean isItemValid(@Nullable ItemStack stack){
        return true;
    }

    public ItemStack decrStackSize(int amount){
        if (this.getHasStack()){
            this.removeCount += Math.min(amount, this.getStack().getCount());
        }
        return super.decrStackSize(amount);
    }

    //Что происходит, если забрать предмет из слота
    public ItemStack onTake(EntityPlayer player, ItemStack stack){
        this.onCrafting(stack);
        super.onTake(player, stack);
        return stack;
    }


    protected void onCrafting(ItemStack stack, int amount){
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack){
        stack.onCrafting(this.thePlayer.world, this.thePlayer, this.removeCount);
        this.removeCount = 0;
    }
}