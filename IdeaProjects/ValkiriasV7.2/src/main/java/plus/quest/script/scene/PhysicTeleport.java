package plus.quest.script.scene;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;

import java.util.List;


public class PhysicTeleport implements UniversalScript{

    protected final BasicPos motion;
    protected final boolean isRelative;

    public PhysicTeleport(Sector s, String path) {
        this.motion = TpMotion.build(s.getOrSetString(path+".location", "0;0;0"));
        this.isRelative = s.getOrSetBoolean(path+".relative", true);
    }


    @Override
    public void run(Player player) {
        Location old = player.getLocation();
        tp(player, old.getX(), old.getY(), old.getZ());
    }


    private void tp(Player player, double x, double y, double z){
        Location old = player.getLocation();
        Location loc;
        if(isRelative) {
            loc = new Location(
                    old.getWorld(),
                    motion.X + x, motion.Y + y, motion.Z + z,
                    old.getYaw(), old.getPitch()
            );
        } else {
            loc = new Location(old.getWorld(), motion.X, motion.Y, motion.Z, old.getYaw(), old.getPitch());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc);
            }
        }.runTaskLater(Main.instance, 0);
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        tp(player, pos.X, pos.Y, pos.Z);
    }


    @Override
    public void run(List<Player> players, int uid, BasicPos pos) {
        double x; double y; double z;
        if(isRelative) { x = motion.X + pos.X; y = motion.Y + pos.Y;z = motion.Z + pos.Z; }
        else { x = motion.X;y = motion.Y;z = motion.Z; }

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player:players){
                    Location old = player.getLocation();
                    player.teleport(new Location(old.getWorld(), x, y, z, old.getYaw(), old.getPitch()));
                }
            }
        }.runTaskLater(Main.instance, 0);
    }
}