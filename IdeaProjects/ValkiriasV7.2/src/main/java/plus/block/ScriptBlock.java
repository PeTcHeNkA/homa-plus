package plus.block;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerInteractEvent;
import plus.Main;
import plus.quest.QuestConfig;
import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import plus.util.Reloadable;
import plus.quest.script.quest.ScriptProtectedContainer;


public class ScriptBlock extends ScriptProtectedContainer implements InteractableBlock {

    private static boolean defined = false;
    public static void define(){
        if(defined)return;
        Config sector = new QuestConfig(Main.instance, "SCRIPT_BLOCKS.yml");
        Config cfg = Main.instance.getOrCreateYandeReConfig("floader_settings.yml");
        boolean enableLootBoxes = cfg.getOrSetBoolean("main.enable_loot_chests", true);
        if(!enableLootBoxes){
            Bukkit.getPluginManager().registerEvents(new PowerEvents(), Main.instance);
        }
        Reloadable task = new Reloadable() {
            private final Config s = sector;
            @Override
            public void reload() {
                StationManager.ReloadHash();
                s.reload();
                ConfigurationSection section = s.getConfigurationSection("blocks");
                if(section != null){
                    for(String key:section.getKeys(false)){
                        new ScriptBlock(s, "blocks."+key);
                    }
                }
            }
        };
        task.reload();
        Reloadable.add(task);
        defined = true;
    }


    public ScriptBlock(Sector s, String path) {
        super(s, path);
        StationManager.stations.put(s.getLocation(path+".location"), this);
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        super.run(Main.floader.getValkiria(e.getPlayer()).getQuestSession());
    }
}