package plus.elch.chat.rule;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public class SelectPerm implements IUserSelector{
    private final String permission;
    private final IUserSelector parent;

    public SelectPerm(IUserSelector parent, String permission) {
        this.parent = parent;
        this.permission = permission.toLowerCase();
    }


    @NotNull
    @Override
    public Iterator<? extends Player> selectBy(Player player) {
        return new PermSelector(parent.selectBy(player), permission);
    }


    protected static class PermSelector extends BaseSelector{
        private final String checkPermission;

        public PermSelector(Iterator<? extends Player> parent, String checkPermission) {
            super(parent);
            this.checkPermission = checkPermission;
        }


        @Override
        public boolean check(Player player) {
            if(player.hasPermission(checkPermission))
                return super.check(player);
            return false;
        }
    }
}