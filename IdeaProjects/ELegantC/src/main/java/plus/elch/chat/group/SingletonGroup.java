package plus.elch.chat.group;

import org.bukkit.entity.Player;
import plus.elch.chat.rule.IUserSelector;


public class SingletonGroup extends ChatGroup{
    private final ChatPage page;

    public SingletonGroup(IUserSelector selector, String name, ChatPage page) {
        super(selector, name);
        this.page = page;
    }


    @Override
    public ChatPage getPage(Player player, String message) {
        return page;
    }
}
