package plus.util;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import plus.util.cmd.SubCommandable;
import plus.util.cmd.YandeReCommand;
import plus.util.cfg.Config;
import plus.util.cmd.YandeReCommandExucator;
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
            c.reload();
        }
    }


    public final void reloadAll(){
        reloadConfig();
        for(Reloadable r:anyReloadables){
            r.reload();
        }
        for(YandeReCommand c:commands.values()){
            if(c instanceof Reloadable){
                ((Reloadable) c).reload();
            }
            for(YandeReCommandExucator cc:c.getSubCommands()){
                reloadSubCommands(cc);
            }
        }
    }


    public void registerReloadable(Reloadable object){
        if(object instanceof Config c) {
            configs.put(c.getFileName(), c);
        } else {
            if (anyReloadables == null) anyReloadables = new ArrayList<>();
            anyReloadables.add(object);
        }
    }


    private void reloadSubCommands(YandeReCommandExucator exucator){
        if(exucator instanceof SubCommandable c) {
            if (!c.getSubCommands().isEmpty()) {
                for (YandeReCommandExucator cc : c.getSubCommands()) {
                    if (cc instanceof Reloadable) {
                        ((Reloadable) cc).reload();
                        reloadSubCommands(cc);
                    }
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
