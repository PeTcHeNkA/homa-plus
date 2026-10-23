package plus.invlib.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;


public class EquipmentSlotProxy extends SSlot {
    private final EntityPlayer player;
    private final EntityEquipmentSlot slot;
    private final int is;


    public EquipmentSlotProxy(EntityPlayer player, InventoryPlayer playerInventory, int index, int xPos, int yPos, EntityEquipmentSlot slot, int is) {
        super(playerInventory, index, xPos, yPos);
        this.player = player;
        this.slot = slot;
        this.is = is;
    }


    public int getSlotStackLimit() {
        return 1;
    }


    public boolean isItemValid(ItemStack stack) {
        return stack.getItem().isValidArmor(stack, slot, player);
    }


    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public String getSlotTexture() {
        return ItemArmor.EMPTY_SLOT_NAMES[is];
    }
}
