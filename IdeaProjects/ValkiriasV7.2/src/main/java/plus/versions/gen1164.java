package plus.versions;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


public class gen1164 implements AbstractItemGenerator{


    @Override
    public ItemStack writeTag(ItemStack item, String tag, String value) {
        net.minecraft.server.v1_16_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tags = itemStack.getOrCreateTag();
        tags.setString(tag, value);
        itemStack.setTag(tags);
        return  CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public String getTag(ItemStack item, String tag) {
        net.minecraft.server.v1_16_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        if(itemStack.hasTag()) return itemStack.getTag().getString(tag);
        else return null;
    }

    @Override
    public boolean hasTag(ItemStack item, String tag) {
        net.minecraft.server.v1_16_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        return itemStack.hasTag() ? itemStack.getTag().hasKey(tag):false;
    }
}
