package plus.valcirias.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.ValkiriaManager;
import plus.util.ConfigHelper;
import plus.util.YandeRePlugin;
import plus.valcirias.Valkiria;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class UserFloader{

    public final ValkiriaDataFloader bd;
    public UserFloader(YandeRePlugin plugin){
        bd = new ValkiriaDataFloader(plugin, this);
    }
    private final HashMap<String, Valkiria> floader = new HashMap<>();
    private final List<String> premiumClasses = new ArrayList<>();


    public void addPremiumClass(String className){
        premiumClasses.add(className);
    }


    public void ClearHash(){
        for(Valkiria valkiria: floader.values()) {
            if (valkiria.toPlayer().isOnline()) {
                valkiria.toPlayer().kickPlayer("");
            }
        }
        saveallusers();

        floader.clear();
    }


    public void ChangeValkiriaType(Player player, String newClass){
        Valkiria valkiria = floader.get(player.getName());

        if(valkiria.CombatMode()){
            player.spigot().sendMessage(ConfigHelper.messageDisableCombat);
            return;
        }
        if(premiumClasses.contains(newClass)){
            if(!valkiria.isPremium()){
                player.spigot().sendMessage(ConfigHelper.messageOnlyPremium);
                return;
            }
        }
        Valkiria newValkiria = bd.changeClassOfValkiria(valkiria, newClass);
        floader.put(player.getName(), newValkiria);

        if(valkiria.getWeapon()!=null){
            ValkiriaManager.addOrDropItem(player, valkiria.getWeapon());
        }
        newValkiria.updateAll();
    }


    public void loadValkiria(Player player){
        player.setHealthScale(20);
        Valkiria v;
        if(!floader.containsKey(player.getName())) {
            v = bd.getValkiria(player);
            floader.put(player.getName(), v);
        } else {
            v = floader.get(player.getName());
            v.setPlayerName(player);
        }
        System.out.println(String.format("Valkiria [%s], lvl: %s was uploaded!", player.getName(), v.getLvl()));
        v.getQuestSession().setPlayer(player);
        try {
            Thread.sleep(7500);
            v.onPostLoad();
        } catch (InterruptedException ignored) {}
    }


    public Valkiria getValkiria(Player player){
        return floader.get(player.getName());
    }


    public Valkiria getValkiria(String name){
        return floader.get(name);
    }


    public boolean hasValkiria(Player player){
        return floader.containsKey(player.getName());
    }


    public boolean hasValkiria(String name){
        return floader.containsKey(name);
    }


    public void saveallusers(){
        for(Valkiria valkiria: floader.values()) {
            bd.putValkiria(valkiria);
            if(!valkiria.toPlayer().isOnline()){
                floader.remove(valkiria.toPlayerName());
            }
        }
    }


    public void saveValkiria(Valkiria v){
        bd.putValkiria(v);
    }


    public void saveValkiria(String name){
        saveValkiria(floader.get(name));
    }


    public void clearValkiriaCacheOf(String player){
        Player pl = Bukkit.getPlayer(player);
        if (pl != null) clearValkiriaCacheOf(pl);
        else floader.remove(player);
    }


    public void clearValkiriaCacheOf(Player pl){
        if(pl.isOnline()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    pl.kickPlayer("");
                    floader.remove(pl.getName());
                }
            }.runTaskLater(Main.instance, 0);
        } else floader.remove(pl.getName());
    }


    public Set<String> getValciriaNames(){
        return floader.keySet();
    }
}