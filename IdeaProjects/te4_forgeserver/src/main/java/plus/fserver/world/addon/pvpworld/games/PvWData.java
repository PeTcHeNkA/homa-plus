package plus.fserver.world.addon.pvpworld.games;

import plus.fserver.world.addon.pvpworld.stats.PlayerData;
import plus.fserver.world.addon.pvpworld.tasks.GiveItems;

public class PvWData extends CommonWorldData {
    public GiveItems usedKit;

    @Override
    public PlayerData create() {
        return new PvPlData();
    }
}
