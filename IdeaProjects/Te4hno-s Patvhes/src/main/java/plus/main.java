package plus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plus.parsers.ParserSendCommand;
import plus.parsers.ParserSuggestCommand;
import plus.parsers.ParserUrl;

public final class main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("advchat").setExecutor(new commandhelper());

        parserhelper.registreParserPatch(new ParserUrl(), 'U');
        parserhelper.registreParserPatch(new ParserSendCommand(), 'C');
        parserhelper.registreParserPatch(new ParserSuggestCommand(), 'S');

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
