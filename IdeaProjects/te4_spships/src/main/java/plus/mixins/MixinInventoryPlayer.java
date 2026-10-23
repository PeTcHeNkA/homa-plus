package plus.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;
import java.util.List;


@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IInventory {
    @Final
    @Mutable
    @Shadow
    private List<NonNullList<ItemStack>> allInventories;
    @Shadow
    public EntityPlayer player;
    @Final
    @Shadow
    public final NonNullList<ItemStack> mainInventory = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
    @Final
    @Shadow
    public final NonNullList<ItemStack> armorInventory = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
    @Final
    @Shadow
    public final NonNullList<ItemStack> offHandInventory = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);

    public final NonNullList<ItemStack> spInventory =
            NonNullList.withSize(12, ItemStack.EMPTY);


    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityPlayer playerIn, CallbackInfo ci){
        allInventories = Arrays.asList(
                mainInventory,
                armorInventory,
                offHandInventory,
                spInventory
        );
    }



    /**
     * @author
     * @reason
     */
    @Overwrite
    public void dropAllItems() {
        dropAllFor(mainInventory);
        dropAllFor(armorInventory);
        dropAllFor(offHandInventory);
        dropAllFor(spInventory);
    }


    private void dropAllFor(List<ItemStack> list){
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            if (!itemstack.isEmpty()) {
                this.player.dropItem(itemstack, true, false);
                list.set(i, ItemStack.EMPTY);
            }
        }
    }


    /**
     * @author Te4hno
     * @reason patch
     */
    @Overwrite
    public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
        ItemStack item;
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            item = mainInventory.get(i);
            if (!item.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                item.writeToNBT(tag);
                nbtTagListIn.appendTag(tag);
            }
        }

        for (int j = 0; j < this.armorInventory.size(); ++j) {
            item = armorInventory.get(j);
            if (!item.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)(j + 50));
                item.writeToNBT(tag);
                nbtTagListIn.appendTag(tag);
            }
        }

        item = offHandInventory.get(0);
        if (!item.isEmpty()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Slot", (byte) 59);
            item.writeToNBT(tag);
            nbtTagListIn.appendTag(tag);
        }

        for(int l = 0; l < this.spInventory.size(); ++l){
            item = spInventory.get(l);
            if (!item.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)(l+60) );
                item.writeToNBT(tag);
                nbtTagListIn.appendTag(tag);
            }
        }
        return nbtTagListIn;
    }


    /**
     * @author Te4hno
     * @reason patch
     */
    @Overwrite
    public void readFromNBT(NBTTagList nbtTagListIn) {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();
        spInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty()) {
                if (j < this.mainInventory.size()) {
                    this.mainInventory.set(j, itemstack);
                } else if (j >= 50 && j < 59) {
                    this.armorInventory.set(j - 50, itemstack);
                } else if (j == 59) {
                    this.offHandInventory.set(0, itemstack);
                } else if(j >= 60){
                    spInventory.set(j-60, itemstack);
                }
            }
        }
    }
}