package plus.region;

import org.bukkit.entity.Player;

public class DisabledPvp implements TerrianChecker{
    @Override
    public boolean inPvpTerritory(Player player) {
        return true;
    }
}
