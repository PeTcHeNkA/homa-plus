package plus.common.mashine.capicator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import plus.common.block.SpBasicTE;
import plus.common.mashine.inv.IEnergyDependent;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.item.ItemCapacitor;
import javax.annotation.Nullable;


public class SpCapacitorTileEntity extends TileEntityLockableLoot implements ITickable, IEnergyFolder, IEnergyDependent {
    public final ItemCapacitor.Provider[] providers;
    public final ItemCapacitor.Provider inputEnrg = new ItemCapacitor.Provider();;
    public final ItemCapacitor.Provider outputEnrg = new ItemCapacitor.Provider();;

    public IEnergyFolder folder = null;
    private byte skipTicks = 0;

    public SpCapacitorTileEntity() {
        providers = new ItemCapacitor.Provider[4];
        for(int i=0;i<providers.length;i++){
            providers[i] = new ItemCapacitor.Provider();
        }
    }


    protected void tickEnergy(){
        if(folder == null){
            SpBasicTE.SCANNER.doScan(world, pos);
            if(folder == null){
                folder = IEnergyFolder.EMPTY;
                return;
            }
        }
        int maxTake = folder.getCurEnergy();
        if(maxTake==0)return;
        int delta = getMaxEnergy()-getCurEnergy();
        if(delta<=0)return;

        if(maxTake>=delta){
            folder.takeEnergy(delta);
            putEnergy(delta);
        } else {
            folder.takeEnergy(maxTake);
            putEnergy(maxTake);
        }
    }


    protected void tickChargeStation(){
        if(!inputEnrg.getItem().isEmpty()){
            moveEnergy(inputEnrg, 750);
        }
        if(!outputEnrg.getItem().isEmpty()){
            outputEnrg.moveEnergy(this, 750);
        }
    }


    @Override
    public void update() {
        if(skipTicks++>=21){
            skipTicks=0;
            tickEnergy();
            tickChargeStation();
        }
    }


    @Override
    public Container createContainer(InventoryPlayer playerInv, EntityPlayer player) {
        return new SpCapacitorContainer(playerInv, this);
    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_eng_cap";
    }


    @Override
    public void setEnergyFolder(IEnergyFolder folder) {
        this.folder = folder;
    }


    @Override
    public boolean takeEnergy(int amount) {
        return IEnergyFolder.takeMulti(providers, amount);
    }


    @Override
    public boolean putEnergy(int amount) {
        return IEnergyFolder.putMulti(providers, amount);
    }


    @Override
    public int getCurEnergy() {
        return IEnergyFolder.getCurMulti(providers);
    }


    @Override
    public int getMaxEnergy() {
        return IEnergyFolder.getMaxMulti(providers);
    }


    @Override
    protected NonNullList<ItemStack> getItems() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = getAndSplit(index, count);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }


    public ItemStack getAndSplit(int index, int amount) {
        if(index >= 0 && index < 6){
            ItemCapacitor.Provider prv;
            if(index==4) {
                prv = inputEnrg;
            } else if(index == 5){
                prv = outputEnrg;
            } else {
                prv = providers[index];
            }
            return !prv.getItem().isEmpty() && amount > 0 ?
                    prv.getItem().splitStack(amount) : ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
    }


    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemCapacitor.Provider prv;
        if(index==4){
            prv = inputEnrg;
        } else if (index==5) {
            prv = outputEnrg;
        } else {
            prv = providers[index];
        }
        ItemStack item = prv.getItem();
        prv.setItem(ItemStack.EMPTY);
        return item;
    }


    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        if(index==4) {
            inputEnrg.setItem(stack);
        } else if(index==5) {
            outputEnrg.setItem(stack);
        } else {
            providers[index].setItem(stack);
        }
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }


    @Override
    public int getSizeInventory() {
        return 6;
    }


    @Override
    public boolean isEmpty() {
        for (ItemCapacitor.Provider provider : providers) {
            if (!provider.getItem().isEmpty()) return false;
        }
        return inputEnrg.getItem().isEmpty() || outputEnrg.getItem().isEmpty();
    }


    @Override
    public ItemStack getStackInSlot(int index) {
        if(index==4) {
            return inputEnrg.getItem();
        } else if(index==5) {
            return outputEnrg.getItem();
        } else {
            return providers[index].getItem();
        }
    }


    @Override
    public void clear() {
        for (ItemCapacitor.Provider provider : providers) {
            provider.setItem(ItemStack.EMPTY);
        }
        inputEnrg.setItem(ItemStack.EMPTY);
        outputEnrg.setItem(ItemStack.EMPTY);
    }


    @Override
    public String getName() {
        return getGuiID();
    }






    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("CustomName", 8)) {
            this.customName = tag.getString("CustomName");
        }

        NBTTagList nbttaglist = tag.getTagList("Items", 10);
        for(int i=0;i<4;i++){
            providers[i].setItem(new ItemStack(nbttaglist.getCompoundTagAt(i)));
        }
        inputEnrg.setItem(new ItemStack(nbttaglist.getCompoundTagAt(4)));
        outputEnrg.setItem(new ItemStack(nbttaglist.getCompoundTagAt(5)));
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if (this.hasCustomName()) {
            tag.setString("CustomName", this.customName);
        }
        NBTTagList nbttaglist = new NBTTagList();

        for(int i=0;i<4;i++){
            nbttaglist.appendTag(providers[i].getItem().writeToNBT(new NBTTagCompound()));
        }
        nbttaglist.appendTag(inputEnrg.getItem().writeToNBT(new NBTTagCompound()));
        nbttaglist.appendTag(outputEnrg.getItem().writeToNBT(new NBTTagCompound()));

        tag.setTag("Items", nbttaglist);

        return super.writeToNBT(tag);
    }
}