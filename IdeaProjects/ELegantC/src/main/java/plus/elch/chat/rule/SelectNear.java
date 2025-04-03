package plus.elch.chat.rule;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Iterator;


public class SelectNear implements IUserSelector{
    private final int radius;

    public SelectNear(int radius) {
        this.radius = radius;
    }


    @Override
    public Iterator<? extends Player> selectBy(Player player) {
        return new NearSelector(radius, player);
    }


    protected static class NearSelector extends BaseSelector{
        private final int radiusSq;
        private final Location loc;

        public NearSelector(int radius, Player player) {
            super(player.getWorld().getPlayers().iterator());
            this.radiusSq = radius * radius;
            this.loc = player.getLocation();
        }


        @Override
        public boolean check(Player player) {
            return player.getLocation().distanceSquared(loc) < radiusSq;
        }
    }
}