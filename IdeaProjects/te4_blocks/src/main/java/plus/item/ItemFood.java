package plus.item;

import plus.tson.TsonMap;
import plus.tson.TsonSerelizable;
import static plus.item.Item.*;


public class ItemFood extends net.minecraft.item.ItemFood implements TsonSerelizable {


    public ItemFood(String id, Integer heal, Float saturation){
        this(id, id, heal, saturation);
    }


    public ItemFood(String id, String name, Integer heal, Float saturation){
        super(heal, saturation, true);
        setRegistryName(id);
        setTranslationKey(name);
        setCreativeTab(TE4ITEMS_TAB);
    }


    public ItemFood(TsonMap map){
        super((int) getOrDef(map, "heal", 5),
                (float) getOrDef(map, "saturation", 5),true);
        Item.onInitItemFromMap(map, this);
        setCreativeTab(TE4ITEMS_TAB);
    }


    @Override
    public TsonMap toTson() {
        TsonMap map = getDefMapOfItem(this);
        map.put("heal", getHealAmount(null));
        map.put("saturation", getSaturationModifier(null));
        return null;
    }
}