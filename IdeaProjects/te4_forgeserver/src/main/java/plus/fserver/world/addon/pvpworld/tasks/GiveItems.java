package plus.fserver.world.addon.pvpworld.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.Nullable;
import plus.fserver.utls.McTsonUtils;
import plus.fserver.world.addon.pvpworld.MinigameWorld;
import plus.fserver.world.addon.pvpworld.games.PvPlData;
import plus.fserver.world.addon.pvpworld.games.PvWData;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static plus.fserver.api.ServerCommand.reply;


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


    public static boolean processKitCommand(Map<String,GiveItems> kitMap, MinigameWorld world, ICommandSender sender, @Nullable ICommand command, String cmdName, String[] args) {
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (cmdName.equals("syskit")) {
                PvWData data = world.data();

                if(data.usedKit != null || kitMap == null){
                    reply(sender, "&cChoose kit is locked!");
                } else {
                    PvPlData pData = (PvPlData)data.stats(player);

                    GiveItems kit = kitMap.get(args[0]);
                    if(kit == null && !args[0].equals("null")){
                        reply(sender, "&cKit ["+args[0]+"] not founded!");
                    } else {
                        pData.usedKit = kit;
                    }
                }
                return true;
            }
        }
        return false;
    }
}