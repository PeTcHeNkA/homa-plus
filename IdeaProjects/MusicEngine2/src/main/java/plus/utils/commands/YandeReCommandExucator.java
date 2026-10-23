package plus.utils.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface YandeReCommandExucator {
    void onCommand(CommandSender commandSender, String string, List<String> args);
    void exec(CommandSender commandSender, List<String> args);
}
