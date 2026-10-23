package plus.quest.script.scene;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.QuestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ScriptChat implements UniversalScript{

    protected final TextComponent[] message;

    public ScriptChat(Sector s, String path){
        if(s instanceof QuestConfig c) message = parse(
                    c.getOrSetSingleStringList(path, ".message", Arrays.asList("example AChat message")));
        else message = parse(
                s.getOrSetStringList(path+".message", Arrays.asList("example AChat message")));
    }


    @Override
    public void run(Player player) {
        player.spigot().sendMessage(message);
    }


    protected static TextComponent[] parse(List<String> args) {

        List<TextComponent> components = new ArrayList();

        int flag = 0;
        for (String newcomps : args) {
            String newcomp = newcomps.replace('&', '\u00A7').replace('+','\n');

            if (newcomp.charAt(0) == '@') {
                TextComponent edit = components.get(flag - 1);
                if (newcomp.charAt(1) == 'U') {

                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    newcomp.substring(2)
                            )
                    );
                }

                if (newcomp.charAt(1) == 'C') {
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, newcomp.substring(2))
                    );
                }

                if (newcomp.charAt(1) == 'S') {
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, newcomp.substring(2))
                    );
                }

                if (newcomp.charAt(1) == 'H') {
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(newcomp.substring(2)
                            )
                    ));
                }
            } else {
                components.add(
                        new TextComponent(
                                newcomp
                        )
                );
                flag = flag + 1;
            }
        }
        return components.toArray(new TextComponent[0]);
    }
}