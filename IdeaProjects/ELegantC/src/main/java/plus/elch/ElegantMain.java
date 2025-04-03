package plus.elch;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class ElegantMain extends JavaPlugin {
    public static final MiniMessage MINI_MSG_INST = MiniMessage.miniMessage();
    public static ElegantMain INSTANCE;

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            throw new RuntimeException("PlaceholderAPI not installed");


        //Bukkit.getPluginCommand()

        INSTANCE = this;
//        getServer().getPluginManager().registerEvent(
//
//        );
    }
}
