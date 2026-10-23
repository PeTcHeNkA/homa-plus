package plus.util.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class YandeReCommand extends Command implements YandeReCommandExucator, TabComplater{

    protected final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public YandeReCommand(String main,String name, String description, List<String> aliases) {
        super(name, description, "<args>", aliases);
        Bukkit.getServer().getCommandMap().register(main, name,this);
    }
    public YandeReCommand(String name, String description, List<String> aliases) {
        super(name, description, "<args>", aliases);
        Bukkit.getServer().getCommandMap().register(name, name,this);
    }

    public void addSubCommand(SubCommand... commands){
        for(SubCommand c:commands){
            subCommands.put(c.Name, c);
        }
    }

    public final Collection<SubCommand> getSubCommands(){
        return subCommands.values();
    }

    @Override
    public boolean execute(CommandSender commandSender, String string, String[] strings) {
        exec(commandSender, new ArrayList<>(Arrays.asList(strings)));
        return true;
    }

    @Override
    public void exec(CommandSender commandSender, List<String> args) {
        if(args.size()<1){
            this.onCommand(commandSender, null, args);
            return;
        }
        String label = args.remove(0);
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
            List<String> args = new ArrayList<>(Arrays.asList(strings));
            //args.remove(args.size() - 1);
            String firstArg = args.remove(0);
            return exexTabComplate(sender, firstArg, args);
        }
    }

    @Override
    public List<String> exexTabComplate(CommandSender commandSender, String label,List<String> args) {
        TabComplater t = subCommands.get(label);
        if(t==null || args.isEmpty()){
            return onTabComplate(commandSender, label, args);
        } else {
            label = args.remove(0);
            return t.exexTabComplate(commandSender, label, args);
        }
    }


    @Override
    public void onCommand(CommandSender commandSender, String label, List<String> args) {

    }

    private List<String> subLabels = null;
    @Override
    public List<String> onTabComplate(CommandSender commandSender, String label, List<String> args) {
        if(subLabels == null) subLabels = new ArrayList<>(subCommands.keySet());
        return subLabels;
    }


}