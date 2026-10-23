package plus.util.cmd;

import org.bukkit.command.CommandSender;


public interface YandeReCommandExucator {
    void onCommand(CommandSender commandSender, String string, CommandArgs args);
    void exec(CommandSender commandSender, CommandArgs args);
    String getName();
}
