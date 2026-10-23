package plus;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import plus.util.Commands.SubCommand;
import plus.util.Commands.YandeReCommand;
import plus.util.Config.Config;
import plus.util.Reloadable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class YandeRePlugin extends JavaPlugin{

    private final HashMap<String, Config> configs = new HashMap();
    private final HashMap<String, YandeReCommand> commands = new HashMap<>();
    private List<Reloadable> anyReloadables;

    @Override
    public void reloadConfig(){
        super.reloadConfig();
        for(Config c:configs.values()){
            c.Reload();
        }
    }
    public final void reloadAll(){
        reloadConfig();
        for(Reloadable r:anyReloadables){
            r.Reload();
        }
        for(YandeReCommand c:commands.values()){
            if(c instanceof Reloadable){
                ((Reloadable) c).Reload();
            }
            for(SubCommand cc:c.getSubCommands()){
                ReloadSubCommands(cc);
            }
        }
    }

    public void registerReloadable(Reloadable object){
        if(anyReloadables==null)anyReloadables = new ArrayList<>();
        anyReloadables.add(object);
    }

    private void ReloadSubCommands(SubCommand c){
        if(!c.getSubCommands().isEmpty()) {
            for (SubCommand cc : c.getSubCommands()) {
                if (cc instanceof Reloadable) {
                    ((Reloadable) cc).Reload();
                    ReloadSubCommands(cc);
                }
            }
        }
    }

    public final PluginCommand getCommand(String arg){
        System.out.println("This command method is legacy. Use [ createYandeReCommand(<args>) ]");
        return super.getCommand(arg);
    }

    public final YandeReCommand createYandeReCommand(String main,String name, String description, List<String> aliases){
        YandeReCommand c = new YandeReCommand(main, name, description, aliases);
        commands.put(main, c);
        return c;
    }

    public final YandeReCommand createYandeReCommand(String name, String description, List<String> aliases){
        return createYandeReCommand(this.getName(), name, description, aliases);
    }

    public final Config getOrCreateYandeReConfig(String name){
        if(configs.containsKey(name))
            return configs.get(name);
        else {
            Config c = new Config(this, name);
            configs.put(name, c);
            return c;
        }
    }
}
