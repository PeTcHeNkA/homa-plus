package net.minecraft.inventory;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.collections.ArrayListE2;
import plus.fserver.utls.collections.ArrayListEV;
import plus.tson.utl.Te4HashSet;


public abstract class Container {
    public NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    public List<Slot> inventorySlots                  = new ArrayListEV<>(9);
    public int windowId;
    @SideOnly(Side.CLIENT)
    private short transactionID;
    private int dragMode = -1;
    private byte dragEvent;
    private final Set<Slot> dragSlots            = new Te4HashSet<>(7);
    protected List<IContainerListener> listeners = new ArrayListE2<>();
    private final Set<EntityPlayer>   playerList = new Te4HashSet<>(3);

    protected Slot addSlotToContainer(Slot slotIn) {
        List<Slot> slots;
        slotIn.setSlotNum((slots = this.inventorySlots).size());
        slots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
        return slotIn;
    }


    public void addListener(IContainerListener listener) {
        if (this.listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.listeners.add(listener);
            listener.sendAllContents(this, this.getInventory());
            this.detectAndSendChanges();
        }
    }


    public NonNullList<ItemStack> getInventory() {
        List<Slot> slots = this.inventorySlots;
        NonNullList<ItemStack> inv = NonNullList.withSize(slots.size(), ItemStack.EMPTY);
        for (int i = 0, s = slots.size(); i < s; ++i) {
            inv.set(i, slots.get(i).getStack());
        }
        return inv;
    }


    @SideOnly(Side.CLIENT)
    public void removeListener(IContainerListener listener) {
        this.listeners.remove(listener);
    }


    public void detectAndSendChanges() {
        List<IContainerListener> listeners = this.listeners;
        List<Slot> slots = this.inventorySlots;
        NonNullList<ItemStack> stacks = this.inventoryItemStacks;

        for (int i = 0, s = slots.size(); i < s; ++i) {
            ItemStack item1 = slots.get(i).getStack();
            ItemStack item2 = stacks.get(i);

            if (!ItemStack.areItemStacksEqual(item2, item1)) {
                boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(item2, item1);
                item2 = item1.isEmpty() ? ItemStack.EMPTY : item1.copy();
                stacks.set(i, item2);

                if (clientStackChanged) {

                    for (int j = 0; j < listeners.size(); ++j) {
                        listeners.get(j).sendSlotContents(this, i, item2);
                    }
                }
            }
        }
    }


    public boolean enchantItem(EntityPlayer playerIn, int id) {
        return false;
    }


    @Nullable
    public Slot getSlotFromInventory(IInventory inv, int slotIn) {
        List<Slot> slots = this.inventorySlots;
        for (int i = 0, s = slots.size(); i < s; ++i) {
            Slot slot;
            if ((slot = slots.get(i)).isHere(inv, slotIn)) return slot;
        }
        return null;
    }


