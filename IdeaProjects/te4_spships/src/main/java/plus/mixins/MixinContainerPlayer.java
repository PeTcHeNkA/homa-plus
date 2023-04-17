package plus.mixins;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.common.etc.DSlot;
import plus.common.player.PlayerSpship;

import javax.annotation.Nullable;


@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends Container {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(InventoryPlayer playerInventory, boolean localWorld,
                     EntityPlayer playerIn, CallbackInfo ci){

        this.addSlotToContainer(new DSlot(playerInventory, 41, 9,   98));
        this.addSlotToContainer(new DSlot(playerInventory, 42, 35,  98));
        this.addSlotToContainer(new DSlot(playerInventory, 43, 61,  98));
        this.addSlotToContainer(new DSlot(playerInventory, 44, 87,  98));
        this.addSlotToContainer(new DSlot(playerInventory, 45, 113, 98));

        this.addSlotToContainer(new DSlot(playerInventory, 46, 9,  124));
        this.addSlotToContainer(new DSlot(playerInventory, 47, 35, 124));
        this.addSlotToContainer(new DSlot(playerInventory, 48, 61, 124));
        this.addSlotToContainer(new DSlot(playerInventory, 49, 87, 124));
        this.addSlotToContainer(new DSlot(playerInventory, 50, 113,124));

        this.addSlotToContainer(new DSlot(playerInventory, 51, 0,0));
    }


    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        if(slotId>45 && slotId < 57){//46 <-> 55 and 56
            ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
            PlayerSpship sp = (PlayerSpship) player;
            sp.getSp().calcParams(player);
            return result;
        } else {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }
}