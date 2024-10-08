package net.minecraft.tileentity;

import net.minecraft.item.*;
import net.minecraft.util.datafix.walkers.*;
import net.minecraft.util.datafix.*;
import net.minecraft.nbt.*;
import java.util.*;
import net.minecraft.entity.item.*;
import net.minecraftforge.items.*;
import javax.annotation.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;


public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable {
    private NonNullList<ItemStack> inventory;
    private int transferCooldown;
    private long tickedGameTime;

    public TileEntityHopper() {
        this.inventory = NonNullList.withSize(5, ItemStack.EMPTY);
        this.transferCooldown = -1;
    }


    public static void registerFixesHopper(final DataFixer fixer) {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityHopper.class, "Items"));
    }


    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.inventory);
        }
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
        this.transferCooldown = compound.getInteger("TransferCooldown");
    }


    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.inventory);
        }
        compound.setIntegerU("TransferCooldown", this.transferCooldown);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return compound;
    }


    @Override
    public int getSizeInventory() {
        return this.inventory.size();
    }


    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        this.fillWithLoot(null);
        final ItemStack itemstack = ItemStackHelper.getAndSplit((List)this.getItems(), index, count);
        return itemstack;
    }


    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        this.fillWithLoot(null);
        this.getItems().set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.hopper";
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public void update() {
        if (this.world != null && !this.world.isRemote) {
            --this.transferCooldown;
            this.tickedGameTime = this.world.getTotalWorldTime();
            if (!this.isOnTransferCooldown()) {
                this.setTransferCooldown(0);
                this.updateHopper();
            }
        }
    }


    protected boolean updateHopper() {
        if (this.world != null && !this.world.isRemote) {
            if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata())) {
                boolean flag = false;
                if (!this.isInventoryEmpty()) {
                    flag = this.transferItemsOut();
                }
                if (!this.isFull()) {
                    flag = (pullItems(this) || flag);
                }
                if (flag) {
                    this.setTransferCooldown(8);
                    this.markDirty();
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    private boolean isInventoryEmpty() {
        for (final ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean isEmpty() {
        return this.isInventoryEmpty();
    }


    private boolean isFull() {
        for (final ItemStack itemstack : this.inventory) {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }


    private boolean transferItemsOut() {
        if (VanillaInventoryCodeHooks.insertHook(this)) {
            return true;
        }
        final IInventory iinventory = this.getInventoryForHopperTransfer();
        if (iinventory == null) {
            return false;
        }
        final EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();
        if (this.isInventoryFull(iinventory, enumfacing)) {
            return false;
        }
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            if (!this.getStackInSlot(i).isEmpty()) {
                final ItemStack itemstack = this.getStackInSlot(i).copy();
                final ItemStack itemstack2 = putStackInInventoryAllSlots((IInventory)this, iinventory, this.decrStackSize(i, 1), enumfacing);
                if (itemstack2.isEmpty()) {
                    iinventory.markDirty();
                    return true;
                }
                this.setInventorySlotContents(i, itemstack);
            }
        }
        return false;
    }


    private boolean isInventoryFull(final IInventory inventoryIn, final EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            final ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            final int[] slotsForFace = isidedinventory.getSlotsForFace(side);
            for (final int k : slotsForFace) {
                final ItemStack itemstack1 = isidedinventory.getStackInSlot(k);
                if (itemstack1.isEmpty() || itemstack1.getCount() != itemstack1.getMaxStackSize()) {
                    return false;
                }
            }
        }
        else {
            for (int i = inventoryIn.getSizeInventory(), j = 0; j < i; ++j) {
                final ItemStack itemstack2 = inventoryIn.getStackInSlot(j);
                if (itemstack2.isEmpty() || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isInventoryEmpty(final IInventory inventoryIn, final EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            final ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            final int[] slotsForFace = isidedinventory.getSlotsForFace(side);
            for (final int i : slotsForFace) {
                if (!isidedinventory.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
        } else {
            for (int j = inventoryIn.getSizeInventory(), k = 0; k < j; ++k) {
                if (!inventoryIn.getStackInSlot(k).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean pullItems(final IHopper hopper) {
        final Boolean ret = VanillaInventoryCodeHooks.extractHook(hopper);
        if (ret != null) {
            return ret;
        }
        final IInventory iinventory = getSourceInventory(hopper);
        if (iinventory != null) {
            final EnumFacing enumfacing = EnumFacing.DOWN;
            if (isInventoryEmpty(iinventory, enumfacing)) {
                return false;
            }
            if (iinventory instanceof ISidedInventory) {
                final ISidedInventory isidedinventory = (ISidedInventory)iinventory;
                final int[] slotsForFace = isidedinventory.getSlotsForFace(enumfacing);
                for (final int i : slotsForFace) {
                    if (pullItemFromSlot(hopper, iinventory, i, enumfacing)) {
                        return true;
                    }
                }
            }
            else {
                for (int j = iinventory.getSizeInventory(), k = 0; k < j; ++k) {
                    if (pullItemFromSlot(hopper, iinventory, k, enumfacing)) {
                        return true;
                    }
                }
            }
        }
        else {
            for (final EntityItem entityitem : getCaptureItems(hopper.getWorld(), hopper.getXPos(), hopper.getYPos(), hopper.getZPos())) {
                if (putDropInInventoryAllSlots(null, (IInventory)hopper, entityitem)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static boolean pullItemFromSlot(final IHopper hopper, final IInventory inventoryIn, final int index, final EnumFacing direction) {
        final ItemStack itemstack = inventoryIn.getStackInSlot(index);
        if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            final ItemStack copy = itemstack.copy();
            final ItemStack itemstack3 = putStackInInventoryAllSlots(inventoryIn, hopper, inventoryIn.decrStackSize(index, 1), null);
            if (itemstack3.isEmpty()) {
                inventoryIn.markDirty();
                return true;
            }
            inventoryIn.setInventorySlotContents(index, copy);
        }
        return false;
    }


    public static boolean putDropInInventoryAllSlots(final IInventory src, final IInventory desc, final EntityItem entity) {
        boolean flag = false;
        if (entity == null) {
            return false;
        }
        final ItemStack itemstack = entity.getItem().copy();
        final ItemStack itemstack2 = putStackInInventoryAllSlots(src, desc, itemstack, null);
        if (itemstack2.isEmpty()) {
            flag = true;
            entity.setDead();
        }
        else {
            entity.setItem(itemstack2);
        }
        return flag;
    }


    @Override
    protected IItemHandler createUnSidedHandler() {
        return new VanillaHopperItemHandler(this);
    }


    public static ItemStack putStackInInventoryAllSlots(final IInventory src, final IInventory dest, ItemStack stack, @Nullable final EnumFacing dir) {
        if (dest instanceof ISidedInventory && dir != null) {
            final ISidedInventory isidedinventory = (ISidedInventory)dest;
            final int[] aint = isidedinventory.getSlotsForFace(dir);
            for (int k = 0; k < aint.length && !stack.isEmpty(); stack = insertStack(src, dest, stack, aint[k], dir), ++k) {}
        }
        else {
            for (int i = dest.getSizeInventory(), j = 0; j < i && !stack.isEmpty(); stack = insertStack(src, dest, stack, j, dir), ++j) {}
        }
        return stack;
    }


    private static boolean canInsertItemInSlot(final IInventory inventoryIn, final ItemStack stack, final int index, final EnumFacing side) {
        return inventoryIn.isItemValidForSlot(index, stack) && (!(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side));
    }


    private static boolean canExtractItemFromSlot(final IInventory inventoryIn, final ItemStack stack, final int index, final EnumFacing side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }


    private static ItemStack insertStack(final IInventory src, final IInventory dest, ItemStack stack, final int index, final EnumFacing dir) {
        final ItemStack itemstack = dest.getStackInSlot(index);
        if (canInsertItemInSlot(dest, stack, index, dir)) {
            boolean flag = false;
            final boolean flag2 = dest.isEmpty();
            if (itemstack.isEmpty()) {
                dest.setInventorySlotContents(index, stack);
                stack = ItemStack.EMPTY;
                flag = true;
            }
            else if (canCombine(itemstack, stack)) {
                final int i = stack.getMaxStackSize() - itemstack.getCount();
                final int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemstack.grow(j);
                flag = (j > 0);
            }
            if (flag) {
                if (flag2 && dest instanceof TileEntityHopper) {
                    final TileEntityHopper hopper = (TileEntityHopper)dest;
                    if (!hopper.mayTransfer()) {
                        int k = 0;
                        if (src != null && src instanceof TileEntityHopper) {
                            final TileEntityHopper tileentityhopper2 = (TileEntityHopper)src;
                            if (hopper.tickedGameTime >= tileentityhopper2.tickedGameTime) {
                                k = 1;
                            }
                        }
                        hopper.setTransferCooldown(8 - k);
                    }
                }
                dest.markDirty();
            }
        }
        return stack;
    }


    private IInventory getInventoryForHopperTransfer() {
        final EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
        return getInventoryAtPosition(this.getWorld(), this.getXPos() + enumfacing.getXOffset(), this.getYPos() + enumfacing.getYOffset(), this.getZPos() + enumfacing.getZOffset());
    }


    public static IInventory getSourceInventory(final IHopper hopper) {
        return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0, hopper.getZPos());
    }


    public static List<EntityItem> getCaptureItems(final World worldIn, final double x, final double y, final double z) {
        return worldIn.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 1.5, z + 0.5), EntitySelectors.IS_ALIVE);
    }


    public static IInventory getInventoryAtPosition(final World world, final double x, final double y, final double z) {
        IInventory iinventory = null;
        final int bx = MathHelper.floor(x);
        final int by = MathHelper.floor(y);
        final int bz = MathHelper.floor(z);
        final IBlockState state = world.access().getBlockState(bx, by, bz);
        if (state.hasTile()) {
            final BlockPos blockpos = new BlockPos(bx, by, bz);
            final TileEntity tileentity = world.getTileEntity(blockpos);
            if (tileentity instanceof IInventory) {
                final Block block = state.getBlock();
                iinventory = (IInventory)tileentity;
                if (iinventory instanceof TileEntityChest && block instanceof BlockChest) {
                    iinventory = ((BlockChest)block).getContainer(world, blockpos, true);
                }
            }
        }
        if (iinventory == null) {
            final List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntitySelectors.HAS_INVENTORY);
            if (!list.isEmpty()) {
                iinventory = (IInventory)list.get(world.rand.nextInt(list.size()));
            }
        }
        return iinventory;
    }


    private static boolean canCombine(final ItemStack stack1, final ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && stack1.getMetadata() == stack2.getMetadata() && stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }


    @Override
    public double getXPos() {
        return this.pos.getX() + 0.5;
    }


    @Override
    public double getYPos() {
        return this.pos.getY() + 0.5;
    }


    @Override
    public double getZPos() {
        return this.pos.getZ() + 0.5;
    }


    public void setTransferCooldown(final int ticks) {
        this.transferCooldown = ticks;
    }


    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }


    public boolean mayTransfer() {
        return this.transferCooldown > 8;
    }


    @Override
    public String getGuiID() {
        return "minecraft:hopper";
    }


    @Override
    public Container createContainer(final InventoryPlayer playerInventory, final EntityPlayer playerIn) {
        this.fillWithLoot(playerIn);
        return new ContainerHopper(playerInventory, this, playerIn);
    }


    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }


    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }
}