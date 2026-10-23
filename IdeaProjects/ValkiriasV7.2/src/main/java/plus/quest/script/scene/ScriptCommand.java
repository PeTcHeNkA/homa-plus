package plus.quest.script.scene;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import java.util.List;


public class ScriptCommand implements UniversalScript{

    protected final String command;
    protected final boolean asConsole;

    public ScriptCommand(Sector s, String path){
        command = s.getOrSetString(path+".command", "/example %player%");
        asConsole = s.getOrSetBoolean(path+".as_console", true);
    }


    @Override
    public void run(Player player) {
        if(asConsole){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        } else {
            Bukkit.dispatchCommand(player, command.replace("%player%", player.getName()));
        }
    }


    @Override
    public void run(List<Player> players, int uid, BasicPos pos) {
        if(asConsole){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            for (Player player:players){
                Bukkit.dispatchCommand(player, command.replace("%player%", player.getName()));
            }
        }
    }
}