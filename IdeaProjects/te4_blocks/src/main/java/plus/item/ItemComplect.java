package plus.item;

import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import plus.tson.TsonClass;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;
import java.util.ArrayList;
import static plus.item.Item.TE4ITEMS_TAB;
import static plus.item.Item.getOrDef;


public class ItemComplect extends ArrayList<net.minecraft.item.Item> implements TsonSerelizable {

    protected static Item.ToolMaterial toolMaterialOfMap(TsonMap map){
        String id = map.getStr("id").toUpperCase();
        try {
            return Item.ToolMaterial.valueOf(id);
        }catch (IllegalArgumentException | NullPointerException e){
            return newMaterial(
                    id,
                    (int) getOrDef(map, "lvl", 2),
                    (int) getOrDef(map, "usages", 1000),
                    (float) getOrDef(map, "speed", 8),
                    (float) getOrDef(map, "damage", 2),
                    (int) getOrDef(map, "enchant", 15)
            );
        }
    }


    public static Item.ToolMaterial newMaterial(String id, int lvl, int usages, float speed, float damage, int enchant){
        try {
            return Item.ToolMaterial.valueOf(id);
        }catch (IllegalArgumentException | NullPointerException e){
            return EnumHelper.addToolMaterial(id, lvl, usages,speed,damage,enchant);
        }
    }


    protected static TsonMap toolMaterialToMap(Item.ToolMaterial m){
        TsonMap map = new TsonMap();
        map.put("id", m.name());
        map.put("lvl",m.getHarvestLevel());
        map.put("usages",m.getMaxUses());
        map.put("speed",m.getEfficiency());
        map.put("damage",m.getAttackDamage());
        map.put("enchant",m.getEnchantability());
        return map;
    }


    protected static void applyBaseData(net.minecraft.item.Item item, String id, String name){
        item.setRegistryName(id);
        item.setTranslationKey(name);
    }

    public Item.ToolMaterial material;
    public String patern = "SPALHI";
    public String id;
    public String name;

    public ItemComplect(){}

    public ItemComplect(TsonMap map){
        patern = map.getStr("patern").toUpperCase();
        id = map.getStr("id");
        name = map.containsKey("name")? map.getStr("name") : id;
        material = toolMaterialOfMap(map.getMap("material"));
        net.minecraft.item.Item item;
        if(patern.contains("S")){
            item = new ItemSword(material);
            applyBaseData(item, id+"_sword",name+"_sword");
            add0(item);
        }
        if(patern.contains("P")){
            item = (net.minecraft.item.Item) new TsonClass(ItemPickaxe.class).createInst(material);
            applyBaseData(item, id+"_pickaxe",name+"_pickaxe");
            add0(item);
        }
        if(patern.contains("A")){
            item = new ItemAxe(material);
            applyBaseData(item, id+"_axe",name+"_axe");
            add0(item);
        }
        if(patern.contains("L")){
            item = new ItemSpade(material);
            applyBaseData(item, id+"_shovel",name+"_shovel");//plus.item.ItemComplect plus.block.BlockGen
            add0(item);
        }
        if(patern.contains("H")){
            item = new ItemHoe(material);
            applyBaseData(item, id+"_hoe",name+"_hoe");
            add0(item);
        }
        if(patern.contains("I")){
            item = new ItemSword(material);
            applyBaseData(item, id,name);
            add0(item);
            material.setRepairItem(new ItemStack(item));
        }
    }


    public void add0(net.minecraft.item.Item item) {
        item.setCreativeTab(TE4ITEMS_TAB);
        super.add(item);
    }


    @Override
    public TsonObj toTson() {
        TsonMap map = new TsonMap();
        map.put("patern",patern);
        map.put("id", id);
        map.put("name", name);
        map.put("material", toolMaterialToMap(material));
        return map;
    }
}