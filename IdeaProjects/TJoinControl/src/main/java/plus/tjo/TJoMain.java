package plus.tjo;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import plus.tjo.listener.FExecutor;


public final class TJoMain extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public <T extends Event> void register(Class<T> clazz, FExecutor.Listener<T> listener, int priority){
        getServer().getPluginManager().registerEvent(clazz, listener, priorityOf(priority), FExecutor.INSTANCE, this);
    }


    static EventPriority priorityOf(int i){
        EventPriority[] values = EventPriority.values();
        if(i >= values.length)i = values.length-1;
        if(i < 0)i = 0;
        return values[i];
    }
}
