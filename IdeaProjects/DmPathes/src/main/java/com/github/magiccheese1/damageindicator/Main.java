//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.github.magiccheese1.damageindicator;

import com.github.magiccheese1.damageindicator.versions.PacketManager;
import com.github.magiccheese1.damageindicator.versions.PacketManager1_16_R3;
import com.github.magiccheese1.damageindicator.versions.PacketManager1_17_R1;
import com.github.magiccheese1.damageindicator.versions.PacketManager1_18_R1;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import plus.YandeRePlugin;
import plus.util.Config.Config;

public class Main extends YandeRePlugin {

    public static PacketManager pm;
    public static String DAMAGE_FORMAT;
    public static double VIEW_DISTANCE;

    public Main() {
    }

    public void onEnable() {
        //this.saveDefaultConfig();
        //File configFile = new File(this.getDataFolder(), "config.yml");
        //try {
        //    ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
        //} catch (IOException var6) {
        //    var6.printStackTrace();
        //}
        reloadConfig();

        //this.reloadConfig();
        this.getCommand("damageindicator").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String string, String[] strings) {
                if(commandSender.isOp()){
                    try {
                        if(strings[0].equals("reload")){
                            reloadConfig();
                            commandSender.sendMessage("Reloaded.");
                        }
                    } catch (Exception ignored){}
                }
                return true;
            }
        });


        String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].trim();
        byte var5 = -1;
        switch(serverVersion.hashCode()) {
            case -1497046089:
                if (serverVersion.equals("v1_16_R3")) {
                    var5 = 0;
                }
                break;
            case -1497016300:
                if (serverVersion.equals("v1_17_R1")) {
                    var5 = 1;
                }
                break;
            case -1496986509:
                if (serverVersion.equals("v1_18_R1")) {
                    var5 = 2;
                }
        }

        Object packetManager;
        switch(var5) {
            case 0:
                packetManager = PacketManager1_16_R3.make();
                break;
            case 1:
                packetManager = PacketManager1_17_R1.make();
                break;
            case 2:
                packetManager = PacketManager1_18_R1.make();
                break;
            default:
                throw new RuntimeException("Failed to create version specific server accessor");
        }

        this.getLogger().info(String.format("Using server version accessor for %s", serverVersion));

        pm = (PacketManager)packetManager;

    }
    @Override
    public void reloadConfig(){
        super.reloadConfig();
        Config c = getOrCreateYandeReConfig("settings.yml");
        if(!c.contains("indicator.distance")){
            c.set("indicator.distance", 16.5);
            c.saveChanges();
        }
        VIEW_DISTANCE = c.getDouble("indicator.distance");
        DAMAGE_FORMAT = c.getOrSetString("indicator.format", "&6&l%.2f", true);
    }
}