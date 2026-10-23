package main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChatMe implements CommandExecutor {

    private String colormail;
    private String NullMassage;
    private String ColorMe;
    //private String FormateMe;
    private TextComponent FormateMe;
    private TextComponent ConsoleSender;

    public ChatMe(String Colormail, String nullMassage, String formateMe, String consoleSender, String consolelore, String colorMe){
        colormail = Colormail;
        NullMassage = nullMassage;
        FormateMe = new TextComponent(formateMe);
        ConsoleSender = new TextComponent(consoleSender);

        ConsoleSender.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(consolelore)
                )
        );
        ColorMe = colorMe;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length <1){
            sender.sendMessage(NullMassage);
            return true;
        }

        String message = ColorMe;
        List<TextComponent> components = new ArrayList();
        components.add(FormateMe);

        if(sender instanceof Player){
            TextComponent senr;
            ChatUser send = Floader.getUser(sender.getName());
            senr = new TextComponent(send.getName());

            senr.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(PlaceHolderSupporter.PlaceReplace((Player) sender, send.getNickInfo()))
                )
            );

            if(send.hasLinc){
                senr.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, send.getLinc()));
            }

            components.add(senr);
        } else {
            components.add(ConsoleSender);
        }

        for(String arg : args) {
            //message = message+ " " +args[i];
            if(arg.charAt(0) == '@'){
                //String metion = arg;
                if(Bukkit.getPlayer(arg.substring(1)) != null){
                    Player targetmetion = Bukkit.getPlayer(arg.substring(1));
                    ChatUser user = Floader.getUser(arg.substring(1));

                    components.add(new TextComponent(message));

                    TextComponent Met = new TextComponent(colormail + arg + "");

                    Met.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new Text(PlaceHolderSupporter.PlaceReplace(targetmetion,
                                            user.getNickInfo()))
                            )
                    );

                    if(user.hasLinc){
                        Met.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                    }

                    components.add(Met);
                    message = ColorMe;
                }
            } else {
                message = message+ " " +arg;
            }

        }

        components.add(new TextComponent(message));

        TextComponent[] messagecomponents = components.toArray(new TextComponent[components.size()]);

        for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {
            PlayerOnline.spigot().sendMessage(messagecomponents);
        }

        return true;
    }
}
