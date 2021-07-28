package main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class AdvantureChatBuilder {

    public static String buildStr(String startstring){
        String newstring;

        newstring = startstring.replace('+', '\n').replace('&', '\u00A7').replace('$','&');

        return newstring;
    }
    public static TextComponent[] parseComponents(Player player, String[] args){

        //System.out.println("nen1");

        List<TextComponent> components = new ArrayList();

        int flag = 0;
        for (String newcomp : args) {

            //System.out.println("nen88");
            //newcomp = newcomp.replace('+', '\n').replace('&', '\u00A7').replace('$','&').replace('_',' ');

            if(newcomp.charAt(0) == '@'){

                TextComponent edit = components.get(flag-1);

                if(newcomp.charAt(1) == 'U') {//PlaceHolderSupporter.PlaceReplace(player, newcomp.substring(2))

                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            newcomp.substring(2)
                            )
                    );
                }

                if(newcomp.charAt(1) == 'C'){
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, newcomp.substring(2).replace('_',' '))
                    );
                }

                if(newcomp.charAt(1) == 'S'){
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, newcomp.substring(2).replace('_',' '))
                    );
                }

                if(newcomp.charAt(1) == 'H'){
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(PlaceHolderSupporter.PlaceReplace(player,
                                    newcomp.substring(2).replace('+', '\n').replace('&', '\u00A7').replace('$','&').replace('_',' ')
                            ))
                    ));
                }

            } else {
                //System.out.println("nen7");
                components.add(
                        new TextComponent(PlaceHolderSupporter.PlaceReplace(player,
                                newcomp.replace('+', '\n').replace('&', '\u00A7').replace('$','&').replace('_',' ')
                        ))
                );
                flag = flag+1;
            }
        }

        TextComponent[] fin = components.toArray(new TextComponent[components.size()]);

        return (fin);

    }
    public static TextComponent[] consoleParser(List<String> args){
        return consoleParser(args.toArray(new String[args.size()]));
    }


    public static TextComponent[] consoleParser(String[] args){

        List<TextComponent> components = new ArrayList();

        int flag = 0;
        for (String newcomp : args) {

            if(newcomp.charAt(0) == '@'){

                TextComponent edit = components.get(flag-1);

                if(newcomp.charAt(1) == 'U') {

                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    newcomp.substring(2)
                            )
                    );
                }

                if(newcomp.charAt(1) == 'C'){
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, newcomp.substring(2).replace('_',' '))
                    );
                }

                if(newcomp.charAt(1) == 'S'){
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, newcomp.substring(2).replace('_',' '))
                    );
                }

                if(newcomp.charAt(1) == 'H'){
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(newcomp.substring(2).replace('+', '\n').replace('&', '\u00A7').replace('$','&').replace('_',' ')
                            )
                    ));
                }

            } else {
                //System.out.println("nen7");
                components.add(
                        new TextComponent(
                                newcomp.replace('+', '\n').replace('&', '\u00A7').replace('$','&').replace('_',' ')
                        )
                );
                flag = flag+1;
            }
        }

        TextComponent[] fin = components.toArray(new TextComponent[components.size()]);

        return (fin);

    }
}
