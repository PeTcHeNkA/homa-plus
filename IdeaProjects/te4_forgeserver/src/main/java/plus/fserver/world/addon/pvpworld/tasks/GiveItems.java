package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.function.Consumer;


public class GiveItems implements Consumer<EntityPlayerMP> {
    private final ItemStack[] stacks;
    private final int[] slots;

    public GiveItems(NBTTagList list) {
        stacks = new ItemStack[list.tagCount()];
        slots  = new int[list.tagCount()];

        for (int i = 0; i < list.tagCount(); i++){
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            int slot = nbt.getByte("Slot") & 255;

            stacks[i] = new ItemStack(nbt);
            slots [i] = slot;
        }
    }


    @Override
    public void accept(EntityPlayerMP player) {
        ItemStack[] stacks = this.stacks;
        int[] slots        = this.slots;

        InventoryPlayer inv = player.inventory;

        for (int i = 0; i < stacks.length; i++){
            ItemStack stack = stacks[i].copy();
            if(!inv.add(slots[i], stack)){
                player.dropItem(stack, false);
            }
        }
    }
}