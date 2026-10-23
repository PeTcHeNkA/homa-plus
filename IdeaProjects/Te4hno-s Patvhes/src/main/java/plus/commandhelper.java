package plus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commandhelper implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player pl = (Player) sender;
            pl.spigot().sendMessage(parserhelper.parseComponents(pl, args));
        } else {
            //logic usage as console
        }
        return true;
    }
}
