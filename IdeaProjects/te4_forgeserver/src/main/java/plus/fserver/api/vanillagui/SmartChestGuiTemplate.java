package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
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

        TsonList lActs = map.getList("actions");

        for (TsonObj obj:lActs){
            TsonMap map1 = obj.getMap();
            slotFunctions.put(map1.getInt("slot"), map.getFunc("func"));
        }

        if(map.containsKey("on-close")){
            onClose = map.getFunc("on-close");
        }
    }


    @Override
    public ChestGui create(){
        if(functions().isEmpty()){
            return super.create();
        }
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


    public static ItemStack cansel(ChestGui.VrContainer container, int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
        return container.gui().getStackInSlot(slotId);
    }
}
