package plus.elch.chat.rule;

import org.bukkit.entity.Player;
import java.util.Iterator;


public class SelectWorld implements IUserSelector{
    private static SelectAll INSTANCE;

    public static SelectAll getInstance(){
        if(INSTANCE == null)
            INSTANCE = new SelectAll();
        return INSTANCE;
    }


    @Override
    public Iterator<Player> selectBy(Player player) {
        return player.getWorld().getPlayers().iterator();
    }
}