package plus.parsers;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

public interface ParserPatch {
    public TextComponent parse(Player player, TextComponent edit, String value);
}
