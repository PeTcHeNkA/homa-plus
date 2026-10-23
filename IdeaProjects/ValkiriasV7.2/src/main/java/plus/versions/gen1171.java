package plus.versions;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


public class gen1171 implements AbstractItemGenerator{

    @Override
    public ItemStack writeTag(ItemStack item, String tag, String value) {
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tags = itemStack.getOrCreateTag();
        tags.setString(tag, value);
        itemStack.setTag(tags);
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public String getTag(ItemStack item, String tag) {
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        if(itemStack.hasTag()) return itemStack.getTag().getString(tag);
        else return null;
    }


    @Override
    public boolean hasTag(ItemStack item, String tag) {
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        return itemStack.hasTag() ? itemStack.getTag().hasKey(tag):false;
    }
}
