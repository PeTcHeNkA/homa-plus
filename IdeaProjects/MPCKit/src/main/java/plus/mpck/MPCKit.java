package plus.mpck;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * MMO Placeholders kit (As plugin)
 * A set of useful placeholders for PVP/PVE
 * <br><br>
 * mpc_cur_health - Current health
 * <br>
 * mpc_max_health - Max. Health
 * <br>
 * mpc_damage     - Current damage dealt
 * <br>
 * mpc_armor      - Current "booking" (0-20)
 * <br>
 * mpc_food_lvl   - Current value of food
 *
 * @author HomaPlus
 */
public final class MPCKit extends JavaPlugin {
    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new MPlaceholders().register();
        else
            throw new RuntimeException("PlaceholderAPI not installed");
    }


    @Override
    public void reloadConfig() {
        onEnable();
    }
}