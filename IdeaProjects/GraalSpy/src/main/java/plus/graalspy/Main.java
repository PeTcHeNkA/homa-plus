package plus.graalspy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import plus.pyplugin.PyPlugin;


public final class Main extends JavaPlugin {
    public static Main INSTANCE;
    private PyPlugin pyPlugin;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic
    }


    @Override
    public void onDisable() {
        try {
            onDisablePlugin();
        } catch (Throwable e){
            e.printStackTrace();
        } finally {
            INSTANCE = null;
        }
    }


    private void onDisablePlugin(){}


    public void register(Class<? extends Event> clazz, GraalEventListener listener){
        getServer().getPluginManager().registerEvent(
                clazz, listener, EventPriority.NORMAL,  EventExecutor.INSTANCE, this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }
}
