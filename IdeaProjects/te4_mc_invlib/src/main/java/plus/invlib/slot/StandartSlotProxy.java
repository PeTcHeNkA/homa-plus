package plus.invlib.slot;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class StandartSlotProxy extends SSlot {

    private final EntityPlayer thePlayer;
    private int removeCount;
    
    public StandartSlotProxy(EntityPlayer player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition){
        super(inventoryIn, slotIndex, xPosition, yPosition);
        this.thePlayer = player;
        //player.inventory.
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack){
        return true;
    }


    @Override
    public ItemStack decrStackSize(int amount){
        if (this.getHasStack()){
            this.removeCount += Math.min(amount, this.getStack().getCount());
        }
        return super.decrStackSize(amount);
    }


    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack){
        this.onCrafting(stack);
        super.onTake(player, stack);
        return stack;
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount){
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack){
        stack.onCrafting(this.thePlayer.world, this.thePlayer, this.removeCount);
        this.removeCount = 0;    
    }
//
//    @Nullable
//    @SideOnly(Side.CLIENT)
//    public String getSlotTexture(){
//        return Te4InvContainer.RES_LOC;
//    }
}