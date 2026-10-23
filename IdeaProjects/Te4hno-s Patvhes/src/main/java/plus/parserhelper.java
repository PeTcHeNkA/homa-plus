package plus;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;
import plus.parsers.ParserPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class parserhelper {

    public static String buildStr(String startstring){
        String newstring;

        newstring = startstring.replace('+', '\n').replace('&', '\u00A7').replace('$','&');

        return newstring;
    }
    private static HashMap<Character, ParserPatch> parsers = new HashMap<>();

    public static void registreParserPatch(ParserPatch patch, Character tag){
        parsers.put(tag, patch);
    }

    public static TextComponent[] parseComponents(Player TargetPapiParser, String[] args){

        List<TextComponent> components = new ArrayList();

        int flag = 0;
        for (String newcomp : args) {

            if(newcomp.charAt(0) == '@' && parsers.containsKey(newcomp.charAt(1))){

                TextComponent edit = components.get(flag-1);

                edit = parsers.get(newcomp.charAt(1)).parse(TargetPapiParser, edit, newcomp.substring(2));

            } else {

                if(TargetPapiParser == null){
                    components.add(
                      new TextComponent(newcomp.replace('+', '\n'))
                    );
                } else {
                    components.add(

                            new TextComponent(PapiHelper.Replace(TargetPapiParser, newcomp.replace('+', '\n').replace('_', ' ')))
                    );
                }

                flag = flag+1;
            }
        }

        TextComponent[] fin = components.toArray(new TextComponent[components.size()]);

        return fin;

    }
}
