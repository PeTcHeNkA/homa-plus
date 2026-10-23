package plus.quest.script.quest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.chest.SimpleChest;
import plus.quest.AbstractQuestSession;
import plus.util.cfg.Sector;


public class ScriptReward implements QuestScript{

    private final SimpleChest loot;
    private final int exp;


    public ScriptReward(Sector s, String path){
        if(s.contains(path+".items")) loot = new SimpleChest(s, path, true);
        else loot = null;
        exp = s.getOrSetInt(path+".exp", 50);
    }


    //@Override
    //public void run(QuestSession session) {}


    @Override
    public void run(AbstractQuestSession session){
        if(exp>0) Main.floader.getValkiria(session.getPlayer().getName()).addExperience(exp);
        if(loot != null) {
            Player player = session.getPlayer();
            ItemStack[] items =  loot.dropContent();
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ItemStack item : items) {
                        int slot = player.getInventory().firstEmpty();
                        if (slot >= 0) {
                            player.getInventory().setItem(slot, item);
                        } else {
                            player.getWorld().dropItem(player.getLocation(), item);
                        }
                    }
                }
            }.runTaskLater(Main.instance, 0);
        }
        //return true;
    }
}