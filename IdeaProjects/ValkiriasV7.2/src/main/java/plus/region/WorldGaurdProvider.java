package plus.region;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldedit.util.Location;
import org.bukkit.entity.Player;


public class WorldGaurdProvider implements TerrianChecker{

    private final RegionQuery query;

    public WorldGaurdProvider(){
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        query = container.createQuery();
    }

    @Override
    public boolean inPvpTerritory(Player player){

        LocalPlayer pl = WorldGuardPlugin.inst().wrapPlayer(player);
        Location loc = pl.getLocation();

        return query.testState(loc, pl, Flags.PVP);
    }
}