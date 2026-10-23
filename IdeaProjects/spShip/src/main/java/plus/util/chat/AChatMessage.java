package plus.util.chat;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AChatMessage {

    private final String format;
    private static ModeFormat mode = new DisabledPapi();

    public AChatMessage(List<String> args){
        if(args == null || args.isEmpty())throw new NullPointerException();
        format = Parse(args);
    }

    public String toString(){
        return format;
    }

    public String toString(Player pl){
        if(pl == null){
            return format;
        } else return mode.parse(format, pl);
    }

    private String Parse(List<String> args) {

        List<TextModule> components = new ArrayList();

        int flag = 0;
        for (String newcomps : args) {
            String newcomp = newcomps.replace('&', '\u00A7').replace('+','\n');

            if (newcomp.charAt(0) == '@') {

                TextModule edit = components.get(flag - 1);

                if (newcomp.charAt(1) != 'H') {
                    edit.setClickEvent( newcomp.charAt(1),
                                    newcomp.substring(2)
                            );
                } else {
                    edit.setHoverEvent(newcomp.substring(2));
                }
            } else {
                components.add(new TextModule(newcomp));
                flag = flag + 1;
            }
        }

        String s = "";
        for(TextModule m:components){
            s += (","+m.toString());
        }
        return s = "[\"\""+s+"]";
        //return String.join(",", );
        //return components.toArray(new TextComponent[components.size()]);
    }
}
class TextModule{

    private final List<String> additional = new ArrayList<>();

    private int hoverIndex = -1;
    private int clickIndex = -1;

    private String replace(char input){
        if(input == 'C')return "run_command";
        if(input == 'U')return "open_url";
        if(input == 'S')return "insertion";
        else return "copy_to_clipboard";
    }

    TextModule(String value){
        additional.add(String.format("\"text\":\"%s\"", value));
    }

    public void setHoverEvent(String value){
        if(hoverIndex>-1)additional.remove(hoverIndex);
        additional.add(String.format("\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"%s\"}", value));
        hoverIndex = additional.size()-1;
    }
    public void removeHoverEvent(){
        if(hoverIndex>-1) {
            additional.remove(hoverIndex);
            hoverIndex = -1;
        }
    }

    public String toString(){
        if(additional.size()>1){
            return "{"+String.join(",", additional).replace("%%", "%")+"}";
        } else return "{"+additional.get(0).replace("%%", "%")+"}";
    }

    public void setClickEvent(char action, String value){
        if(clickIndex>-1)additional.remove(clickIndex);
        additional.add(String.format("\"clickEvent\":{\"action\":\"%s\",\"value\":\"%s\"}", replace(action), value));
        clickIndex = additional.size()-1;
    }
    public void removeClickEvent(){
        if(clickIndex>-1) {
            additional.remove(clickIndex);
            clickIndex = -1;
        }
    }
}
interface ModeFormat{
    String parse(String str, Player pl);
}
class EnabledPapi implements ModeFormat{
    @Override
    public String parse(String str, Player pl) {
        //logic replace with papi
        return str;
    }
}
class DisabledPapi implements ModeFormat{
    @Override
    public String parse(String str, Player pl) {
        return str;
    }
}