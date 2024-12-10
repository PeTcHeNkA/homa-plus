package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class SmartChestGuiTemplate extends ChestGuiTemplate{
    private final Int2ObjectOpenHashMap<TsonFunc> slotFunctions = new Int2ObjectOpenHashMap<>();
    private TsonFunc onClose;

    public SmartChestGuiTemplate(TsonMap map) {
        super(map);

        if(map.containsKey("default-action")){
            slotFunctions.defaultReturnValue(map.getFunc("default-action"));
        }

        if(map.containsKey("actions")) {
            TsonList lActs = map.getList("actions");

            for (TsonObj obj : lActs) {
                TsonMap map1 = obj.getMap();
                slotFunctions.put(map1.getInt("slot"), map.getFunc("func"));
            }
        }

        if(map.containsKey("on-close")){
            onClose = map.getFunc("on-close");
        }
    }


    @Override
    public ChestGui create(){
        SmartChestGui chest = new SmartChestGui(rows());
        chest.fillOf(this);
        chest.setCustomName(getCustomName());
        chest.setFunctions(functions());
        chest.setOnClose(onClose);
        return chest;
    }


    public Int2ObjectMap<TsonFunc> functions(){
        return slotFunctions;
    }


    public static ItemStack canselAll(ChestGui.VrContainer cont, Integer slotId, Integer dType, ClickType cType, EntityPlayer player){
        if(cType == ClickType.PICKUP || cType == ClickType.PICKUP_ALL)
            return player.inventory.getItemStack();
        if(slotId < 0)return null;
        if(slotId >= cont.inventorySlots.size())return ItemStack.EMPTY;
        return cont.getSlot(slotId).getStack();
    }


    public static ItemStack canselAllSmart(ChestGui.VrContainer cont, Integer slotId, Integer dType, ClickType cType, EntityPlayer player){
        if(slotId < 0)return null;
        if(slotId >= cont.inventorySlots.size())return null;

        Slot slot = cont.getSlot(slotId);
        if(slot.inventory == cont.gui()){
            if(cType == ClickType.PICKUP || cType == ClickType.PICKUP_ALL) {
                return player.inventory.getItemStack();
            }
            return slot.getStack();
        }
        return null;
    }
}