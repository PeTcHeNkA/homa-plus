package plus.utils.commands;

import org.bukkit.command.CommandSender;

import java.util.List;


public interface TabComplater {
    List<String> exexTabComplate(CommandSender commandSender, String string, List<String> args);
    List<String> onTabComplate(CommandSender commandSender, String string, List<String> args);
}
