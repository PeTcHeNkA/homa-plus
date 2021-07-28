package main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ChatHundler implements Listener{//, CommandExecutor

    private static TextComponent sepotator_msg;
    private static String colormail;

    private final TextComponent seporator_prf;
    private final TextComponent chat_global;
    private final TextComponent chat_local;
    private static TextComponent chat_private;
    private static TextComponent chat_private_sp_to;
    private static TextComponent chat_private_sp_fr;

    private static String NullMassage;
    private static String NullNickName;
    private static String WrongNickName;

    private int localdistanse = 101;

    private final String GlobalColor;
    private final String LocalColor;
    private static String PrivateColor;

    public ChatHundler(String sm, String sp, String chg, String chl, String chp, String spt, String spf, int lc, String colm, String nullMassage, String nullNickName, String wrongNickName, String glcl, String lccl, String prrcl){
        sepotator_msg = new TextComponent(sm);
        seporator_prf = new TextComponent(sp);
        chat_global = new TextComponent(chg);
        chat_local = new TextComponent(chl);
        chat_private = new TextComponent(chp);
        chat_private_sp_to = new TextComponent(spf);
        chat_private_sp_fr = new TextComponent(spt);
        localdistanse = lc;
        colormail = colm;
        NullMassage = nullMassage;
        NullNickName = nullNickName;
        WrongNickName = wrongNickName;
        GlobalColor = glcl;
        LocalColor = lccl;
        PrivateColor = prrcl;

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent event){
        //Логика которая чекает на мут
        event.setCancelled(true);

        ChatUser user = Floader.getUser(event.getPlayer().getName());

        TextComponent NickName = new TextComponent(user.getName());
        //PlaceHolderSupporter.PlaceReplace(event.getPlayer(), user.getNickInfo());
        boolean global = false;
        String activeColor = LocalColor;

        String strmessage = event.getMessage();

        if(strmessage.charAt(0) == '!'){
            strmessage = strmessage.substring(1);
            global = true;
            activeColor = GlobalColor;
        }


        List<TextComponent> components = new ArrayList();
        if(event.getPlayer().hasPermission("TChat.mention")) {
            int indexofmail = strmessage.indexOf("@");
            while (indexofmail != -1) {
                String pplayer;

                components.add(new TextComponent(activeColor+strmessage.substring(0, indexofmail)));
                strmessage = strmessage.substring(indexofmail);
                int index = strmessage.indexOf(" ");

                boolean notfinishstring = (index != -1);
                if (notfinishstring) {
                    pplayer = strmessage.substring(1, index);
                } else {
                    pplayer = strmessage.substring(1);
                }

                if (Bukkit.getPlayer(pplayer) != null) {

                    Player menti = Bukkit.getPlayer(pplayer);

                    TextComponent Men = new TextComponent(colormail + "@" + pplayer);
                    Men.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new Text(PlaceHolderSupporter.PlaceReplace(menti, Floader.getUser(pplayer).getNickInfo()))
                            )
                    );

                    if(user.hasLinc){
                        Men.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                    }

                    components.add(Men);

                    if (notfinishstring) {
                        strmessage = strmessage.substring(index);
                    } else {
                        strmessage = "";
                    }

                } else {
                    //System.out.println(components.size());
                    //components.get(components.size()).getText();

                    components.add(new TextComponent(activeColor+"@"+pplayer));

                    if (notfinishstring) {
                        strmessage = strmessage.substring(index);
                    } else {
                        strmessage = "";
                    }
                }
                indexofmail = strmessage.indexOf("@");
                //System.out.println(strmessage);
                //System.out.println(pplayer);

            }
        }
        if(!strmessage.equals("")) {
            components.add(new TextComponent(activeColor+strmessage));
        }

        TextComponent[] messagecomponents = components.toArray(new TextComponent[components.size()]);

        NickName.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(PlaceHolderSupporter.PlaceReplace(event.getPlayer(), user.getNickInfo()))
                )
        );
        if(user.hasLinc){
            NickName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
        }

        if(user.hasPrefix){
            TextComponent Prefix = new TextComponent(user.getTextprefix());

            if(user.hasLorePrefix) {
                Prefix.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(PlaceHolderSupporter.PlaceReplace(event.getPlayer(), user.getLorePrefix()))
                        )
                );
            }

            if(global) {
                TextComponent[] form = {chat_global, Prefix, seporator_prf, NickName, sepotator_msg};
                for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {

                    PlayerOnline.spigot().sendMessage(addargs(form, messagecomponents));
                }
                //user.toPlayer().spigot().sendMessage(chat_global, Prefix, seporator_prf, NickName, sepotator_msg,Message);
            } else {
                Location loc = event.getPlayer().getLocation().clone();
                TextComponent[] form = {chat_local, Prefix, seporator_prf, NickName, sepotator_msg};

                for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {

                    //System.out.println("========");
                    //System.out.println(PlayerOnline.getWorld());
                    //System.out.println(loc.getWorld());
                    //System.out.println(loc);
                    //System.out.println(PlayerOnline.getLocation());

                    if(PlayerOnline.getWorld() == loc.getWorld()) {
                        //System.out.println(PlayerOnline.getLocation().distanceSquared(loc));
                        //System.out.println(localdistanse);

                        if (PlayerOnline.getLocation().distanceSquared(loc) < localdistanse) {
                            PlayerOnline.spigot().sendMessage(addargs(form, messagecomponents));
                        }
                    }
                }
            }

        } else {

            if(global) {
                TextComponent[] form = {chat_global, NickName, sepotator_msg};
                for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {
                    PlayerOnline.spigot().sendMessage(addargs(form, messagecomponents));
                }

            } else {
                TextComponent[] form = {chat_local, NickName, sepotator_msg};
                for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {

                    Location loc = event.getPlayer().getLocation().clone();

                    if (PlayerOnline.getWorld() == loc.getWorld()) {
                        if (PlayerOnline.getLocation().distanceSquared(loc) < localdistanse) {
                            PlayerOnline.spigot().sendMessage(addargs(form, messagecomponents));
                        }
                    }
                }
            }
        }

    }

    //@Override
    public static boolean privateMessage(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)){
            System.out.println("This command only to Player!");
            return true;
        }
        final Player player = (Player) sender;

        if(args.length <2){
            if(args.length == 1){
                player.sendMessage(NullMassage);
                return true;
            } else {
                player.sendMessage(NullNickName);
                return true;
            }
        }
        if(Bukkit.getPlayer(args[0]) == null){
            player.sendMessage(WrongNickName);
            return true;
        }

        String message = "";
        List<TextComponent> components = new ArrayList();

        for(int i = 1; i < args.length ; i++) {
            //message = message+ " " +args[i];

            if(args[i].charAt(0) == '@'){
                String metion = args[i];
                if(Bukkit.getPlayer(metion.substring(1)) != null){
                    Player targetmetion = Bukkit.getPlayer(metion.substring(1));
                    ChatUser user = Floader.getUser(metion.substring(1));

                    components.add(new TextComponent(PrivateColor+message));

                    TextComponent Met = new TextComponent(colormail + metion + "");

                    Met.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(PlaceHolderSupporter.PlaceReplace(targetmetion,
                                    user.getNickInfo()))
                        )
                    );

                    if(user.hasLinc){
                        Met.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                    }

                    components.add(Met);
                    message = "";
                } else {
                    message = message+" "+metion;
                }
            } else {
                message = message+ " " +args[i];
            }


        }
        components.add(new TextComponent(PrivateColor+message));

        TextComponent[] Messages = components.toArray(new TextComponent[components.size()]);


        TextComponent Message = new TextComponent(message);


        final Player target = Bukkit.getPlayer(args[0]);

        ChatUser sendr = Floader.getUser(player.getName());
        ChatUser trget = Floader.getUser(target.getName());

        TextComponent targetName = new TextComponent(trget.getName());

        targetName.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(PlaceHolderSupporter.PlaceReplace(target, trget.getNickInfo()))
                )
        );
        if(trget.hasLinc){
            targetName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, trget.getLinc()));
        }

        TextComponent senderName = new TextComponent(sendr.getName());

        senderName.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(PlaceHolderSupporter.PlaceReplace(player, sendr.getNickInfo()))
                )
        );
        if(sendr.hasLinc){
            senderName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sendr.getLinc()));
        }

        sendr.setLastMessageTarget(target.getName());
        trget.setLastMessageTarget(sender.getName());

        //Логика префиксов в личке
        TextComponent[] finalmessageS= {chat_private, senderName, chat_private_sp_to, targetName, sepotator_msg};
        TextComponent[] finalmessageT= {chat_private, targetName, chat_private_sp_fr, senderName, sepotator_msg};


        player.spigot().sendMessage(addargs(finalmessageS ,Messages));

        target.spigot().sendMessage(addargs(finalmessageT ,Messages));

        return true;
    }
    public static TextComponent[] addargs(TextComponent[] a, TextComponent[] b) {

        TextComponent[] r = new TextComponent[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

}
