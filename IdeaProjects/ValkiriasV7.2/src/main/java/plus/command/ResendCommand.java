package plus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plus.Main;
import plus.util.ConfigHelper;
import plus.valcirias.Valkiria;

import java.util.Arrays;

public class ResendCommand extends ChatCommand{

    public ResendCommand(){
        super("resend", "send private message", "/r <message>", Arrays.asList("r", "rs"));
    }


    @Override
    public boolean execute(CommandSender commandSender, String string, String[] args) {
        Valkiria sender = Main.floader.getValkiria(commandSender.getName());
        if(args.length==0)return true;
        Player target = Bukkit.getPlayer(sender.getLastTargetMessage());
        if(target==null) {
            commandSender.spigot().sendMessage(ConfigHelper.messagePlayerOffline);
            return true;
        }

        onCommand(sender, target, args);
        return true;
    }
}