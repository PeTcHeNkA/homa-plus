package plus.elch;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plus.elch.chat.ElMessageBus;
import plus.elch.utl.FastExitException;
import plus.tson.TsonMap;

import java.io.IOException;


public class ElegantMain extends JavaPlugin {
    public static final MiniMessage MINI_MSG_INST = MiniMessage.miniMessage();
    public static ElegantMain INSTANCE;
    public static String ALLOW_EVAL_PERM = "elegantc.allow-eval";
    public static ElMessageBus BUS;

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            throw new RuntimeException("PlaceholderAPI not installed");

        //todo
        try {
            BUS =  new ElMessageBus(new TsonMap());
        } catch (FastExitException ignored) {} catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Bukkit.getPluginCommand()

        INSTANCE = this;
//        getServer().getPluginManager().registerEvent(
//
//        );
    }
}
