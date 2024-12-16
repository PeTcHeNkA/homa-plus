package plus.fserver.api.vanillagui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import plus.fserver.api.IPlayerDataModel;
import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class SmartChestGuiTemplate extends ChestGuiTemplate{
    private final Int2ObjectOpenHashMap<TsonFunc> slotFunctions = new Int2ObjectOpenHashMap<>();
    private TsonFunc onClose;
    private TsonFunc[] runtimeInvGen;
    private byte[] runtimeInvSlots;

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

        if(map.containsKey("dynamic")){
            TsonList lActs = map.getList("dynamic");
            runtimeInvGen = new TsonFunc[lActs.size()];
            runtimeInvSlots = new byte[lActs.size()];
            int i = 0;

            for (TsonObj obj : lActs) {
                TsonMap map1 = obj.getMap();
                TsonObj obj2 = map1.get("item");
                if(obj2.isFunc()) {
                    runtimeInvGen[i] = (TsonFunc) obj2.getField();
                } else {
                    runtimeInvGen[i] = new ItemGenFunc(obj2);
                }
                runtimeInvSlots[i] = (byte) map1.getInt("slot");
                ++i;
            }
        }

        if(map.containsKey("on-close")){
            onClose = map.getFunc("on-close");
        }
    }


    @Override
    protected ChestGui create(){
        SmartChestGui chest = new SmartChestGui(size());
        chest.fillOf(this);
        chest.setCustomName(getCustomName());
        chest.setFunctions(functions());
        chest.setOnClose(onClose);
        return chest;
    }


    @Override
    public ChestGui open(IPlayerDataModel player) {
        ChestGui gui = super.open(player);
        TsonFunc[] runtimeInvGen = this.runtimeInvGen;
        if(runtimeInvGen != null){
            byte[] slots = this.runtimeInvSlots;

            for (int i = 0; i < runtimeInvGen.length; i++) {
                ItemStack stack = (ItemStack)runtimeInvGen[i].call(player);
                if(stack != null){
                    gui.setInventorySlotContents(slots[i]&255, stack);
                }
            }
        }
        return gui;
    }


    public Int2ObjectMap<TsonFunc> functions(){
        return slotFunctions;
    }
}