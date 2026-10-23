package main;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements CommandExecutor {

    private String Manyargas;
    private String ChooseColor;
    private String ChoosePrefix;
    private String ChooseAvatar;
    private String ChooseAvatarToDefault;
    //private static String Help;
    private int Maxavatar;
    private int CountListAvatars;
    private String ClearPrefix;
    private String WrongColor;
    private String NoPerms;
    private String WrongPrefix;

    private TextComponent next;
    private TextComponent last;
    private String[] avararsstr;
    private TextComponent PageGardeFormat;
    private String AvatarsFormat;
    private TextComponent lastsimple;

    private TextComponent info;
    //private TextComponent[] listinfo;

    private int MaxFullAvatars;

    private String ClearColor;
    private String ClearAvatar;
    private String ChooseLinc;
    private String NullLinc;
    private String ClearLinc;

    public CommandHelper(String manyargas, String chooseColor, String choosePrefixTo,
                         String chooseAvatar, String help, String chooseAvatarToDefault,
                         int maxavatar, String clearPrefix, String wrongColor, String noPerms,
                         String PageAvatarsFormat1, String PageAvatarsFormat2, String PageAvatarsFormat3, String wrongPrefix,
                         String clearColor, String clearAvatar, String chooseLinc, String nullLinc, String clearLinc
    ){
        ChooseColor = chooseColor;
        Manyargas = manyargas;
        ChoosePrefix = choosePrefixTo;
        ChooseAvatar = chooseAvatar;
        //Help = help;
        ChooseAvatarToDefault = chooseAvatarToDefault;
        Maxavatar = maxavatar+4097;
        WrongColor = wrongColor;
        NoPerms = noPerms;
        CountListAvatars = (maxavatar/8)+1;

        avararsstr = new String[CountListAvatars];
        MaxFullAvatars = maxavatar;
        AvatarsFormat = PageAvatarsFormat1;
        next = new TextComponent(PageAvatarsFormat2+" >> ");
        last = new TextComponent("\n\n\n\n"+PageAvatarsFormat2+" << ");
        lastsimple = new TextComponent("\n"+PageAvatarsFormat2+" << ");
        PageGardeFormat = new TextComponent("\n"+PageAvatarsFormat3);
        WrongPrefix = wrongPrefix;

        ClearPrefix = clearPrefix;
        ClearColor = clearColor;
        ClearAvatar = clearAvatar;

        ChooseLinc = chooseLinc;
        NullLinc = nullLinc;
        ClearLinc = clearLinc;

        info = new TextComponent("\n"+help);

        //listinfo = AdvantureChatBuilder.consoleParser(TChatReload.lianes("test"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            System.out.println("This command only to Player!");
            return true;
        }

        Player player = (Player) sender;

        if(args.length == 0){
            String[] argg = {"av"};
            args = argg;
        }

        if(args[0].equals("avatars") || args[0].equals("av") || args[0].equals("help")){

            int numberlist = 1;
            boolean inf = true;

            try {
                numberlist = Integer.parseInt(args[1]);
                if(numberlist == 0 || numberlist>CountListAvatars || numberlist<0) {
                    inf = false;
                    numberlist = 1;
                }
            }
            catch (Exception nfe) {
                inf = false;
            }
            numberlist = numberlist-1;
            Player Sender =  (Player) sender;

            if(inf) {

                next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile av "+String.valueOf(numberlist+2)));
                last.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile av "+String.valueOf(numberlist)));
                //numberlist = numberlist-1;
                if(avararsstr[numberlist] == null) {

                    avararsstr[numberlist] = generateListAvatars(numberlist+1);
                }

                Sender.spigot().sendMessage(PageGardeFormat,
                        new TextComponent("\n"+avararsstr[numberlist]),
                        last,
                        new TextComponent(AvatarsFormat+" "+String.valueOf(numberlist+1)+"/"+String.valueOf(CountListAvatars)), next);
            } else {
                next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile av 1"));
                lastsimple.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile av "+String.valueOf(CountListAvatars)));
                Sender.spigot().sendMessage(PageGardeFormat, info, lastsimple,
                        new TextComponent(AvatarsFormat+" "+"Info"), next);
            }
            return true;

        }

        ChatUser user = Floader.getUser(player.getName());

        if(args[0].equals("setcolor")){

            if(player.hasPermission("TChat.color")) {

                if (args.length == 2) {
                    String newcolor = args[1];
                    if (newcolor.charAt(1) != '&' && newcolor.length() != 2) {
                        player.sendMessage(WrongColor);
                        return true;
                    }

                    user.setColor(args[1]);
                    //player.sendMessage(ChooseColor + args[1] + " , " + user.getName());
                    TextComponent NickName = new TextComponent(user.getName());
                    NickName.setHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new Text(PlaceHolderSupporter.PlaceReplace(player, user.getNickInfo()))
                            )
                    );
                    if(user.hasLinc){
                        NickName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                    }

                    if(user.hasPrefix){
                        TextComponent Prefix = new TextComponent(user.getTextprefix());
                        Prefix.setHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new Text(PlaceHolderSupporter.PlaceReplace(player, user.getLorePrefix()))
                                )
                        );

                        player.spigot().sendMessage(
                                new TextComponent(ChooseColor + args[1] + " , "),
                                Prefix,
                                new TextComponent(" "),
                                NickName
                        );

                    } else {
                        player.spigot().sendMessage(
                                new TextComponent(ChooseColor + args[1] + " , "),
                                NickName
                        );

                    }
                    return true;
                } else {
                    player.sendMessage(WrongColor);
                    return true;
                }
            } else {
                player.sendMessage(NoPerms);
                return true;
            }
        }

        if(args[0].equals("setprefix")){
            if(player.hasPermission("TChat.prefix")) {
                if(args.length == 1){
                    player.sendMessage(WrongPrefix);
                    return true;
                }

                TextComponent NickName = new TextComponent(user.getName());
                NickName.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(PlaceHolderSupporter.PlaceReplace(player, user.getNickInfo()))
                        )
                );
                if(user.hasLinc){
                    NickName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                }

                if (args.length == 2) {
                    user.setPrefix(args[1]);


                    player.spigot().sendMessage(
                            new TextComponent(ChoosePrefix + args[1] + " , "+ user.getTextprefix() + " "),
                            NickName
                    );

                    return true;
                } else {
                    user.setPrefix(args[1], args[2]);

                    TextComponent Prefix = new TextComponent(user.getTextprefix());
                    Prefix.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new Text(PlaceHolderSupporter.PlaceReplace(player, user.getLorePrefix()))
                            )
                    );


                    player.spigot().sendMessage(
                            new TextComponent(ChoosePrefix + args[1] + " , "),
                            Prefix,
                            new TextComponent(" "),
                            NickName);


                    return true;
                }
            } else {
                player.sendMessage(NoPerms);
                return true;
            }
        }
        if(args[0].equals("setavatar")){

            if(player.hasPermission("TChat.avatar")) {
                int avatarID = 4097;
                boolean wrongarg = false;
                try {
                    avatarID = Integer.parseInt(args[1]) + 4097;
                } catch (Exception err) {
                    wrongarg = true;
                }
                if (avatarID > Maxavatar) {
                    avatarID = Maxavatar;
                }

                user.setAvatar(avatarID);

                TextComponent NickName = new TextComponent(user.getName());
                NickName.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(PlaceHolderSupporter.PlaceReplace(player, user.getNickInfo()))
                        )
                );
                if(user.hasLinc){
                    NickName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, user.getLinc()));
                }

                if (wrongarg) {
                    if(user.hasPrefix){
                        if(user.hasLorePrefix) {

                            TextComponent Prefix = new TextComponent(user.getTextprefix());

                            Prefix.setHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(PlaceHolderSupporter.PlaceReplace(player, user.getLorePrefix()))
                                    )
                            );

                            player.spigot().sendMessage(
                                    new TextComponent(ChooseAvatarToDefault),
                                    Prefix,
                                    new TextComponent(" "),
                                    NickName
                            );
                        } else {
                            player.spigot().sendMessage(
                                    new TextComponent(ChooseAvatarToDefault+user.getTextprefix()+" "),
                                    NickName
                            );
                        }
                    } else {
                        player.spigot().sendMessage(
                                new TextComponent(ChooseAvatarToDefault),
                                NickName
                        );
                    }
                } else {
                    //player.sendMessage(ChooseAvatar + args[1] + ", " + user.getName());
                    if(user.hasPrefix){
                        if(user.hasLorePrefix) {

                            TextComponent Prefix = new TextComponent(user.getTextprefix());

                            Prefix.setHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(PlaceHolderSupporter.PlaceReplace(player, user.getLorePrefix()))
                                    )
                            );

                            player.spigot().sendMessage(
                                    new TextComponent(ChooseAvatar + args[1] + ", "),
                                    Prefix,
                                    new TextComponent(" "),
                                    NickName
                            );
                        } else {
                            player.spigot().sendMessage(
                                    new TextComponent(ChooseAvatar + args[1] + ", "+user.getTextprefix()+" "),
                                    NickName
                            );
                        }
                    } else {
                        player.spigot().sendMessage(
                                new TextComponent(ChooseAvatar + args[1] + ", "),
                                NickName
                        );
                    }

                }

            } else {
                player.sendMessage(NoPerms);
            }
            return true;
        }
        if(args[0].equals("setlinc")){
            if(player.hasPermission("TChat.linc")){
                if(args.length == 2){

                    user.setLinc(args[1]);

                    TextComponent NickName = new TextComponent(user.getName());
                    NickName.setHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new Text(PlaceHolderSupporter.PlaceReplace(player, user.getNickInfo()))
                            )
                    );

                    NickName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, args[1]));

                    if(user.hasPrefix){
                        if(user.hasLorePrefix) {

                            TextComponent Prefix = new TextComponent(user.getTextprefix());

                            Prefix.setHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(PlaceHolderSupporter.PlaceReplace(player, user.getLorePrefix()))
                                    )
                            );

                            player.spigot().sendMessage(
                                    new TextComponent(ChooseLinc),
                                    Prefix,
                                    new TextComponent(" "),
                                    NickName
                            );
                        } else {
                            player.spigot().sendMessage(
                                    new TextComponent(ChooseLinc+user.getTextprefix()+" "),
                                    NickName
                            );
                        }
                    } else {
                        player.spigot().sendMessage(
                                new TextComponent(ChooseLinc),
                                NickName
                        );
                    }


                } else {
                    player.sendMessage(NullLinc);

                }

            } else {
                player.sendMessage(NoPerms);
            }
            return true;
        }

        if(args[0].equals("clear")){
            if(args[1].equals("prefix")) {
                player.sendMessage(ClearPrefix);
                user.ClearPrefix();
                return true;
            }

            if(args[1].equals("ClearLinc")){
                user.setLinc(null);
                player.sendMessage(ClearLinc);
            }

            if(args[1].equals("color")) {

                user.setColor("&f");
                player.sendMessage(ClearColor);

                return true;
            }

            if(args[1].equals("avatar")){
                user.setAvatar(4097);
                player.sendMessage(ClearAvatar);

            }
        }
        return false;
    }

    public String generateListAvatars(int nomber) {

        String avatarslist = "";
        int rangeavatars = 4097+(nomber*8);

        for(int i=rangeavatars-8; i<rangeavatars; ++i) {

            if(i<Maxavatar) {
                avatarslist = avatarslist+String.valueOf((char )i);
            }

        }
        return avatarslist;

    }

}
