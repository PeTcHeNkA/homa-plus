package plus.parsers;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ParserSendCommand implements  ParserPatch{
    @Override
    public TextComponent parse(Player player, TextComponent edit, String value) {
        if(player != null) {

            edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value.replace('_', ' ')));

        } else {

            edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));

        }
        return edit;
    }
}
