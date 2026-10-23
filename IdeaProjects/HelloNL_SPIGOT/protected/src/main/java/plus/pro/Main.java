package plus.pro;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plus.tson.TsonMap;

import java.util.function.Consumer;


public class Main implements Consumer<TsonMap> {
    @Override
    public void accept(TsonMap tsonMap) {
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("ExampleNL");
        plugin.getServer().getPluginManager().registerEvents(new Events(), plugin);
    }
}