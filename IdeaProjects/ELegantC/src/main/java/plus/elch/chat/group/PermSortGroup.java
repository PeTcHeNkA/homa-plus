package plus.elch.chat.group;

import org.bukkit.entity.Player;
import plus.elch.chat.rule.IUserSelector;


public class PermSortGroup extends ChatGroup{
    private final ChatPage[] pages;
    private final String[] perms;

    public PermSortGroup(IUserSelector selector, String name, ChatPage[] pages, String[] perms) {
        super(selector, name);
        this.pages = pages;
        this.perms = perms;
    }


    @Override
    public ChatPage getPage(Player player, String message) {
        String[] perms = this.perms;
        for (int i = 0; i < perms.length; i++) {
            if (player.hasPermission(perms[i]))
                return pages[i];
        }
        return null;
    }
}