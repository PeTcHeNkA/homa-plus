package plus.util.chat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import java.util.ArrayList;
import java.util.List;

public class ChatHelper {
    protected static TextComponent[] Parse(String[] args) {

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

        return components.toArray(new TextComponent[components.size()]);
    }
    public static TextComponent[] Parse(List<String> args) {
        return Parse(args.toArray(new String[args.size()]));
    }

}