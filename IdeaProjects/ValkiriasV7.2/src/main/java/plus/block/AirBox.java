package plus.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.chest.Chest;
import plus.util.ConfigHelper;
import plus.system.ModuleLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AirBox{

    private boolean finded = false;

    protected Chest lotBox;

    private final int maxX;
    private final int minX;
    private final int maxY;
    private final int minY;
    private final Random r = new Random();
    private final World w;

    private final Plugin p;
    private final int cdRemove;
    private Location HashLoc;

    public AirBox(String key, int cdremove, String cords, String world, Plugin p, String hashActive) {
        parse(hashActive);
        lotBox = ModuleLoader.ChestC.getChest(key);//ConfigHelper.getChest(key);

        String[] hcords = cords.split(";");
        maxX = Integer.parseInt(hcords[0]);
        minX = Integer.parseInt(hcords[1]);
        maxY = Integer.parseInt(hcords[2]);
        minY = Integer.parseInt(hcords[3]);

        w = Bukkit.getWorld(world);
        if(w==null){
            System.out.println("Wrong world!");
            System.out.println("--- loaded worlds ---");
            for(World a:Bukkit.getWorlds()){
                System.out.println(a.getName());
            }
            System.out.println("----------------------");

        }
        this.p = p;
        cdRemove = cdremove;
        SpawnBlock();
    }


    public void SpawnBlock(){
        HashLoc = getReqLoc();

        HashLoc.setY(HashLoc.getY()+1);
        BukkitTask r = new BukkitRunnable() {
            @Override
            public void run() {
                HashLoc.getBlock().setType(Material.CHEST);
            }
        }.runTaskLater(p, 1);

        String msg = String.format(
                ConfigHelper.spawnBoxFormat,
                HashLoc.getWorld().getName(),
                ((double) HashLoc.getX())/1000,
                ((double) HashLoc.getY())/100,
                ((double) HashLoc.getZ())/1000
        );
        for(Player pl:Bukkit.getOnlinePlayers()){
            pl.sendMessage(msg);
            //p.playSound(p.getLocation(), sound, 3.0F, 0.5F);
        }
        System.out.println(String.format(
                "Spawned at: [%s, %s, %s, %s]",
                HashLoc.getWorld().getName(),
                HashLoc.getX(),
                HashLoc.getY(),
                HashLoc.getZ()
                )
        );
        StationManager.stations.put(HashLoc, new AirBoxDropped(lotBox));
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("Removed.");
                HashLoc.getBlock().setType(Material.AIR);
                StationManager.stations.remove(HashLoc);
                HashLoc = null;
            }
        }.runTaskLater(p, cdRemove * 1200);
        //return HashLoc;
    }


    public Location getRandomLoc(){
        //World w = getRandomWorld();
        int Rx = r.nextInt(maxX-minX)+minX;
        int Ry = r.nextInt(maxY-minY)+minY;
        int y = 254;
        Location l;
        while (y>0) {
            Block b = w.getBlockAt(Rx, y, Ry);
            y-=1;
            if(b.getType() != Material.AIR){
                l = b.getLocation();
                return l;
            }
        }
        return getRandomLoc();
    }


    public Location getReqLoc(){
        while (true){
            Location l = getRandomLoc();
            if(l.getBlock().getType() != Material.WATER)return l;
        }
    }

    private final List<Integer> triggers = new ArrayList<>();

    public boolean active(int nowTime){
        return triggers.contains(nowTime);
    }

    private void parse(String rawTime){
        String[] hs = rawTime.split(":");
        rawTime = hs[1];
        if(hs[0].equals("cd")){
            hs = rawTime.split("-");
            int time = Integer.parseInt(hs[0])*60+Integer.parseInt(hs[1]);
            if(time>1440)time=time%1440;
            if(time==0)time = 1;

            int trigger = 0;
            while (trigger <= 1440) {
                trigger += time;
                triggers.add(trigger);
            }
        } else if(hs[0].equals("tg")){
            for(String hss:rawTime.split(";")){
                String[] t = hss.split("-");
                int time = Integer.parseInt(t[0])*60+Integer.parseInt(t[1]);
                if(time>1440)time=time%1440;
                triggers.add(time);
            }
        }
    }


    public void onDisable(){
        System.out.println("Removed.");
        if(HashLoc==null)return;
        HashLoc.getBlock().setType(Material.AIR);
        StationManager.stations.remove(HashLoc);
    }
}