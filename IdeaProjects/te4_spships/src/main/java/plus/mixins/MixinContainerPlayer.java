package plus.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.common.etc.DSlot;
import plus.common.etc.SyncRenderSlot;
import plus.common.player.PlayerSpship;


@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends Container {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(InventoryPlayer plInv, boolean localWorld,
                     EntityPlayer player, CallbackInfo ci){

        this.addSlotToContainer(new DSlot(plInv, 41, 9,   98));
        this.addSlotToContainer(new DSlot(plInv, 42, 35,  98));
        this.addSlotToContainer(new DSlot(plInv, 43, 61,  98));
        this.addSlotToContainer(new DSlot(plInv, 44, 87,  98));
        this.addSlotToContainer(new DSlot(plInv, 45, 113, 98));

        this.addSlotToContainer(new DSlot(plInv, 46, 9,  124));
        this.addSlotToContainer(new DSlot(plInv, 47, 35, 124));
        this.addSlotToContainer(new DSlot(plInv, 48, 61, 124));
        this.addSlotToContainer(new DSlot(plInv, 49, 87, 124));
        this.addSlotToContainer(new DSlot(plInv, 50, 113,124));

        this.addSlotToContainer(new DSlot(plInv, 51, 0,0));
        this.addSlotToContainer(SyncRenderSlot.build(plInv, 52, 77, 8, player));
    }


    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        if(slotId>45 && slotId < 57){//46 <-> 55 and 56
            ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
            PlayerSpship sp = (PlayerSpship) player;
            sp.getSp().calcParams();
            return result;
        } else {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }
}