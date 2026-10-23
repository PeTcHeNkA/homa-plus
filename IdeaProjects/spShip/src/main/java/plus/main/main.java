package plus.main;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plus.main.spship.SpaseShip;
import plus.util.YandeRePlugin;
import plus.util.cmd.CommandArgs;
import plus.util.cmd.YandeReCommand;
import java.util.Arrays;
import java.util.logging.Logger;


public class main extends YandeRePlugin {

    public static final Cmd CMD = new Cmd();
    public static main I;
    public static Logger LOG;

    public main(){
        I = this;
        LOG = this.getLogger();
    }

    @Override
    public void onEnable(){
//        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }
}
class Cmd extends YandeReCommand{
    public Cmd() {
        super("spship", "test",
                "debug sp command", Arrays.asList("tst"));
    }
    @Override
    public void onCommand(CommandSender commandSender, String label, CommandArgs args){
        System.out.println("test!");
        Player player = ((Player) commandSender);
        SpaseShip.test(
                player.getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ()
        );
    }
}