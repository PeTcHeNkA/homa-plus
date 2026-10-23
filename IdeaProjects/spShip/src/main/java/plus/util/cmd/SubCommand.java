package plus.util.cmd;

import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public abstract class SubCommand implements YandeReCommandExucator, TabComplater, SubCommandable, Aliacable{

    protected final HashMap<String, YandeReCommandExucator> subCommands = new HashMap<>();
    protected final String Name;

    protected SubCommand(String name) {
        Name = name;
        YandeReCommandExucator[] s = injectSubCommands();
        if(s!=null)for(YandeReCommandExucator subCommand:s){
            if(subCommand instanceof Aliacable a){
                for(String aliace:a.aliaces()){
                    subCommands.put(aliace, subCommand);
                }
            }
            subCommands.put(subCommand.getName(), subCommand);
        }
    }


    @Override
    public final Collection<YandeReCommandExucator> getSubCommands(){
        return subCommands.values();
    }


    public final YandeReCommandExucator getSubCommand(String label){
        return subCommands.get(label);
    }


    public final void addSubCommand(SubCommand c){
        subCommands.put(c.Name, c);
    }


    @Override
    public void exec(CommandSender commandSender, CommandArgs args) {
        if(args.isLast()){
            this.onCommand(commandSender, null, args);
            return;
        }
        String label = args.getAndShift();

        YandeReCommandExucator executor = subCommands.get(label);
        if(executor == null){
            this.onCommand(commandSender, label, args);
        } else {
            executor.exec(commandSender, args);
        }
    }


    @Override
    public String getName(){
        return Name;
    }


    @Override
    public List<String> exexTabComplate(CommandSender commandSender, String label, CommandArgs args) {
        YandeReCommandExucator exucator = label==null?null:subCommands.get(label);
        if(exucator==null || args.isEmpty()){
            List<String> l = onTabComplate(commandSender, label, args);
            if(l == null)return new ArrayList<>(subCommands.keySet());
            else {
                l.addAll(subCommands.keySet());
                return l;
            }
        } else {
            if(exucator instanceof TabComplater t) {
                label = args.getAndShift();
                return t.exexTabComplate(commandSender, label, args);
            } else return null;
        }
    }
}