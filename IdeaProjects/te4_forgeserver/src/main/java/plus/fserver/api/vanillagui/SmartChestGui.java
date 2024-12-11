package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import plus.tson.TsonFunc;


public class SmartChestGui extends ChestGui{
    private Int2ObjectMap<TsonFunc> slotFunctions = Int2ObjectMaps.emptyMap();
    private TsonFunc onClose;

    public SmartChestGui(int size) {
        super(size);
    }


    public void setFunctions(Int2ObjectMap<TsonFunc> funcs){
        slotFunctions = funcs;
    }


    public void setOnClose(TsonFunc onClose) {
        this.onClose = onClose;
    }


    @Override
    public void closeInventory(EntityPlayer player) {
        if(onClose != null)onClose.call(this, player);
    }


    @Override
    public ItemStack onClick(Container cont, int slotId, int dType, ClickType cType, EntityPlayer player) {
        TsonFunc func = slotFunctions.get(slotId);
        if(func == null)return null;
        ItemStack result;
        if(func.countArgs() == 1){
            result = ChestGui.canselAll(cont, slotId, dType, cType, player);
            try {
                func.call(player);
            } catch (Throwable e){
                e.printStackTrace();
            }
            return result;
        }
        try {
            result = (ItemStack)func.call(cont, slotId, dType, cType, player);
        } catch (Throwable e){
            e.printStackTrace();
            result = ChestGui.canselAll(cont, slotId, dType, cType, player);
        }
        return result;
    }
}