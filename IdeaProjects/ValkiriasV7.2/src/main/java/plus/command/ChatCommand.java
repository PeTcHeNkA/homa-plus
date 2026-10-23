package plus.command;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plus.Main;
import plus.util.ConfigHelper;
import plus.util.chat.ChatHelper;
import plus.valcirias.Valkiria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChatCommand extends Command {

    public ChatCommand(String name, String description, String usage, List<String> aliases) {
        super(name, description, usage, aliases);
        Bukkit.getServer().getCommandMap().register(name, "chat",this);
    }

    public ChatCommand(){
        this("chat", "send private message", "/m <player> <message>", Arrays.asList("m", "ms"));
    }


    public void onCommand(Valkiria sender, Player target, String[] args) {
        if(sender.isMuted()){
            sender.toPlayer().spigot().sendMessage(ConfigHelper.messageMuted);
            return;
        }

        String sMessage = String.join(" ", args);
        if(sMessage.contains("$"))sMessage = sMessage.replace('$', '&');
        TextComponent Message = new TextComponent(sMessage);

        sender.toPlayer().spigot().sendMessage(
                ConfigHelper.SenderPrivate_pefix,
                ChatHelper.getValkiriaProfile(Main.floader.getValkiria(target)),
                Message
        );

        target.spigot().sendMessage(
                ConfigHelper.TargetPrivate_pefix,
                ChatHelper.getValkiriaProfile(sender),
                Message
        );
    }


    @Override
    public boolean execute(CommandSender commandSender, String string, String[] args) {
        Valkiria sender = Main.floader.getValkiria(commandSender.getName());
        if(args.length==0)return true;
        Player target = Bukkit.getPlayer(args[0]);
        if(target==null) {
            commandSender.spigot().sendMessage(ConfigHelper.messagePlayerOffline);
            return true;
        } else {
            sender.setLastTargetMessage(target);
            Main.floader.getValkiria(target).setLastTargetMessage(sender.toPlayer());
        }

        String[] newArgs = new String[args.length-1];
        System.arraycopy(args, 1, newArgs, 0, args.length-1);
        onCommand(sender, target, newArgs);
        return true;
    }


    private static final List<String> muted = Arrays.asList(
            "Hey! You can't send messages in mute!",
            "You can appeal your punishment to the moderators.");
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Valkiria valkiria = Main.floader.getValkiria(sender.getName());
        if(valkiria.isMuted())return muted;
        if(args.length<2){
            return new ArrayList<>(Main.floader.getValciriaNames());
        }
        return null;
    }
}