package plus.utls;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plus.main;

import java.util.ArrayList;
import java.util.List;

public class LootBoxUtls {

    public List<World> w = new ArrayList<>();
    public List<Integer> MaxX = new ArrayList<>();
    public List<Integer> MinX = new ArrayList<>();

    public Location HashLoc;
    public Inventory inv;
    public int LastIndexRandomWorld = 0;

    public String messageFormat;

    public int cdRemove;

    public String sound = "";

    Plugin p;

    public boolean finded = false;

    public LootBoxUtls(Plugin p){
        this.p = p;
        ReSetValues();
    }

    public void generateBlock(){
        Location l = getReqLoc();
        l.setY(l.getY()+1);
        BukkitTask r = new BukkitRunnable() {
            @Override
            public void run() {
                l.getBlock().setType(Material.CHEST);
            }
        }.runTaskLater(p, 1);
        inv = generateRandomInv(54);
        HashLoc = l;
    }
    public Inventory generateRandomInv(int Size){
        Inventory i = Bukkit.createInventory(null, Size);
        i.setContents(main.inv.getRandomContent(Size));
        return i;
    }

    public void SpawnBlock(){
        finded = false;
        generateBlock();
        String msg = String.format(
                messageFormat,
                HashLoc.getWorld().getName(),
                ((double) HashLoc.getX())/1000,
                ((double) HashLoc.getY())/100,
                ((double) HashLoc.getZ())/1000
        );
        for(Player p:Bukkit.getOnlinePlayers()){
            p.sendMessage(msg);
            p.playSound(p.getLocation(), sound, 3.0F, 0.5F);
        }
        System.out.println(String.format(
                "Spawned at: [%s, %s, %s, %s]",
                HashLoc.getWorld().getName(),
                HashLoc.getX(),
                HashLoc.getY(),
                HashLoc.getZ())
        );
        BukkitTask r = new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("Removed.");
                HashLoc.getBlock().setType(Material.AIR);
                HashLoc = null;
            }
        }.runTaskLater(p, cdRemove);
    }
    protected void onBlockFind(){
        if(!finded){
            finded = true;
            for(Player p:Bukkit.getOnlinePlayers()){
                p.sendMessage(main.messages.getString("msg_find", "LootBox was been founded!"));
            }
        }
    }

    public Location getRandomLoc(){

        World w = getRandomWorld();
        int Rx = getRandomPos();
        int Ry = getRandomPos();

        int y = 254;

        Location l = null;

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
    public int getRandomPos(){
        return main.r.nextInt(MaxX.get(LastIndexRandomWorld)-MinX.get(LastIndexRandomWorld))+MinX.get(LastIndexRandomWorld);
    }
    public World getRandomWorld(){
        if(w.size()==1)return w.get(0);
        LastIndexRandomWorld = main.r.nextInt(w.size());
        return w.get(LastIndexRandomWorld);
    }

    public Location getReqLoc(){
        boolean Nreq = true;
        while (Nreq){
            Location l = getRandomLoc();
            if(l.getBlock().getType() != Material.WATER)return l;
        }
        return null;
    }

    public void ReSetValues(){
        //Te4hnoConfig c = main.config;
        cdRemove = main.config.getInt("long", 5)*1200;
        sound = main.config.getString("sound");
        messageFormat = main.messages.getString("msg_spawnbox");
        try {
            List<String> rw = main.config.getStringList("worlds");
            for(String rv : rw){
                String[] vs = rv.split(";");
                World ww = Bukkit.getWorld(vs[0]);
                if(ww==null){
                    System.out.println(String.format("World name is wrong! -> [%s]", vs[0]));
                    System.out.println("--- loaded worlds ---");
                    for(World a:Bukkit.getWorlds()){
                        System.out.println(a.getName());
                    }
                    System.out.println("----------------------");
                    throw new NullPointerException();
                    //System.out.println(String.format("Using defaults", Bukkit.getWorlds().get(0)));
                }
                w.add(ww);
                MinX.add(Integer.parseInt(vs[1]));
                MaxX.add(Integer.parseInt(vs[2]));
            }
        } catch (Exception e){
            e.printStackTrace();
            for(World wr:Bukkit.getWorlds()) {
                w.add(wr);
                MinX.add(-1000);
                MaxX.add(1000);
            }
        }

    }
}
