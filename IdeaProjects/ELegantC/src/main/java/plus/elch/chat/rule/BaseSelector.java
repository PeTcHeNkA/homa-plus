package plus.elch.chat.rule;

import org.bukkit.entity.Player;
import java.util.Iterator;


public abstract class BaseSelector implements Iterator<Player> {
    private final Iterator<? extends Player> parent;
    private Player next;

    public BaseSelector(Iterator<? extends Player> parent) {
        this.parent = parent;
        this.next = parent.next();
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    public boolean check(Player player) {
        return true;
    }


    @Override
    public Player next() {
        Player prev;
        if ((prev = next) == null) return null;
        Iterator<? extends Player> parent;
        while ((parent = this.parent).hasNext()) {
            Player newNext;
            if ((newNext = parent.next()) != null)
                if (!check(newNext)) continue;

            next = newNext;
            return prev;
        }
        return next = null;
    }
}
