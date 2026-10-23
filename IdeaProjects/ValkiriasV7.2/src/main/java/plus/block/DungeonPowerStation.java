package plus.block;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.player.PlayerInteractEvent;
import plus.Main;
import plus.util.ConfigHelper;
import plus.valcirias.DungeonValkiria;


public class DungeonPowerStation extends PowerStation{

    public DungeonPowerStation(int maxPower, int speedRegen) {
        super(maxPower, speedRegen);
    }

    @Override
    public void onPlayerClick(PlayerInteractEvent e) {
        DungeonValkiria v = (DungeonValkiria) Main.floader.getValkiria(e.getPlayer());
        if(v.getMxRegenShield()==-1){
            super.onValciriaClick(e, v);
        } else {
            double needRegen = v.getMaxShield() - v.getShield();
            if (needRegen < 1) {
                v.toPlayer().sendMessage(String.format(
                        ConfigHelper.fullShield_Format,
                        v.getRemainsRegen(),
                        v.getMxRegenShield())
                );
            } else {
                if (needRegen > v.getRemainsRegen()) {
                    if (v.getRemainsRegen() < 1) {
                        v.toPlayer().sendMessage(String.format(ConfigHelper.emptyStation_Format, v.getMxRegenShield()));
                    }
                    double amount = v.getShield() + v.getRemainsRegen();
                    v.setShield(amount);
                    v.setRemainsRegen(0);
                    v.toPlayer().sendMessage(String.format(
                            ConfigHelper.regenShield_Format,
                            v.getRemainsRegen(), 0,
                            v.getMxRegenShield())
                    );
                    summonParticles(e.getClickedBlock().getLocation(), (int) amount);
                } else {
                    v.setShield(v.getShield() + v.getMaxShield());
                    v.toPlayer().sendMessage(String.format(
                            ConfigHelper.regenShield_Format,
                            needRegen,
                            v.getRemainsRegen(),
                            v.getMxRegenShield())
                    );
                    summonParticles(e.getClickedBlock().getLocation(), (int) needRegen);
                    v.setRemainsRegen(v.getRemainsRegen() - needRegen);
                }
            }
        }
    }
}