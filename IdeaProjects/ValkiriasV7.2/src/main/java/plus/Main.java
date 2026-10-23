package plus;

import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import plus.item.IItemGenerator;
import plus.system.ModuleLoader;
import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import plus.quest.Quest;
import plus.quest.script.quest.listener.RegionListener;
import plus.valcirias.classes.ValkiriaKeQuing;
import plus.valcirias.classes.ValkiriaShooter;
import plus.valcirias.classes.ValkiriaSword;
import plus.valcirias.classes.ValkiriaUeMia;
import plus.valcirias.combat.CombatModule;
import plus.valcirias.data.UserFloader;


public final class Main extends YandeRePlugin {


    public static Config cfg;
    public static IItemGenerator generator;
    public static Main instance;
    public static ModuleLoader loader;
    public static UserFloader floader;

    public Main(){
        instance = this;
    }


    @Override
    public void onLoad(){
        WorldGuard.getInstance().getFlagRegistry()
                .register(RegionListener.QUEST_TRIGGER_ID);
        WorldGuard.getInstance().getFlagRegistry()
                .register(RegionListener.QUEST_TAKE_ID);
    }


    @Override
    public void onEnable() {
        cfg = getOrCreateYandeReConfig("config.yml");
        floader = new UserFloader(this);
        loader = new ModuleLoader(this);

        Bukkit.getPluginManager().registerEvents(new ValkiriaManager(floader), this);
        new CombatModule(this, floader);
        try {
            Quest.define();
        } catch (Exception e){e.printStackTrace();}
        new ValkiriaUeMia();
        new ValkiriaSword();
        new ValkiriaKeQuing();
        new ValkiriaShooter();
    }


    @Override
    public void onDisable() {
        floader.saveallusers();
    }
}