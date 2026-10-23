package plus.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.world.World;
import plus.invlib.slot.*;
import javax.annotation.Nullable;


public class AInventoryContainer extends Container {

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public InventoryCraftResult craftResult = new InventoryCraftResult();

    public static final int[] syncIndexes = new int[]{9};
    public final EntityPlayer player;
    private int craftResultIndex;
    public AInventoryContainer(InventoryPlayer playerInventory, AInventory cInventory, EntityPlayer player) {
        this.player = player;
        initHotBarSlots(playerInventory);
        initEquipSlots(playerInventory, player, AInventory.xSlots*18+9,18*2);

        this.addSlotToContainer(
                SynchronizedRenderableSlot.build(playerInventory,9,
                        (AInventory.xSlots)*18+9,18*4,player)
        );

        initCraftMatrix(player);
        initInvSlots(playerInventory, cInventory, player);
    }


    private void initCraftMatrix(EntityPlayer player){
        craftResultIndex = this.inventorySlots.size();
        this.addSlotToContainer(new
                SSlotCrafting(player,
                this.craftMatrix, this.craftResult, craftResultIndex, 45, 9));

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.addSlotToContainer(
                        new SSlot(craftMatrix, j + i * 2, j * 18, i * 18)
                );
            }
        }
    }


    private void initEquipSlots(InventoryPlayer playerInventory, EntityPlayer player, int xPos, int yPos){
        this.addSlotToContainer(new EquipmentSlotProxy(
                player, playerInventory,39,xPos,yPos,EntityEquipmentSlot.HEAD, 3)
        );
        this.addSlotToContainer(new EquipmentSlotProxy(
                player, playerInventory,38,xPos,yPos+18,EntityEquipmentSlot.CHEST, 2)
        );
        this.addSlotToContainer(new EquipmentSlotProxy(
                player, playerInventory,37,xPos,yPos+18*3,EntityEquipmentSlot.LEGS, 1)
        );
        this.addSlotToContainer(new EquipmentSlotProxy(
                player, playerInventory,36,xPos,yPos+18*4,EntityEquipmentSlot.FEET, 0)
        );
        this.addSlotToContainer(
                new SSlot(playerInventory, 40, xPos, yPos+18*5)
        );
    }


    private void initHotBarSlots(InventoryPlayer playerInventory){
        for(int i=0;i<9;i++){
            this.addSlotToContainer(new SSlot(playerInventory, i, i * 18, 0));
        }
    }


    private Slot buildSlot(InventoryPlayer playerInventory, AInventory cInventory,
                           EntityPlayer player, int index, int xPosition, int yPosition){
        if(index>=36){
            return new StandartSlotProxy(player, cInventory, index-36,xPosition,yPosition);
        } else return new SSlot(playerInventory, index, xPosition, yPosition);
    }


    private void initInvSlots(InventoryPlayer playerInventory, AInventory cInventory,
                              EntityPlayer player){
        int i = 10;
        for(int iY = 0; iY< AInventory.ySlots; iY++){
            int iY18 = iY * 18;
            for(int iX = 0; iX< AInventory.xSlots; iX++){
                this.addSlotToContainer(buildSlot(
                        playerInventory, cInventory, player,
                        i, iX * 18, iY18)
                );
                i+=1;
            }
        }
    }


    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }


    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        if(slotId<=0)return super.slotClick(slotId, dragType, clickTypeIn, player);
        Slot slot0 = getSlot(slotId);
        if(slot0 instanceof SynchronizedRenderableSlot){
            ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
            ((SynchronizedRenderableSlot) slot0).onSynchronizedSlotChange(player);
            return result;
        } else return super.slotClick(slotId, dragType, clickTypeIn, player);
    }


    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
//        if(index==13){
//            return ItemStack.EMPTY;
//        }
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()){
            ItemStack itemstack1 = slot.getStack();

            ItemStack itemstack = itemstack1.copy();
            if (index > 9){
                if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else {
                if (!this.mergeItemStack(itemstack1, 14, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            slot.onTake(playerIn, itemstack1);
        }

        return ItemStack.EMPTY;
    }


    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        slotChangedCraftingGrid(player.world, player, craftMatrix, craftResult);
    }


    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.craftResult.clear();

        if (!playerIn.world.isRemote) {
            this.clearContainer(playerIn, playerIn.world, this.craftMatrix);
        }
    }


    @Override
    protected void slotChangedCraftingGrid(World world, EntityPlayer player,
                                           InventoryCrafting inventoryCrafting,
                                           InventoryCraftResult result) {
        if (!world.isRemote) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)player;
            ItemStack itemstack = ItemStack.EMPTY;
            IRecipe irecipe = CraftingManager.findMatchingRecipe(inventoryCrafting, world);

            if (irecipe != null && (irecipe.isDynamic() ||
                    !world.getGameRules().getBoolean("doLimitedCrafting")
                    || entityplayermp.getRecipeBook().isUnlocked(irecipe))) {
                result.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(inventoryCrafting);
            }

            result.setInventorySlotContents(craftResultIndex, itemstack);
            entityplayermp.connection.sendPacket(
                    new SPacketSetSlot(this.windowId, craftResultIndex, itemstack));
        }
    }
}