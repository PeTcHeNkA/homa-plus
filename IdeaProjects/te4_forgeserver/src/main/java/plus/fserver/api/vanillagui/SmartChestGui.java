package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import plus.tson.TsonFunc;


public class SmartChestGui extends ChestGui{
    private Int2ObjectMap<TsonFunc> slotFunctions = Int2ObjectMaps.emptyMap();
    private TsonFunc onClose;

    public SmartChestGui(int rows) {
        super(rows);
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
    public ItemStack onClick(VrContainer container, int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        TsonFunc func = slotFunctions.get(slotId);
        if(func == null)return null;
        return (ItemStack)func.call(container, slotId, dragType, clickTypeIn, player);
    }
}
