package plus.fserver.api.vanillagui;

import net.minecraft.block.BlockChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.ILockableContainer;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class ChestGuiTemplate {
    private final ItemStack[] stacks;
    private final byte[] slots;
    private String customName;
    protected Object[] vrPl;

    public static ChestGuiTemplate of(TsonMap map){
        if(map.containsKey("actions") || map.containsKey("default-action") || map.containsKey("on-close")){
            return new SmartChestGuiTemplate(map);
        } else {
            return new ChestGuiTemplate(map);
        }
    }


    public static ChestGuiTemplate of(TileEntityChest chest){
        BlockChest bc = (BlockChest) chest.getWorld().getBlockState(chest.getPos()).getBlock();

        ILockableContainer inv = bc.getLockableContainer(chest.getWorld(), chest.getPos());

        ChestGuiTemplate template = new ChestGuiTemplate(inv.getSizeInventory());

        for (int i = 0; i < template.stacks.length; i++){
            template.stacks[i] = inv.getStackInSlot(i);
            template.slots[i] = (byte) i;
        }
        return template;
    }


    public TsonMap toTson(){
        TsonMap map = new TsonMap();
        TsonList list = new TsonList();
        map.put("items", list);
        map.put("size", stacks.length);
        for (int i = 0; i < stacks.length; i++){
            ItemStack item = stacks[i];
            if(!item.isEmpty()) {
                TsonObj sub = McTsonUtils.itemStackToTson(item);
                TsonMap map0;
                if(sub.isString()){
                    map0 = new TsonMap();
                    map0.put("id", sub);
                } else {
                    map0 = sub.getMap();
                }
                map0.put("slot", slots[i]);

                list.add(map0);
            }
        }
        return map;
    }


    protected ChestGuiTemplate(int size){
        this.stacks = new ItemStack[size];
        this.slots = new byte[size];
    }


    public ChestGuiTemplate(TsonMap map){
        int size = -1;
        if(map.containsKey("size")){
            TsonObj sizeR = map.get("size");
            if(sizeR.isNumber()) {
                size = sizeR.getInt();
                if (size % 9 != 0 && size != 5)
                    throw new IllegalArgumentException();
            }
        } else if(map.containsKey("rows")){
            TsonObj sizeR = map.get("rows");
            if(sizeR.isNumber())
                size = sizeR.getInt() * 9;
        }

        if(map.containsKey("name")){
            customName = map.getStr("name");
        }

        TsonList lItems = map.getList("items");

        if(size == -1){
            int maxSlot = 0;
            int i = 0;
            for (TsonObj item:lItems){
                if(item.isMap()){
                    if(item.getMap().containsKey("slots")){
                        TsonList slots = item.getMap().getList("slots");
                        for (TsonObj slot0:slots){
                            maxSlot = Math.max(slot0.getInt(), maxSlot);
                        }
                    } else {
                        maxSlot = Math.max(item.getMap().getOrDefaultInt("slot", i), maxSlot);
                    }
                }
                ++i;
            }

            size = maxSlot;
            if(size != 5) {
                int delta = maxSlot % 9;
                if (delta != 0) {
                    size = size - delta + 9;
                }
            }
        }

        ItemStack[] stacks = this.stacks = new ItemStack[size];
        byte[] slots = this.slots = new byte[size];

        int i = 0;
        for (TsonObj item:lItems){
            int slot = i;
            if(item.isMap()){
                if(item.getMap().containsKey("slots")){
                    ItemStack item1 = McTsonUtils.itemStackOfTson(item);

                    TsonList slots1 = item.getMap().getList("slots");
                    for (TsonObj slot0:slots1){
                        int slot1 = slot0.getInt();
                        slots[slot1] = (byte) slot1;
                        stacks[slot1] = item1;
                    }
                    ++i;
                    continue;
                } else {
                    slot = item.getMap().getOrDefaultInt("slot", slot);
                }
            }
            stacks[i] = McTsonUtils.itemStackOfTson(item);
            slots[i]  = (byte) slot;
            ++i;
        }

        if(map.containsKey("uitems")){
            lItems = map.getList("uitems");

            ItemStack[] stacksU = new ItemStack[41];
            byte[] slotsU = new byte[41];

            vrPl = new Object[]{stacksU, slotsU};

            for (TsonObj item:lItems){
                int slot = i;
                if(item.isMap()){
                    if(item.getMap().containsKey("slots")){
                        ItemStack item1 = McTsonUtils.itemStackOfTson(item);

                        TsonList slots1 = item.getMap().getList("slots");
                        for (TsonObj slot0:slots1){
                            int slot1 = slot0.getInt();
                            slotsU[slot1] = (byte) slot1;
                            stacksU[slot1] = item1;
                        }
                        ++i;
                        continue;
                    } else {
                        slot = item.getMap().getOrDefaultInt("slot", slot);
                    }
                }
                stacksU[i] = McTsonUtils.itemStackOfTson(item);
                slotsU[i]  = (byte) slot;
                ++i;
            }
        }
    }


    protected ChestGui create(){
        ChestGui chest = new ChestGui(size());
        chest.fillOf(this);
        chest.setCustomName(getCustomName());
        chest.setVrPl(vrPl);
        return chest;
    }


    public ChestGui open(IPlayerDataModel player){
        ChestGui chest = create();
        chest.open(player.getSelfPlayer());
        return chest;
    }


    public String getCustomName() {
        return customName;
    }


    public void setCustomName(String customName) {
        this.customName = customName;
    }


    public int rows(){
        return stacks.length / 9;
    }


    public int size(){
        return stacks.length;
    }


    public ItemStack getStackCopy(int index){
        ItemStack result = stacks[index];
        if(result == null)return ItemStack.EMPTY;
        return result.copy();
    }


    public ItemStack getStack(int index){
        ItemStack result = stacks[index];
        if(result == null)return ItemStack.EMPTY;
        return result;
    }


    public int getSlot(int index){
        return slots[index] & 255;
    }
}