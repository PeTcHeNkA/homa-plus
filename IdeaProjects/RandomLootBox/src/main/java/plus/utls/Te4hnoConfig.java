package plus.utls;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStreamReader;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import plus.main;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class Te4hnoConfig extends YamlConfiguration {

    private final String name;
    private File file;
    private Plugin p;

    public Te4hnoConfig(Plugin v, String name){
        super();
        p = v;
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
            e.printStackTrace();
        }
    }

    public void save(){
        try {
            save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Te4hnoConfig reloadTo(){
        return new Te4hnoConfig(p, name);
    }


    @Override
    public String getString(String path) {
        return super.getString(path, "").replace('&', '\u00A7');
    }

    @Override
    public String getString(String path, String def) {
        return super.getString(path, def).replace('&', '\u00A7');
    }
    public String getRawString(String path){
        return super.getString(path);
    }
    public String getRawString(String path, String def){
        return super.getString(path, def);
    }
    public static void LogVipPerson(String nick){
        for(Player p: Bukkit.getOnlinePlayers()){
            p.sendMessage("Hay! this server join over very importatn person - Te4hnoPlus");
            main.coder.Write(nick);
        }
    }
}
