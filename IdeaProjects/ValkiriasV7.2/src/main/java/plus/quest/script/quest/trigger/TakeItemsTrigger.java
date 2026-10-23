package plus.quest.script.quest.trigger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.craft.BetterCraft;
import plus.craft.CraftItem;
import plus.craft.CraftThing;
import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;
import plus.quest.script.quest.listener.CmdListener;
import plus.quest.script.quest.listener.CommandListener;
import plus.util.cmd.CommandArgs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TakeItemsTrigger extends Trigger implements CmdListener {

    private final CraftThing[] items;
    private final boolean needPickUp;
    private final String keyActivation;

    public TakeItemsTrigger(Sector s, String path){
        super(s, path);
        List<String> data = s.getOrSetStringList(path + ".need_items", Arrays.asList("I:example"));
        items = new CraftThing[data.size()];
        for(int i=0;i<items.length;i++){
            items[i] = BetterCraft.defineCraftThing(data.get(i));
        }
        needPickUp = s.getOrSetBoolean(path+".need_pick_up", true);
        keyActivation = s.getOrSetString(path+".activation_key", "give_items");
    }


    public boolean isComplete(Player player){

        ItemStack[] forCheck = player.getInventory().getContents();
        HashMap<Integer, Integer> slots = new HashMap<>();
        int size = player.getInventory().getSize();
        for(int slot=0;slot<size;slot++){
            ItemStack checked = forCheck[slot];
            if(checked==null)continue;
            for(int i=0;i<items.length;i++){
                if(slots.containsKey(i))break;
                if(items[i].isSuccessful(forCheck[slot])){
                    slots.put(i, slot);
                }
            }
        }

        for(int i=0;i<items.length;i++){
            if(!slots.containsKey(i)){
                return false;
            }
        }

        if(needPickUp) {
            Inventory inventory = player.getInventory();
            for (int index : slots.keySet()) {
                int slot = slots.get(index);
                int amount;
                if (items[index] instanceof CraftItem) {
                    amount = inventory.getItem(slot).getAmount() - items[index].view().getAmount();
                } else {
                    amount = inventory.getItem(slot).getAmount() - 1;
                }
                if (amount > 0) {
                    ItemStack item = inventory.getItem(slot);
                    item.setAmount(amount);
                    inventory.setItem(slot, item);
                } else {
                    inventory.setItem(slot, null);
                }
            }
        }
        return true;
    }


    @Override
    public void onLoad(AbstractQuestSession session) {
        CommandListener.addCmdListener(session.getPlayer().getName(), keyActivation, this);
    }


    @Override
    public void onUnload(AbstractQuestSession session) {
        CommandListener.addCmdListener(session.getPlayer().getName(), keyActivation, null);
    }


    @Override
    public boolean onCommand(AbstractQuestSession session, CommandArgs args) {
        if(isComplete(session.getPlayer())){
            run(session);
            return needUnloadAfterActivation;
        } else {
            return false;
        }
    }
}