package plus.elch.chat.rule;

import org.bukkit.entity.Player;
import java.util.Iterator;


public class SelectAll implements IUserSelector{
    private static SelectAll INSTANCE;

    public static SelectAll getInstance(){
        if(INSTANCE == null)
            INSTANCE = new SelectAll();
        return INSTANCE;
    }


    @Override
    public Iterator<? extends Player> selectBy(Player player) {
        return player.getServer().getOnlinePlayers().iterator();
    }
}