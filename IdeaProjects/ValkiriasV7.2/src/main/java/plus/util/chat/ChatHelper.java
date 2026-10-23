package plus.util.chat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import plus.util.ConfigHelper;
import plus.util.NextLvlTable;
import plus.valcirias.Valkiria;

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
                if (newcomp.charAt(1) == 'P') {
                    TextComponent edits = new TextComponent(String.format(ConfigHelper.nickFormat, edit.getText(), "&f").replace('&', '\u00A7'));

                    String[] vls = newcomp.substring(2).split(";");

                    //int exp = NextLvlTable.getNextLvlExp(Integer.parseInt(vls[4]));

                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(String.format(ConfigHelper.loreFormat, vls[0],vls[1], "", edit.getText(), vls[2], "???",
                                    NextLvlTable.getNextLvlExp(Integer.parseInt(vls[2]))
                            ))
                    ));

                    components.set(flag -1, edits);
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


    public static void SendMessage(Valkiria valkiria, TextComponent prefix, String message, Player[] targets){

        if(valkiria.isMuted())return;

        TextComponent profile = getValkiriaProfile(valkiria);

        for(Player target:targets){
            target.spigot().sendMessage(prefix, profile, new TextComponent(message));
        }
    }



    public static TextComponent getValkiriaProfile(Valkiria valkiria){

        TextComponent profile = new TextComponent(String.format(ConfigHelper.nickFormat, valkiria.getColor(), valkiria.toPlayerName()));
        profile.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(String.format(
                                ConfigHelper.loreFormat,
                                valkiria.getAvatar().getSymbol(),
                                valkiria.getClassName(),
                                valkiria.getColor(),
                                valkiria.toPlayerName(),
                                valkiria.getLvl(),
                                valkiria.getExperience(),
                                NextLvlTable.getNextLvlExp(valkiria.getLvl())
                        )
                        )
                )
        );
        profile.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg "+valkiria.toPlayerName()));
        return profile;
    }


}
