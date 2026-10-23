package plus.quest.script.quest;

import org.bukkit.entity.Player;
import plus.Main;
import plus.quest.script.scene.UniversalScript;
import plus.valcirias.Valkiria;


public class ScriptHeal implements UniversalScript {

    @Override
    public void run(Player player) {
        Valkiria v = Main.floader.getValkiria(player);
        v.setShield(v.getMaxShield());
        player.setFoodLevel(20);
        v.sendUpdateHP();
    }
}