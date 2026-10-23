package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Floader implements CommandExecutor {
    private static HashMap<String, ChatUser> data = new HashMap<>();

    public static void addUser(String name) {

        if(data.containsKey(name)) {
            return;
        }
        //System.out.println(name);

        ChatUser user = SqLite.loadChatUser(name);

        data.put(name, user);

    }
    public static boolean hasUser(Player player){
        return data.containsKey(player.getName());
    }
    public static boolean hasUser(String player){
        return data.containsKey(player);
    }

    public static ChatUser getUser(String name) {

        return data.get(name);
    }

    public static void saveAllData() {
        System.out.println("Start save data users TChat: Reload");
        for (ChatUser user : data.values()) {
            SqLite.saveChatUser(user);
        }
        System.out.println("save finished.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("tchatreload.data")){
            return true;
        }
        saveAllData();

        return true;
    }
}