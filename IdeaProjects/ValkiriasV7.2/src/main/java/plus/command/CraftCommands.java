package plus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import plus.Main;
import plus.util.ConfigHelper;
import plus.system.CraftModule;
import plus.util.cmd.CommandArgs;
import plus.valcirias.Valkiria;
import plus.util.YandeRePlugin;
import plus.util.cmd.SubCommand;

import java.util.Arrays;
import java.util.List;


public class CraftCommands extends SubCommand {

    private final CraftModule cm = new CraftModule();
    protected CraftCommands(String name, YandeRePlugin p) {
        super(name);
        Bukkit.getPluginManager().registerEvents(cm, p);
    }

    @Override
    public SubCommand[] injectSubCommands() {
        return new SubCommand[0];
    }

    private final List<String> onlyPremium = Arrays.asList("Hey! Craft command only for premium!");
    @Override
    public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
        Valkiria v = Main.floader.getValkiria(commandSender.getName());
        if(v.isPremium()) return null;
        else return onlyPremium;
    }

    @Override
    public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
        try {
            Valkiria v = Main.floader.getValkiria(commandSender.getName());
            if(!v.isPremium()){
                v.toPlayer().spigot().sendMessage(ConfigHelper.messageOnlyPremium);
                return;
            }
            CraftModule.craftTable.Open(v.toPlayer(), -1, 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}