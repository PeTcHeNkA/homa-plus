package main;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceHolderSupporter {
    public static void registerApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            System.out.println("TempChat: ERROR PlaceholderAPI not detect");
        } else {
            System.out.println("TempChat: PlaceholderAPI detect");
        }
    }

    public static String PlaceReplace(Player target, String input) {
        try {
            return PlaceholderAPI.setPlaceholders(target, input);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
