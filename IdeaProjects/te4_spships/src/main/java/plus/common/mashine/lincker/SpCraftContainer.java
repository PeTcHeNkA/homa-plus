package plus.common.mashine.lincker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.mashine.inv.CraftViewInv;
import plus.common.mashine.inv.IPowerContainer;
import plus.common.mashine.inv.PlayerExtendedBlockContainer;
import plus.common.mashine.item.ItemMssCapacitor;
import plus.common.mashine.item.ReactComponent;
import plus.common.mashine.item.ReactCore;
import plus.common.mashine.item.recept.*;
import plus.common.etc.ESlot;
import plus.common.etc.VSlot;
import plus.utls.IConstructorModify;

import java.util.function.Function;


public class SpCraftContainer extends PlayerExtendedBlockContainer implements ICraftMatrix, IPowerContainer, IConstructorModify<SpCraftContainer> {
    private final CraftViewInv craftResult;
    private int shift = 50;
    private int power = 1;

    public SpCraftContainer(IInventory playerInv, IInventory spCraftInv) {
        super(spCraftInv);
        this.craftResult = new CraftViewInv(spCraftInv);

        for (int i = 0; i < 5; ++i) {
            this.addSlotToContainer(
                    new ESlot(spCraftInv, i, 13 + i * 18, 47, this::onContentChanged)
            );
        }
        this.addSlotToContainer(new VSlot(craftResult, 0, 143, 47));
        addPlayerSlots(playerInv);
        power = spCraftInv.getField(0);
        onCallConstruct();
        onInit();
    }


    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, inventory);
    }


    @Override
    public void setPower(int power){
        this.power = power;
        onContentChanged();
    }


    @Override
    public int getPower(){
        return power;
    }


    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        power = data;
        onInit();
    }


    private void onContentChanged(){
        craftResult.setItem(SpReceptBook.INST.craftPreview(this, shift, power));
    }


    private void onInit(){
        craftResult.setItem(SpReceptBook.INST.craftPreview(this, 50, power));
    }


    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        if(slotId >= 10000){
            if(slotId >= 20000){
                shift = slotId - 20000;
                slotId = -999;
                onContentChanged();
            } else {
                slotId = slotId - 10000;
                shift = slotId / 10;
                slotId = slotId % 10;
            }
        }
        if(slotId == 5){
            if(SpReceptBook.INST.isValidCraft(this)){
                SpReceptBook.INST.spendItems(this);
            }
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        } else if(slotId < 5){
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        } else if(clickTypeIn == ClickType.PICKUP_ALL){
            if(SpReceptBook.INST.isValidCraft(this)){
                if(getInnStack(5).getItem() == player.inventory.getItemStack().getItem()) {
                    SpReceptBook.INST.spendItems(this);
                } else {
                    clickTypeIn = ClickType.PICKUP;
                }
            }
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }


    @Override
    public ItemStack[] getItems() {
        return new ItemStack[]{
                getInnStack(0),
                getInnStack(1),
                getInnStack(2),
                getInnStack(3),
                getInnStack(4),
        };
    }


    public ItemStack getInnStack(int i){
        return inventory.getStackInSlot(i);
    }


    @Override
    public int getSize() {
        return 5;
    }


    @Override
    public void takeItem(int index, int count) {
        ItemStack item = inventory.getStackInSlot(index);
        item.setCount(item.getCount() - count);
        inventory.setInventorySlotContents(index, item.isEmpty()?ItemStack.EMPTY:item);
    }
}


