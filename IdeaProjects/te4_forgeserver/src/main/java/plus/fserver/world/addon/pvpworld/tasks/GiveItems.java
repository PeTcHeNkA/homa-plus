package plus.fserver.world.addon.pvpworld.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class GiveItems implements Consumer<EntityPlayerMP> {
    private final ItemStack[] stacks;
    private final int[] slots;

    public static Map<String,GiveItems> kitMap(TsonMap map){
        if(map.isEmpty())return ImmutableMap.of();
        HashMap<String,GiveItems> map1 = new HashMap<>();
        map.forEach((s, obj) -> map1.put(s, new GiveItems(obj.getList())));
        return ImmutableMap.copyOf(map1);
    }


    public GiveItems(TsonList list){
        stacks = new ItemStack[list.size()];
        slots  = new int[list.size()];
        int i = 0;
        for (TsonObj obj:list){
            TsonMap map = obj.getMap();
            stacks[i] = McTsonUtils.itemStackOfTson(map);
            slots[i]  = map.getOrDefaultInt("slot", i);
            ++i;
        }
    }


    public GiveItems(NBTTagList list) {
        stacks = new ItemStack[list.tagCount()];
        slots  = new int[list.tagCount()];

        for (int i = 0; i < list.tagCount(); i++){
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            int slot = nbt.getByte("Slot") & 255;

            stacks[i] = new ItemStack(nbt);
            slots [i] = slot;
        }
    }


    @Override
    public void accept(EntityPlayerMP player) {
        ItemStack[] stacks = this.stacks;
        int[] slots        = this.slots;

        InventoryPlayer inv = player.inventory;

        for (int i = 0; i < stacks.length; i++){
            ItemStack stack = stacks[i].copy();
            if(!inv.add(slots[i], stack)){
                player.dropItem(stack, false);
            }
        }
    }
}