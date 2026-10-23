package plus.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import plus.invlib.cap.CAPInventoryProvider;
import plus.invlib.cap.ICAPInventory;

import java.util.HashMap;


public final class AllPlayerInventoryProxy {
    public static AllPlayerInventoryProxy INST = new AllPlayerInventoryProxy();

    private final HashMap<Integer, Group> indexes = new HashMap<>();

    private AllPlayerInventoryProxy(){
        addHotBarSlots();
        addEquipSlots();

        addSlot(new Group(false, 9));

        addLeftSlots();
    }

    private int curIndex(){
        return indexes.size();
    }


    private void addHotBarSlots(){
        for(int i=0;i<9;i++){
            addSlot(buildGroup(i));
        }
    }


    private void addLeftSlots(){
        int i = 10;
        for(int iY = 0; iY< AInventory.ySlots; iY++){
            for(int iX = 0; iX< AInventory.xSlots; iX++){
                this.addSlot(buildGroup(i));
                i+=1;
            }
        }
    }


    private void addEquipSlots(){
        addSlot(new Group(true, 39));//9 8 7 6  40
        addSlot(new Group(true, 38));
        addSlot(new Group(true, 37));
        addSlot(new Group(true, 36));
        addSlot(new Group(true, 40));
    }


    private void addSlot(Group g){
        indexes.put(curIndex(), g);
    }


    private Group buildGroup(final int index){
        if(index>=36)return new Group(false, index - 36);
        return new Group(true, index);
    }


    public ItemStack getStackInSlot(EntityPlayer player, int index){
        Group group = indexes.get(index);
        if(group.selfInv){
            return player.inventory.getStackInSlot(index);
        } else {
            ICAPInventory inv = player.getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
            if(inv==null)return ItemStack.EMPTY;
            return inv.getInventory().getStackInSlot(index);
        }
    }


    public Group getRealSlotIndex(int index){
        return indexes.get(index);
    }


    public static final class Group{
        public final boolean selfInv;
        public final int index;

        Group(boolean selfInv, int index) {
            this.selfInv = selfInv;
            this.index = index;
        }
    }
}