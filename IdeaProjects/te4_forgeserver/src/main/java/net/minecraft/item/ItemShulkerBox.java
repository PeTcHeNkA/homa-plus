package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import plus.fserver.api.inject.VarOnly;


public class ItemShulkerBox extends ItemBlock {
    public ItemShulkerBox(Block block) {
        super(block);
        this.setMaxStackSize(1);
    }


    @Override
    @VarOnly(var = "fix-shulcer-nbt-view", def = true)
    public void copyForPacket(final ItemStack stack, final ItemStack itemstack) {
        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound copy = null;

        if(nbt != null){
            NBTTagCompound entityTag = nbt.getCompoundTagOrNull("BlockEntityTag");

            if(entityTag != null){
                NBTTagList items = entityTag.getListOrNull("Items", 10);

                if(items != null) {
                    NBTTagList copyItems = new NBTTagList(items.tagCount());
                    int nonEmpty = 0;

                    for (int i = 0, s = items.tagCount(); i < s; i++) {
                        NBTTagCompound itemCopy = new NBTTagCompound(8, 1);
                        NBTTagCompound itemNbt = items.getCompoundTagAt(i);
                        copyItems.appendTag(itemCopy);

                        if (copyItemData(itemCopy, itemNbt, nonEmpty < 6)) ++nonEmpty;
                    }

                    if (nonEmpty != 0) {
                        if(copy == null) copy = new NBTTagCompound(2,1);
                        NBTTagCompound copyEntityTag = new NBTTagCompound(2, 1);
                        copy.setTagU("BlockEntityTag", copyEntityTag);
                        copyEntityTag.setTagU("Items", copyItems);
                    }
                }
            }
        }
        if(copy != null){
            itemstack.setTagCompound(copy);
        }
    }


    @VarOnly(var = "fix-shulcer-nbt-view", def = true)
    private static boolean copyItemData(final NBTTagCompound copy, final NBTTagCompound src, boolean copyTag){
        copy.setTagU("Slot", src.getTag("Slot"));
        NBTBase id = src.getTag("id");
        boolean flag = false;
        if(id != null){
            copy.setTagU("id", id);
            if(copyTag) {
                if (id.getId() == 8) {
                    Item item = Item.getByNameOrId(((NBTTagString) id).getString());
                    if (item != null && item != Items.AIR) {
                        flag = true;
                        if(item == Items.WRITTEN_BOOK || item == Items.WRITABLE_BOOK){
                            copyTag = false;
                        }
                    }
                }
            }
        }
        copy.setTagU("id", id);
        copy.setTagU("Count", src.getTag("Count"));
        copy.setTagU("Damage", src.getTag("Damage"));

        if(copyTag) {
            NBTTagCompound tag = src.getCompoundTagOrNull("tag");
            if (tag != null) {
                copy.setTag("tag", tag.copy());
            }
        }
        return flag;
    }
}