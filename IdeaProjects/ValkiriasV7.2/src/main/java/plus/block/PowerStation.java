package plus.block;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.player.PlayerInteractEvent;
import plus.Main;
import plus.util.ConfigHelper;
import plus.valcirias.Valkiria;


public class PowerStation implements InteractableBlock {

    private int maxPower = 0;
    private int power = 0;
    private int speedRegen = 0;
    private int maxTake = 0;


    public static PowerStation build(int maxPower, int speedRegen){
        if(Main.floader.bd.dungeonMode)return new DungeonPowerStation(maxPower, speedRegen);
        else return new PowerStation(maxPower, speedRegen);
    }


    public PowerStation(int maxPower, int speedRegen){
        this.maxPower = maxPower;
        this.speedRegen = speedRegen;
        power = maxPower;
        maxTake = maxPower/2;
    }


    private int Take(Valkiria valkiria){
        if(power==0)return 0;

        int charge = maxTake;
        if(charge>power)charge=power;

        double newShield = valkiria.getShield()+charge;

        if(newShield>valkiria.getMaxShield()){
            charge = charge -(int)(newShield-valkiria.getMaxShield());
            valkiria.setShield(valkiria.getMaxShield());
        } else {
            valkiria.setShield(newShield);
        }
        valkiria.sendUpdateHP();
        power = power-charge;

        return charge;
    }


    @Override
    public void regen(){
        int newPower = power+speedRegen;
        if(newPower>maxPower){
            power = maxPower;
        }else {
            power = newPower;
        }
    }


    protected void onValciriaClick(PlayerInteractEvent e, Valkiria v){
        int amount = Take(v);
        if (amount > 0) {
            summonParticles(e.getClickedBlock().getLocation(), amount);
            e.getPlayer().sendMessage(String.format(ConfigHelper.regenShield_Format, amount, power, maxPower));
        } else if (power == 0) {
            e.getPlayer().sendMessage(String.format(ConfigHelper.emptyStation_Format, maxPower));
        } else {
            e.getPlayer().sendMessage(String.format(ConfigHelper.fullShield_Format, power, maxPower));
        }
    }


    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        onValciriaClick(e, Main.floader.getValkiria(e.getPlayer()));
    }


    protected static void summonParticles(Location loc, int amount){
        loc.setY(loc.getY() + 0.95);
        loc.setX(loc.getX() + 0.5);
        loc.setZ(loc.getZ() + 0.5);
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, amount);
    }
}