package plus;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

public class PapiHelper {
    public static String Replace(Player target, String input) {
        try {
            return PlaceholderAPI.setPlaceholders(target, input);
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
    }
}
