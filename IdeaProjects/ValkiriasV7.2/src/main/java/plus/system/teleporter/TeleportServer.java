package plus.system.teleporter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.Main;
import plus.util.sql.CommandExecutor;
import plus.util.sql.QuerryExucator;
import plus.util.sql.YandereDBServer;
import java.util.HashMap;


public class TeleportServer implements QuerryExucator {

    protected final YandereDBServer yandereDBServer;
    protected final HashMap<String, CommandExecutor> commands = new HashMap<>();

    public TeleportServer(int port) {
        this.yandereDBServer = new YandereDBServer(this, port);
        initCommands();
    }

    public void initCommands() {
        commands.put("TP", args -> {
            try {
                String[] rCords = args.get(1).split(";");
                Location l = new Location(
                        Bukkit.getWorld(rCords[0]),
                        Double.parseDouble(rCords[1]),
                        Double.parseDouble(rCords[2]),
                        Double.parseDouble(rCords[3])
                );

                Player player = Bukkit.getPlayer(args.get(0));
                if(player == null){
                    int i = 0;
                    while (player == null){
                        try {
                            Thread.sleep(750);
                            player = Bukkit.getPlayer(args.get(0));
                            if(i==5)break;
                            ++i;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Player finalPlayer = player;
                if(finalPlayer == null)System.out.printf("| Player %s not exist!%n", args.get(0));
                else {
                    BukkitTask r = new BukkitRunnable() {
                        @Override
                        public void run() {
                            finalPlayer.teleport(l);
                        }
                    }.runTaskLater(Main.instance, 0);
                }
            } catch (Exception e){e.printStackTrace();}
            return null;
        });
    }

    @Override
    public CommandExecutor getExucator(String command) {
        return commands.get(command);
    }
}