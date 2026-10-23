package plus;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plus.control.Commands;
import plus.music.Events;
import plus.music.MCategory;
import plus.regions.RegionProvider;
import plus.regions.worldgaurd.MusicHandler;
import plus.utils.config.Config;


public class Main extends JavaPlugin {

    public static Main I;
    public static Config c;

    @Override
    public void onLoad(){
        WorldGuard.getInstance().getFlagRegistry()
                .register(MusicHandler.SOUND_ID_FLAG);
    }

    @Override
    public void onEnable(){
        I = this;
        c = new Config(Main.I, "config.yml");
        if(c.getOrSetBoolean("ENABLE_COMMANDS", true)) new Commands();
        if(c.getOrSetBoolean("ENABLE_EVENT_HUNDLER", true))
            Bukkit.getPluginManager().registerEvents(new Events(), this);

        MCategory.loadStatic();
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(MusicHandler.FACTORY, null);
        RegionProvider.setDefault(new MusicHandler.RProvider());

    }
}