    public Slot getSlot(int slotId) {
        return this.inventorySlots.get(slotId);
    }


    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);
        return slot != null ? slot.getStack() : ItemStack.EMPTY;
    }


    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack itemstack = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;
        List<Slot> inventorySlots = this.inventorySlots;

        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            int j1 = this.dragEvent;
            this.dragEvent = (byte) getDragEvent(dragType);

            if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
                this.resetDrag();
            } else if (inventoryplayer.getItemStack().isEmpty()) {
                this.resetDrag();
            } else if (this.dragEvent == 0) {
                this.dragMode = extractDragMode(dragType);

                if (isValidDragMode(this.dragMode, player)) {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                } else {
                    this.resetDrag();
                }
            } else if (this.dragEvent == 1) {
                Slot slot7 = inventorySlots.get(slotId);
                ItemStack itemstack12 = inventoryplayer.getItemStack();

                if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7)) {
                    this.dragSlots.add(slot7);
                }
            } else if (this.dragEvent == 2) {
                if (!this.dragSlots.isEmpty()) {
                    ItemStack itemstack9 = inventoryplayer.getItemStack().copy();
                    int k1 = inventoryplayer.getItemStack().getCount();

                    for (Slot slot8 : this.dragSlots) {
                        ItemStack itemstack13 = inventoryplayer.getItemStack();

                        if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
                            ItemStack itemstack14 = itemstack9.copy();
                            int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
                            computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                            int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));

                            if (itemstack14.getCount() > k3)
                            {
                                itemstack14.setCount(k3);
                            }

                            k1 -= itemstack14.getCount() - j3;
                            slot8.putStack(itemstack14);
                        }
                    }

                    itemstack9.setCount(k1);
                    inventoryplayer.setItemStack(itemstack9);
                }

                this.resetDrag();
            } else {
                this.resetDrag();
            }
        } else if (this.dragEvent != 0) {
            this.resetDrag();
        } else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            if (slotId == -999) {
                if (!inventoryplayer.getItemStack().isEmpty()) {
                    if (dragType == 0) {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (dragType == 1) {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
            } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot5 = inventorySlots.get(slotId);

                if (slot5 == null || !slot5.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }

                for (ItemStack item7 = this.transferStackInSlot(player, slotId); !item7.isEmpty()
                        && ItemStack.areItemsEqual(slot5.getStack(), item7); item7 = this.transferStackInSlot(player, slotId)) {
                    itemstack = item7.copy();
                }
            } else {
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot6 = inventorySlots.get(slotId);

                if (slot6 != null) {
                    ItemStack item8 = slot6.getStack();
                    ItemStack item11 = inventoryplayer.getItemStack();

                    if (!item8.isEmpty()) {
                        itemstack = item8.copy();
                    }

                    if (item8.isEmpty()) {
                        if (!item11.isEmpty() && slot6.isItemValid(item11)) {
                            int i3 = dragType == 0 ? item11.getCount() : 1;

                            if (i3 > slot6.getItemStackLimit(item11)) {
                                i3 = slot6.getItemStackLimit(item11);
                            }

                            slot6.putStack(item11.splitStack(i3));
                        }
                    } else if (slot6.canTakeStack(player)) {
                        if (item11.isEmpty()) {
                            if (item8.isEmpty()) {
                                slot6.putStack(ItemStack.EMPTY);
                                inventoryplayer.setItemStack(ItemStack.EMPTY);
                            } else {
                                int l2 = dragType == 0 ? item8.getCount() : (item8.getCount() + 1) / 2;
                                inventoryplayer.setItemStack(slot6.decrStackSize(l2));

                                if (item8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        } else if (slot6.isItemValid(item11)) {
                            if (item8.getItem() == item11.getItem() && item8.getMetadata() == item11.getMetadata() && ItemStack.areItemStackTagsEqual(item8, item11)) {
                                int k2 = dragType == 0 ? item11.getCount() : 1;

                                if (k2 > slot6.getItemStackLimit(item11) - item8.getCount()) {
                                    k2 = slot6.getItemStackLimit(item11) - item8.getCount();
                                }

                                if (k2 > item11.getMaxStackSize() - item8.getCount()) {
                                    k2 = item11.getMaxStackSize() - item8.getCount();
                                }

                                item11.shrink(k2);
                                item8.grow(k2);
                            } else if (item11.getCount() <= slot6.getItemStackLimit(item11)) {
                                slot6.putStack(item11);
                                inventoryplayer.setItemStack(item8);
                            }
                        } else if (item8.getItem() == item11.getItem() && item11.getMaxStackSize() > 1 && (!item8.getHasSubtypes() || item8.getMetadata() == item11.getMetadata()) && ItemStack.areItemStackTagsEqual(item8, item11) && !item8.isEmpty()) {
                            int j2 = item8.getCount();

                            if (j2 + item11.getCount() <= item11.getMaxStackSize()) {
                                item11.grow(j2);
                                item8 = slot6.decrStackSize(j2);

                                if (item8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                    }
                    slot6.onSlotChanged();
                }
            }
        } else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            Slot slot4 = inventorySlots.get(slotId);
            ItemStack itemstack6 = inventoryplayer.getStackInSlot(dragType);
            ItemStack itemstack10 = slot4.getStack();

            if (!itemstack6.isEmpty() || !itemstack10.isEmpty()) {
                if (itemstack6.isEmpty()) {
                    if (slot4.canTakeStack(player)) {
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot4.onSwapCraft(itemstack10.getCount());
                        slot4.putStack(ItemStack.EMPTY);
                        slot4.onTake(player, itemstack10);
                    }
                } else if (itemstack10.isEmpty()) {
                    if (slot4.isItemValid(itemstack6)) {
                        int l1 = slot4.getItemStackLimit(itemstack6);

                        if (itemstack6.getCount() > l1) {
                            slot4.putStack(itemstack6.splitStack(l1));
                        } else {
                            slot4.putStack(itemstack6);
                            inventoryplayer.setInventorySlotContents(dragType, ItemStack.EMPTY);
                        }
                    }
                }
                else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6)) {
                    int i2 = slot4.getItemStackLimit(itemstack6);

                    if (itemstack6.getCount() > i2) {
                        slot4.putStack(itemstack6.splitStack(i2));
                        slot4.onTake(player, itemstack10);

                        if (!inventoryplayer.addItemStackToInventory(itemstack10)) {
                            player.dropItem(itemstack10, true);
                        }
                    } else {
                        slot4.putStack(itemstack6);
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot4.onTake(player, itemstack10);
                    }
                }
            }
        } else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && slotId >= 0) {
            Slot slot3 = inventorySlots.get(slotId);

            if (slot3 != null && slot3.getHasStack()) {
                ItemStack itemstack5 = slot3.getStack().copy();
                itemstack5.setCount(itemstack5.getMaxStackSize());
                inventoryplayer.setItemStack(itemstack5);
            }
        } else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && slotId >= 0) {
            Slot slot2 = inventorySlots.get(slotId);

            if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player)) {
                ItemStack itemstack4 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().getCount());
                slot2.onTake(player, itemstack4);
                player.dropItem(itemstack4, true);
            }
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                int i = dragType == 0 ? 0 : inventorySlots.size() - 1;
                int j = dragType == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = inventorySlots.get(l);

                        if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack1, slot1)) {
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.grow(i1);

                                if (itemstack3.isEmpty()) {
                                    slot1.putStack(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }
            this.detectAndSendChanges();
        }
        return itemstack;
    }


    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return true;
    }


    public void onContainerClosed(EntityPlayer playerIn) {
        InventoryPlayer inventoryplayer = playerIn.inventory;
        if (!inventoryplayer.getItemStack().isEmpty()) {
            playerIn.dropItem(inventoryplayer.getItemStack(), false);
            inventoryplayer.setItemStack(ItemStack.EMPTY);
        }
    }


    protected void clearContainer(EntityPlayer playerIn, World worldIn, IInventory inventoryIn) {
        if (!playerIn.isEntityAlive() || playerIn instanceof EntityPlayerMP && ((EntityPlayerMP)playerIn).hasDisconnected()) {
            for (int j = 0; j < inventoryIn.getSizeInventory(); ++j) {
                playerIn.dropItem(inventoryIn.removeStackFromSlot(j), false);
            }
        } else {
            for (int i = 0; i < inventoryIn.getSizeInventory(); ++i) {
                playerIn.inventory.placeItemBackInInventory(worldIn, inventoryIn.removeStackFromSlot(i));
            }
        }
    }


    public void onCraftMatrixChanged(IInventory inventoryIn) {
        this.detectAndSendChanges();
    }


    public void putStackInSlot(int slotID, ItemStack stack) {
        this.getSlot(slotID).putStack(stack);
    }


    @SideOnly(Side.CLIENT)
    public void setAll(List<ItemStack> list) {
        for (int i = 0; i < list.size(); ++i) {
            this.getSlot(i).putStack(list.get(i));
        }
    }


    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {}


    @SideOnly(Side.CLIENT)
    public short getNextTransactionID(InventoryPlayer invPlayer) {
        ++this.transactionID;
        return this.transactionID;
    }


    public boolean getCanCraft(EntityPlayer player) {
        return !this.playerList.contains(player);
    }


    public void setCanCraft(EntityPlayer player, boolean canCraft) {
        if (canCraft) {
            this.playerList.remove(player);
        } else {
            this.playerList.add(player);
        }
    }


    public abstract boolean canInteractWith(EntityPlayer playerIn);


    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stack.splitStack(stack.getCount()));
                    }
                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return flag;
    }


    public static int extractDragMode(int eventButton) {
        return eventButton >> 2 & 3;
    }


    public static int getDragEvent(int clickedButton) {
        return clickedButton & 3;
    }


    @SideOnly(Side.CLIENT)
    public static int getQuickcraftMask(int num1, int num2) {
        return num1 & 3 | (num2 & 3) << 2;
    }


    public static boolean isValidDragMode(int dragModeIn, EntityPlayer player) {
        return dragModeIn == 0 || dragModeIn == 1 || dragModeIn == 2 && player.capabilities.isCreativeMode;
    }


    protected void resetDrag() {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }


    public static boolean canAddItemToSlot(@Nullable Slot slotIn, ItemStack stack, boolean stackSizeMatters) {
        boolean flag = slotIn == null || !slotIn.getHasStack();

        if (!flag && stack.isItemEqual(slotIn.getStack()) && ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack)) {
            return slotIn.getStack().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= stack.getMaxStackSize();
        } else {
            return flag;
        }
    }


    public static void computeStackSize(Set<Slot> dragSlotsIn, int dragModeIn, ItemStack stack, int slotStackSize) {
        switch (dragModeIn) {
            case 0:
                stack.setCount(MathHelper.floor((float)stack.getCount() / (float)dragSlotsIn.size()));
                break;
            case 1:
                stack.setCount(1);
                break;
            case 2:
                stack.setCount(stack.getMaxStackSize());
        }
        stack.grow(slotStackSize);
    }


    public boolean canDragIntoSlot(Slot slotIn) {
        return true;
    }


    public static int calcRedstone(@Nullable TileEntity te) {
        return te instanceof IInventory ? calcRedstoneFromInventory((IInventory)te) : 0;
    }


    public static int calcRedstoneFromInventory(@Nullable IInventory inv) {
        if (inv == null) {
            return 0;
        }
        else {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < inv.getSizeInventory(); ++j) {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (!itemstack.isEmpty()) {
                    f += (float)itemstack.getCount() / (float)Math.min(inv.getInventoryStackLimit(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f = f / (float)inv.getSizeInventory();
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }


    protected void slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting inv, InventoryCraftResult result) {
        if (!world.isRemote) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)player;
            ItemStack itemstack = ItemStack.EMPTY;
            IRecipe irecipe = CraftingManager.findMatchingRecipe(inv, world);

            if (irecipe != null && (irecipe.isDynamic() || !world.getGameRules().doLimitedCrafting.getBoolean() || entityplayermp.getRecipeBook().isUnlocked(irecipe))) {
                result.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(inv);
            }
            result.setInventorySlotContents(0, itemstack);
            entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
        }
    }
}