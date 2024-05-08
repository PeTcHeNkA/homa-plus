package net.minecraft.entity.player;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class InventoryPlayer implements IInventory {
    public final NonNullList<ItemStack> mainInventory    = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armorInventory   = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offHandInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories;
    public int currentItem;
    public EntityPlayer player;
    private ItemStack itemStack;
    private int timesChanged;

    public InventoryPlayer(EntityPlayer playerIn) {
        this.allInventories = Arrays.asList(this.mainInventory, this.armorInventory, this.offHandInventory);
        this.itemStack = ItemStack.EMPTY;
        this.player = playerIn;
    }


    public ItemStack getCurrentItem() {
        return isHotbar(this.currentItem) ? this.mainInventory.get(this.currentItem) : ItemStack.EMPTY;
    }


    public static int getHotbarSize() {
        return 9;
    }


    private boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
        return !stack1.isEmpty() && this.stackEqualExact(stack1, stack2) && stack1.isStackable()
                && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < this.getInventoryStackLimit();
    }


    private boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes()
                || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }


    public int getFirstEmptyStack() {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (this.mainInventory.get(i).isEmpty()) return i;
        }
        return -1;
    }


    @SideOnly(Side.CLIENT)
    public void setPickedItemStack(ItemStack stack) {
        int i = this.getSlotFor(stack);

        if (isHotbar(i)) {
            this.currentItem = i;
        } else {
            if (i == -1) {
                this.currentItem = this.getBestHotbarSlot();

                if (!this.mainInventory.get(this.currentItem).isEmpty()) {
                    int j = this.getFirstEmptyStack();

                    if (j != -1) {
                        this.mainInventory.set(j, this.mainInventory.get(this.currentItem));
                    }
                }
                this.mainInventory.set(this.currentItem, stack);
            } else {
                this.pickItem(i);
            }
        }
    }


    public void pickItem(int index) {
        this.currentItem = this.getBestHotbarSlot();
        ItemStack itemstack = this.mainInventory.get(this.currentItem);
        this.mainInventory.set(this.currentItem, this.mainInventory.get(index));
        this.mainInventory.set(index, itemstack);
    }


    public static boolean isHotbar(int index) {
        return index >= 0 && index < 9;
    }


    @SideOnly(Side.CLIENT)
    public int getSlotFor(ItemStack stack) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).isEmpty() && this.stackEqualExact(stack, this.mainInventory.get(i))) {
                return i;
            }
        }
        return -1;
    }


    public int findSlotMatchingUnusedItem(ItemStack p_194014_1_) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            ItemStack itemstack = this.mainInventory.get(i);
            if (!this.mainInventory.get(i).isEmpty() && this.stackEqualExact(p_194014_1_, this.mainInventory.get(i)) && !((ItemStack)this.mainInventory.get(i)).isItemDamaged() && !itemstack.isItemEnchanted() && !itemstack.hasDisplayName()) {
                return i;
            }
        }
        return -1;
    }


    public int getBestHotbarSlot() {
        for (int i = 0; i < 9; ++i) {
            int j = (this.currentItem + i) % 9;

            if (this.mainInventory.get(j).isEmpty()) return j;
        }

        for (int k = 0; k < 9; ++k) {
            int l = (this.currentItem + k) % 9;

            if (!this.mainInventory.get(l).isItemEnchanted()) return l;
        }
        return this.currentItem;
    }


    @SideOnly(Side.CLIENT)
    public void changeCurrentItem(int direction) {
        if (direction > 0) {
            direction = 1;
        }

        if (direction < 0) {
            direction = -1;
        }

        for (this.currentItem -= direction; this.currentItem < 0; this.currentItem += 9) {;}

        while (this.currentItem >= 9) {
            this.currentItem -= 9;
        }
    }


    public int clearMatchingItems(@Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT) {
        int i = 0;

        for (int j = 0; j < this.getSizeInventory(); ++j) {
            ItemStack itemstack = this.getStackInSlot(j);

            if (!itemstack.isEmpty() && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true))) {
                int k = removeCount <= 0 ? itemstack.getCount() : Math.min(removeCount - i, itemstack.getCount());
                i += k;

                if (removeCount != 0) {
                    itemstack.shrink(k);

                    if (itemstack.isEmpty()) {
                        this.setInventorySlotContents(j, ItemStack.EMPTY);
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }


        if (!this.itemStack.isEmpty()) {
            if (itemIn != null && this.itemStack.getItem() != itemIn) {
                return i;
            }

            if (metadataIn > -1 && this.itemStack.getMetadata() != metadataIn) {
                return i;
            }

            if (itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, this.itemStack.getTagCompound(), true)) {
                return i;
            }

            int l = removeCount <= 0 ? this.itemStack.getCount() : Math.min(removeCount - i, this.itemStack.getCount());
            i += l;

            if (removeCount != 0) {
                this.itemStack.shrink(l);

                if (this.itemStack.isEmpty()) {
                    this.itemStack = ItemStack.EMPTY;
                }
                if (removeCount > 0 && i >= removeCount) {
                    return i;
                }
            }
        }
        return i;
    }


    private int storePartialItemStack(ItemStack itemStackIn) {
        int i = this.storeItemStack(itemStackIn);

        if (i == -1) {
            i = this.getFirstEmptyStack();
        }
        return i == -1 ? itemStackIn.getCount() : this.addResource(i, itemStackIn);
    }


    private int addResource(int slot, ItemStack stack) {
        Item item = stack.getItem();
        int i = stack.getCount();
        ItemStack itemstack = this.getStackInSlot(slot);

        if (itemstack.isEmpty()) {
            itemstack = stack.copy(); // Forge: Replace Item clone above to preserve item capabilities when picking the item up.
            itemstack.setCount(0);

            if (stack.hasTagCompound())
            {
                itemstack.setTagCompound(stack.getTagCompound().copy());
            }

            this.setInventorySlotContents(slot, itemstack);
        }

        int j = i;

        if (i > itemstack.getMaxStackSize() - itemstack.getCount()) {
            j = itemstack.getMaxStackSize() - itemstack.getCount();
        }

        if (j > this.getInventoryStackLimit() - itemstack.getCount()) {
            j = this.getInventoryStackLimit() - itemstack.getCount();
        }

        if (j == 0) {
            return i;
        } else {
            i = i - j;
            itemstack.grow(j);
            itemstack.setAnimationsToGo(5);
            return i;
        }
    }


    public int storeItemStack(ItemStack itemStackIn) {
        if (this.canMergeStacks(this.getStackInSlot(this.currentItem), itemStackIn)) {
            return this.currentItem;
        } else if (this.canMergeStacks(this.getStackInSlot(40), itemStackIn)) {
            return 40;
        } else {
            for (int i = 0; i < this.mainInventory.size(); ++i) {
                if (this.canMergeStacks(this.mainInventory.get(i), itemStackIn)) {
                    return i;
                }
            }
            return -1;
        }
    }


    public void decrementAnimations() {
        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (!nonnulllist.get(i).isEmpty()) {
                    nonnulllist.get(i).updateAnimation(this.player.world, this.player, i, this.currentItem == i);
                }
            }
        }
        for (ItemStack is : armorInventory) // FORGE: Tick armor on animation ticks
        {
            if (!is.isEmpty()) {
                is.getItem().onArmorTick(player.world, player, is);
            }
        }
    }


    public boolean addItemStackToInventory(ItemStack itemStackIn) {
        return this.add(-1, itemStackIn);
    }


    public boolean add(int slot, final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            try {
                if (stack.isItemDamaged()) {
                    if (slot == -1) {
                        slot = this.getFirstEmptyStack();
                    }

                    if (slot >= 0) {
                        this.mainInventory.set(slot, stack.copy());
                        this.mainInventory.get(slot).setAnimationsToGo(5);
                        stack.setCount(0);
                        return true;
                    } else if (this.player.capabilities.isCreativeMode) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;

                    while (true) {
                        i = stack.getCount();

                        if (slot == -1) {
                            stack.setCount(this.storePartialItemStack(stack));
                        } else {
                            stack.setCount(this.addResource(slot, stack));
                        }

                        if (stack.isEmpty() || stack.getCount() >= i) {
                            break;
                        }
                    }

                    if (stack.getCount() == i && this.player.capabilities.isCreativeMode) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return stack.getCount() < i;
                    }
                }
            }

            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(stack.getItem()));
                crashreportcategory.addCrashSection("Item data", stack.getMetadata());
                crashreportcategory.addDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashreportcategory.addDetail("Item Class", () -> stack.getItem().getClass().getName());
                crashreportcategory.addDetail("Item name", stack::getDisplayName);
                ReportedException err = new ReportedException(crashreport);
                if(player != null && player.world != null && (!player.world.isRemote || SERVER_COMPILE)) {
                    player.world.getMinecraftServer().ticker().onError((WorldServer) player.world, err);
                    return false;
                }
                throw err;
            }
        }
    }


    public void placeItemBackInInventory(World world, ItemStack stack) {
        if (!world.isRemote || SERVER_COMPILE) {
            while (!stack.isEmpty()) {
                int i = this.storeItemStack(stack);

                if (i == -1) {
                    i = this.getFirstEmptyStack();
                }

                if (i == -1) {
                    this.player.dropItem(stack, false);
                    break;
                }

                int j = stack.getMaxStackSize() - this.getStackInSlot(i).getCount();

                if (this.add(i, stack.splitStack(j))) {
                    ((EntityPlayerMP)this.player).connection.netManager.sendPacketE(new SPacketSetSlot(-2, i, this.getStackInSlot(i)));
                }
            }
        }
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            if (index < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }
            index -= nonnulllist.size();
        }

        return list != null && !list.get(index).isEmpty() ? ItemStackHelper.getAndSplit(list, index, count) : ItemStack.EMPTY;
    }


    public void deleteStack(ItemStack stack) {
        for (NonNullList<ItemStack> list : this.allInventories) {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i) == stack) {
                    list.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        NonNullList<ItemStack> cur = null;

        for (NonNullList<ItemStack> inv : this.allInventories) {
            if (index < inv.size()) {
                cur = inv;
                break;
            }
            index -= inv.size();
        }

        if (cur != null && !cur.get(index).isEmpty()) {
            return cur.set(index, ItemStack.EMPTY);
//            ItemStack itemstack = cur.get(index);
//            cur.set(index, ItemStack.EMPTY);
//            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        NonNullList<ItemStack> cur = null;

        for (NonNullList<ItemStack> inv : this.allInventories) {
            if (index < inv.size()) {
                cur = inv;
                break;
            }

            index -= inv.size();
        }

        if (cur != null) cur.set(index, stack);
    }


    public float getDestroySpeed(IBlockState state) {
        float f = 1.0F;
        ItemStack item = this.mainInventory.get(this.currentItem);
        if (!item.isEmpty()) f *= item.getDestroySpeed(state);
        return f;
    }


    public NBTTagList writeToNBT(NBTTagList list) {
        ItemStack item;
        NBTTagCompound tag;
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            item = this.mainInventory.get(i);
            if (!item.isEmpty()) {
                tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                item.writeToNBT(tag);
                list.appendTag(tag);
            }
        }

        for (int i = 0; i < this.armorInventory.size(); ++i) {
            item = this.armorInventory.get(i);
            if (!item.isEmpty()) {
                tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)(i + 100));
                item.writeToNBT(tag);
                list.appendTag(tag);
            }
        }

        for (int i = 0; i < this.offHandInventory.size(); ++i) {
            item = this.offHandInventory.get(i);
            if (!item.isEmpty()) {
                tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)(i + 150));
                item.writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }


    public void readFromNBT(NBTTagList nbtTagListIn) {
        this.mainInventory.clear();
        this.armorInventory.clear();
        this.offHandInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty()) {
                if (j >= 0 && j < this.mainInventory.size()) {
                    this.mainInventory.set(j, itemstack);
                }
                else if (j >= 100 && j < this.armorInventory.size() + 100) {
                    this.armorInventory.set(j - 100, itemstack);
                }
                else if (j >= 150 && j < this.offHandInventory.size() + 150) {
                    this.offHandInventory.set(j - 150, itemstack);
                }
            }
        }
    }


    @Override
    public int getSizeInventory() {
        return this.mainInventory.size() + this.armorInventory.size() + this.offHandInventory.size();
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.mainInventory) {
            if (!itemstack.isEmpty()) return false;
        }

        for (ItemStack itemstack1 : this.armorInventory) {
            if (!itemstack1.isEmpty()) return false;
        }

        for (ItemStack itemstack2 : this.offHandInventory) {
            if (!itemstack2.isEmpty()) return false;
        }
        return true;
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> inv : this.allInventories) {
            if (index < inv.size()) {
                list = inv;
                break;
            }
            index -= inv.size();
        }
        return list == null ? ItemStack.EMPTY : list.get(index);
    }


    @Override
    public String getName() {
        return "container.inventory";
    }


    @Override
    public boolean hasCustomName() {
        return false;
    }


    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    public boolean canHarvestBlock(IBlockState state) {
        if (state.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            ItemStack itemstack = this.getStackInSlot(this.currentItem);
            return !itemstack.isEmpty() && itemstack.canHarvestBlock(state);
        }
    }


    @SideOnly(Side.CLIENT)
    public ItemStack armorItemInSlot(int slotIn) {
        return this.armorInventory.get(slotIn);
    }


    public void damageArmor(float damage) {
        damage = damage / 4.0F;

        if (damage < 1.0F) {
            damage = 1.0F;
        }

        for (int i = 0; i < this.armorInventory.size(); ++i) {
            ItemStack itemstack = this.armorInventory.get(i);

            if (itemstack.getItem() instanceof ItemArmor) {
                itemstack.damageItem((int)damage, this.player);
            }
        }
    }


    public void dropAllItems() {
        for (NonNullList<ItemStack> list : this.allInventories) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack prev = list.set(i, ItemStack.EMPTY);

                if (!prev.isEmpty()) {
                    this.player.dropItem(prev, true, false);
                    //list.set(i, ItemStack.EMPTY);
                }
            }
        }
    }


    public void markDirty() {
        ++this.timesChanged;
    }


    @SideOnly(Side.CLIENT)
    public int getTimesChanged() {
        return this.timesChanged;
    }


    public void setItemStack(ItemStack itemStackIn) {
        this.itemStack = itemStackIn;
    }


    public ItemStack getItemStack() {
        return this.itemStack;
    }


    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.player.isDead) {
            return false;
        } else {
            return player.getDistanceSq(this.player) <= 64.0D;
        }
    }


    public boolean hasItemStack(ItemStack itemStackIn) {
        label23:

        for (List<ItemStack> list : this.allInventories) {
            Iterator iterator = list.iterator();

            while (true) {
                if (!iterator.hasNext()) {
                    continue label23;
                }

                ItemStack itemstack = (ItemStack)iterator.next();

                if (!itemstack.isEmpty() && itemstack.isItemEqual(itemStackIn)) {
                    break;
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public void openInventory(EntityPlayer player) {}


    @Override
    public void closeInventory(EntityPlayer player) {}


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }


    public void copyInventory(InventoryPlayer playerInventory) {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            this.setInventorySlotContents(i, playerInventory.getStackInSlot(i));
        }
        this.currentItem = playerInventory.currentItem;
    }


    @Override
    public int getField(int id) {
        return 0;
    }


    @Override
    public void setField(int id, int value) {}


    @Override
    public int getFieldCount() {
        return 0;
    }


    @Override
    public void clear() {
        for (List<ItemStack> list : this.allInventories) list.clear();
    }


    public void fillStackedContents(RecipeItemHelper helper, boolean flag) {
        for (ItemStack itemstack : this.mainInventory) {
            helper.accountStack(itemstack);
        }

        if (flag) {
            helper.accountStack(this.offHandInventory.get(0));
        }
    }
}
