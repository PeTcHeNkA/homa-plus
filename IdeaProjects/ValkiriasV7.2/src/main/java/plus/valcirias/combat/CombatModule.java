package plus.valcirias.combat;

import org.bukkit.Bukkit;
import plus.region.DefaultProvider;
import plus.region.DisabledPvp;
import plus.region.TerrianChecker;
import plus.region.WorldGaurdProvider;
import plus.valcirias.data.UserFloader;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;


public class CombatModule {

    public CombatModule(YandeRePlugin p, UserFloader floader){
        Bukkit.getPluginManager().registerEvents(new StandatrCombat(), p);

        Config c = p.getOrCreateYandeReConfig("floader_settings.yml");
        boolean enableRegions = c.getOrSetBoolean("combat.enable_regions", false);
        boolean enablePvp = c.getOrSetBoolean("combat.enable_pvp", true);

        TerrianChecker checker;
        if(enableRegions){
            try {
                checker = new WorldGaurdProvider();
            } catch (Exception e){
                System.out.println("World Guard not defined!");
                System.out.println(String.format("Enabled combat all: [%s]", enablePvp));
                if(enablePvp) checker = new DefaultProvider();
                else checker = new DisabledPvp();
            }
        } else {
            if(enablePvp) checker = new DefaultProvider();
            else checker = new DisabledPvp();
        }

        Bukkit.getPluginManager().registerEvents(new ValkiriasCombat(floader, checker), p);
    }
}
