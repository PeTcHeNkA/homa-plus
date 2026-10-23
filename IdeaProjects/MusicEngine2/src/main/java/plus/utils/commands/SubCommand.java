package plus.utils.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public abstract class SubCommand implements YandeReCommandExucator, TabComplater, SubCommandable{

    protected final HashMap<String, SubCommand> subCommands = new HashMap<>();
    protected final String Name;

    protected SubCommand(String name) {
        Name = name;
        SubCommand[] s = SubCommands();
        if(s!=null)for(SubCommand subCommand:s){
            subCommands.put(subCommand.Name, subCommand);
        }
    }


    public final Collection<SubCommand> getSubCommands(){
        return subCommands.values();
    }


    public final Collection<String> getListSubCommands(){
        return subCommands.keySet();
    }


    public final SubCommand getSubCommand(String label){
        return subCommands.get(label);
    }


    public final void addSubCommand(SubCommand c){
        subCommands.put(c.Name, c);
    }


    @Override
    public void exec(CommandSender commandSender, List<String> args) {
        if(args.isEmpty()){
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
    public List<String> exexTabComplate(CommandSender commandSender, String label,List<String> args) {
        TabComplater t = label==null?null:subCommands.get(label);
        if(t==null || args.isEmpty()){
            List<String> l = onTabComplate(commandSender, label, args);
            if(l == null)return new ArrayList<>(subCommands.keySet());
            else {
                l.addAll(subCommands.keySet());
                return l;
            }
        } else {
            label = args.remove(0);
            return t.exexTabComplate(commandSender, label, args);
        }
    }
}