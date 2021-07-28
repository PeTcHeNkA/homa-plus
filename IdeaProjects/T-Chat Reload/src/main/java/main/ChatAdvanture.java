package main;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ChatAdvanture implements CommandExecutor {

    private String ManyArgs;
    private String MessageAdvanture;
    private int localdistanse;

    public ChatAdvanture(String manyArgs, String messageAdvanture, int ldist){
        ManyArgs = manyArgs;
        MessageAdvanture = messageAdvanture;
        localdistanse = ldist;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println("This command only to Player!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ManyArgs);
            return true;
        }

        if (Bukkit.getPlayer(args[0]) != null) {
            //System.out.println("nennn");
            Player target = Bukkit.getPlayer(args[0]);


            String[] newargs = {args[0], MessageAdvanture};
            ChatHundler.privateMessage(sender, command, label, newargs);
            String[] adventureargs = Arrays.copyOfRange(args, 1, args.length);

            target.spigot().sendMessage(
                    AdvantureChatBuilder.parseComponents(player, adventureargs)
            );
            return true;
        }
        if (args[0].equals("global") || args[0].equals("all")) {



            String[] adventureargs = Arrays.copyOfRange(args, 1, args.length);

            TextComponent[] messages = AdvantureChatBuilder.parseComponents(player, adventureargs);

            for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {
                String[] newargs = {PlayerOnline.getName(), MessageAdvanture};
                ChatHundler.privateMessage(sender, command, label, newargs);
                PlayerOnline.spigot().sendMessage(messages);
            }
            return true;
        }
        if (args[0].equals("local") || args[0].equals("loc")) {

            String[] adventureargs = Arrays.copyOfRange(args, 1, args.length);

            TextComponent[] messages = AdvantureChatBuilder.parseComponents(player, adventureargs);

            Location loc = player.getLocation();
            for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {
                String[] newargs = {PlayerOnline.getName(), MessageAdvanture};
                ChatHundler.privateMessage(sender, command, label, newargs);
                if (PlayerOnline.getLocation().distanceSquared(loc) < localdistanse) {
                    PlayerOnline.spigot().sendMessage(messages);
                }

            }
            return true;

        }
        return true;
    }


}
