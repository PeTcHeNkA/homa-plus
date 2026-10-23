package plus.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;


public class Item extends net.minecraft.item.Item implements TsonSerelizable {
    static final CreativeTabs TE4ITEMS_TAB = new CreativeTabs("te4hnoitems") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Blocks.COMMAND_BLOCK);
        }
    };

    public Item(String id){
        this(id, id);
    }


    public Item(String id, String name){
        setRegistryName(id);
        setTranslationKey(name);
        setCreativeTab(TE4ITEMS_TAB);
    }


    public Item(TsonMap map){
        onInitItemFromMap(map, this);
    }


    protected static double getOrDef(TsonMap map, String param, double def){
        TsonObj obj = map.get(param);
        if(obj != null && obj.isNumber())return obj.getDouble();
        return def;
    }


    protected static void onInitItemFromMap(TsonMap map, net.minecraft.item.Item item){
        String id = map.getStr("id");
        item.setRegistryName(id);

        if(map.containsKey("name")){
            item.setTranslationKey(map.getStr("name"));
        } else {
            item.setTranslationKey(id);
        }
    }


    protected static TsonMap getDefMapOfItem(net.minecraft.item.Item item){
        TsonMap map = new TsonMap();
        map.put("id", item.getRegistryName().toString());
        map.put("name", item.getTranslationKey());
        return map;
    }


    @Override
    public TsonMap toTson() {
        return getDefMapOfItem(this);
    }
}
