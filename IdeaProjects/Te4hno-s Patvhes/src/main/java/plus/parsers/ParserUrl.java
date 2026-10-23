package plus.parsers;

import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;

public class ParserUrl implements ParserPatch{
    @Override
    public TextComponent parse(Player player, TextComponent edit, String value) {

        edit.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, value));
        return edit;
    }
}
