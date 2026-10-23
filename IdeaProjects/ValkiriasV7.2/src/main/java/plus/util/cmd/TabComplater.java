package plus.util.cmd;

import org.bukkit.command.CommandSender;
import java.util.List;


public interface TabComplater {
    List<String> exexTabComplate(CommandSender commandSender, String string, CommandArgs args);
    List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args);
}
