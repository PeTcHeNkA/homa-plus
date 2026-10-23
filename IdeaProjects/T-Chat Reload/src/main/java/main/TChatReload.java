package main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class TChatReload extends JavaPlugin {

    final private File configfile = new File(getDataFolder()+ File.separator+"config.yml");

    public static String NickLoreFormat;// = "\nNewString\nMyHealth: %player_health% resumestring";

    private static FileConfiguration Config;
    @Override
    public void onEnable() {
        // Plugin startup logic
        if(!configfile.exists()){
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }

        SqLite.Conn();
        SqLite.CreateDB();

        PlaceHolderSupporter.registerApi();

        Config = getConfig();
        //cfgg = getConfig();

        NickLoreFormat = Config.getString("NickLoreFormat").replace('+', '\n').replace('&', '\u00A7');

        int locdist = Config.getInt("LocalDistance");

        String Nullmessage = Config.getString("NullMassage").replace('&', '\u00A7');
        String ColorMention = Config.getString("ColorMention").replace('&', '\u00A7');
        String WrongNickName = Config.getString("WrongNickName").replace('&', '\u00A7');

        Bukkit.getPluginManager().registerEvents(new ChatHundler(
                Config.getString("SeporatorMsg").replace('&', '\u00A7'),
                Config.getString("SeporatorPrf").replace('&', '\u00A7'),
                Config.getString("PrefixGlobal").replace('&', '\u00A7'),
                Config.getString("PrefixLocal").replace('&', '\u00A7'),
                Config.getString("PrefixPrivate").replace('&', '\u00A7'),
                Config.getString("PrivateSeporatorTo").replace('&', '\u00A7'),
                Config.getString("PrivateSeporatorFr").replace('&', '\u00A7'),
                locdist,
                ColorMention,
                Nullmessage,
                Config.getString("NullNickName").replace('&', '\u00A7'),
                WrongNickName,

                Config.getString("ColorGloval").replace('&', '\u00A7'),
                Config.getString("ColorLocal").replace('&', '\u00A7'),
                Config.getString("ColorPrivate").replace('&', '\u00A7')


                ), this);

        Bukkit.getPluginManager().registerEvents(new JoinHundler(), this);
        String Manyargs = Config.getString("ManyArgs").replace('&', '\u00A7');
        String HelpfulInfo = Config.getString("HelpfulInfo").replace('&', '\u00A7').replace('+', '\n');
        int maxavatar = Config.getInt("MaxAvatarNumber");

        this.getCommand("profile").setExecutor(new CommandHelper(
                Manyargs,
                Config.getString("ChooseColor").replace('&', '\u00A7'),
                Config.getString("ChoosePrefix").replace('&', '\u00A7'),
                Config.getString("ChooseAvatar").replace('&', '\u00A7'),
                Config.getString("HelpfulInfo").replace('+', '\n').replace('&', '\u00A7'),
                Config.getString("ChooseAvatarToDefault").replace('&', '\u00A7'),
                maxavatar,
                Config.getString("ClearPrefix").replace('&', '\u00A7'),
                Config.getString("WrongColor").replace('+', '\n').replace('&', '\u00A7').replace('$','&'),
                Config.getString("NoPerms").replace('&', '\u00A7'),

                Config.getString("ChatAvatarFormat1").replace('&', '\u00A7'),
                Config.getString("ChatAvatarFormat2").replace('&', '\u00A7'),
                Config.getString("ChatAvatarFormat3").replace('&', '\u00A7'),
                Config.getString("WrongPrefix").replace('&', '\u00A7'),

                Config.getString("ClearColor").replace('&', '\u00A7'),
                Config.getString("ClearAvatar").replace('&', '\u00A7'),

                Config.getString("ChooseLinc").replace('&', '\u00A7'),
                Config.getString("NullLinc").replace('&', '\u00A7'),
                Config.getString("ClearLinc").replace('&', '\u00A7')

                )
        );

        this.getCommand("savechatuserdata").setExecutor(new Floader());
        this.getCommand("msg").setExecutor(new ChatPrivate());
        this.getCommand("r").setExecutor(new ChatResend(
                WrongNickName,
                Nullmessage
        ));

        this.getCommand("me").setExecutor(new ChatMe(
                ColorMention,
                Nullmessage,
                Config.getString("FormateMe").replace('&', '\u00A7'),
                Config.getString("ConsoleSender").replace('&', '\u00A7'),
                Config.getString("ConsoleSenderLore").replace('&', '\u00A7').replace('+', '\n'),
                Config.getString("ColorMe").replace('&', '\u00A7')


        ));
        //this.getCommand("chathelp").setExecutor(new ChatHelp());

        this.getCommand("achat").setExecutor(new ChatAdvanture(
                Manyargs,
                Config.getString("MessageAdvanture").replace('&', '\u00A7'),
                locdist
            )
        );


        for (Player PlayerOnline : Bukkit.getOnlinePlayers()) {
            if(!Floader.hasUser(PlayerOnline)) {
                Floader.addUser(PlayerOnline.getName());
            }
        }


    }

    @Override
    public void onDisable() {
        Floader.saveAllData();
    }

    public static List<String> lianes(String floader){
        //Config.getStringList(floader);

        return Config.getStringList(floader);
    }

}
