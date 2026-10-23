package plus.util.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.*;


public class YandeReCommand extends Command implements YandeReCommandExucator, TabComplater{

    protected final HashMap<String, YandeReCommandExucator> subCommands = new HashMap<>();
    protected final List<String> subLabels = new ArrayList<>();

    public YandeReCommand(String main,String name, String description, List<String> aliases) {
        super(name, description, "<args>", aliases);
        Bukkit.getServer().getCommandMap().register(main, name,this);
    }


    public YandeReCommand(String name, String description, List<String> aliases) {
        super(name, description, "<args>", aliases);
        Bukkit.getServer().getCommandMap().register(name, name,this);
    }


    public void addSubCommand(YandeReCommandExucator... commands){
        for(YandeReCommandExucator c:commands){
            subCommands.put(c.getName(), c);
            subLabels.add(c.getName());
        }
    }


    public final Collection<YandeReCommandExucator> getSubCommands(){
        return subCommands.values();
    }


    @Override
    public boolean execute(CommandSender commandSender, String string, String[] strings) {
        exec(commandSender, new CommandArgs(strings));
        return true;
    }


    @Override
    public void exec(CommandSender commandSender, CommandArgs args) {
        if(args.isLast()){
            this.onCommand(commandSender, null, args);
            return;
        }
        String label = args.getAndShift();
        YandeReCommandExucator exucator = subCommands.get(label);

        if(exucator == null){
            this.onCommand(commandSender, label, args);
        } else {
            exucator.exec(commandSender, args);
        }
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] strings) {
        if(strings.length<2){
            return new ArrayList<>(subCommands.keySet());
        } else {
            CommandArgs args = new CommandArgs(strings);
            return exexTabComplate(sender, args.getAndShift(), args);
        }
    }


    @Override
    public List<String> exexTabComplate(CommandSender commandSender, String label, CommandArgs args) {
        YandeReCommandExucator exucator = subCommands.get(label);
        if(exucator==null || args.isLast()){
            return onTabComplate(commandSender, label, args);
        } else {
            if(exucator instanceof TabComplater t) {
                return t.exexTabComplate(commandSender, args.getAndShift(), args);
            } else return onTabComplate(commandSender, label, args);
        }
    }


    @Override
    public void onCommand(CommandSender commandSender, String label, CommandArgs args) {}


    @Override
    public List<String> onTabComplate(CommandSender commandSender, String label, CommandArgs args) {
        return subLabels;
    }
}