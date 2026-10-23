package plus.util.Config;


import java.io.InputStreamReader;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class PluginSector extends BasicSector {

    private final Plugin plugin;

    public PluginSector(Plugin v, String name){
        plugin = v;
        this.name = name;
        try {
            file = new File(v.getDataFolder(), name);
            if (!file.exists()) {
                load(new InputStreamReader(v.getResource(name), StandardCharsets.UTF_8));
                save(file);
            } else {
                load(file);
            }
        } catch (Exception e){
            try {
                file.createNewFile();
            }catch (Exception ignored){}
            //e.printStackTrace();
        }
    }

    @Override
    public SimpleSector reloadAsFile(){
        return new PluginSector(plugin, name);
    }
}
