package plus.music;

import org.bukkit.Bukkit;
import org.bukkit.World;
import plus.utils.config.Sector;


public class TimeController {

    public final int m;
    public final int d;
    public final int e;
    public final int n;
    public final int mId;
    public final int dId;
    public final int eID;
    public final int nID;
    public final World w;

    public TimeController(Sector c, String path){
        this(
                c.getOrSetInt(path+".time_controller.morning.time", 22500),
                c.getOrSetInt(path+".time_controller.day.time", 500),
                c.getOrSetInt(path+".time_controller.evening.time", 12500),
                c.getOrSetInt(path+".time_controller.night.time", 14000),

                c.getOrSetInt(path+".time_controller.morning.id", 1),
                c.getOrSetInt(path+".time_controller.day.id", 2),
                c.getOrSetInt(path+".time_controller.evening.id", 3),
                c.getOrSetInt(path+".time_controller.night.id", 4),
                getWorld(c.getOrSetString(path+".time_controller.world", "world"))
        );
    }


    private static World getWorld(String world){
        World w = Bukkit.getWorld(world);
        if(w==null){
            System.out.println("| Wrong world name! Allowed only");
            for (World wr:Bukkit.getWorlds()){
                System.out.printf("| [%s]%n", wr.getName());
            }
            return Bukkit.getWorlds().get(0);
        } else return w;
    }


    public TimeController(int morning, int day, int evening, int night, int mId, int dId, int eId, int nId, World world){
        m = morning;d = day;e = evening;n = night;
        this.nID = nId;this.mId = mId;this.dId = dId;this.eID = eId;
        this.w = world;
    }


    public int getTimeId(){
        long time = w.getTime();
        if(time>n){
            if(time>m){
                return mId;
            } else return nID;
        } else {
            if(time>e) return eID;
            else {
                if (time > d) return dId;
                else return mId;
            }
        }
    }
}