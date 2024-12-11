package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
                TsonFunc func = map1.getFunc("func");
                if(map1.containsKey("slots")){
                    TsonList list = map1.getList("slots");
                    for (TsonObj obj1:list){
                        slotFunctions.put(obj1.getInt(), func);
                    }
                } else {
                    slotFunctions.put(map1.getInt("slot"), func);
                }
            }
        }

        if(map.containsKey("on-close")){
            onClose = map.getFunc("on-close");
        }
    }


    @Override
    public ChestGui create(){
        SmartChestGui chest = new SmartChestGui(size());
        chest.fillOf(this);
        chest.setCustomName(getCustomName());
        chest.setFunctions(functions());
        chest.setOnClose(onClose);
        return chest;
    }


    public Int2ObjectMap<TsonFunc> functions(){
        return slotFunctions;
    }
}