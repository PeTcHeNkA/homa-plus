package plus.utils.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
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
                String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length() -1);
                File dir = new File(path);
                if(!dir.exists() || !dir.isDirectory())dir.mkdir();
                file.createNewFile();
            }catch (Exception err){
                err.printStackTrace();
            }
        }
    }


    @Override
    public SimpleSector reloadAsFile(){
        return new PluginSector(plugin, name);
    }
}
