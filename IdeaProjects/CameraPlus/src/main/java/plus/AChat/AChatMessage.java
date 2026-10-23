package plus.AChat;

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

    public String toString(Object pl){
        if(pl == null){
            return format;
        } else return mode.parse(format, pl);
    }

    private String Parse(List<String> args) {

        List<TextModule> components = new ArrayList();
        int flag = 0;
        for (String newcomps : args) {
            String newcomp = newcomps.replace('+','\n');
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
        return "[\"\""+s+"]";
    }

    public static String ColorOf(char s){
        return switch (s) {
            case '0' -> "black"; //
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            default -> null;
        };
    }
    public static String ExtraOf(char s){
        return switch (s) {
            case 'k' -> "obfuscated"; //
            case 'm' -> "strikethrough";
            case 'o' -> "italic";
            case 'l' -> "bold";
            case 'n' -> "underlined";
            default -> null;
        };
    }
}
class TextModule{
    private final List<String> additional = new ArrayList<>();

    private int hoverIndex = -1;
    private int clickIndex = -1;
    private int colorIndex = -1;
    private int extraIndex = -1;
    private final String Text;

    private static String replace(char input){
        if(input == 'C')return "run_command";
        if(input == 'U')return "open_url";
        if(input == 'S')return "suggest_command";
        if(input == 'I')return "insertion";
        else return "copy_to_clipboard";
    }

    public TextModule(String value){
        Text = value;
    }

    public void setHoverEvent(String rvalue){
        String[] coloredParts1 = rvalue.replace("&", "$&").split("\\$");
        ArrayList<String> coloredParts = new ArrayList<>();
        for(String ss:coloredParts1){
            if(!ss.equals(""))coloredParts.add(ss);
        }
        for(int i = 0;i<coloredParts.size();i++){
            String ii = coloredParts.get(i);
            if(ii.charAt(0)=='&') {
                String cl = AChatMessage.ColorOf(ii.charAt(1));
                if(ii.charAt(2)!='@') {
                    if (cl != null) {
                        ii = String.format("{\"text\":\"%s\",\"color\":\"%s\"}", ii.substring(2), cl);
                    } else {
                        ii = String.format("{\"text\":\"%s\"}", ii.substring(2));
                    }
                } else {
                    String extra = AChatMessage.ExtraOf(ii.charAt(3));

                    if (cl != null) {
                        ii = String.format("{\"text\":\"%s\",\"color\":\"%s\"", ii.substring(4), cl);
                    } else {
                        ii = String.format("{\"text\":\"%s\"", ii.substring(4));
                    }
                    if(extra == null){
                        ii = ii +"}";
                    } else {
                        ii = ii + String.format(",\"%s\":true}", extra);
                    }
                }
            } else {
                ii = String.format("{\"text\":\"%s\"}", ii);
            }
            coloredParts.set(i, ii);
        }

        if(hoverIndex>-1)additional.remove(hoverIndex);
        additional.add(
                String.format("\"hoverEvent\":{\"action\":\"show_text\",\"contents\":%s}",
                        "[\"\","+String.join(",", coloredParts)+"]"
                )
        );
        hoverIndex = additional.size()-1;
    }

    public String toString(){
        if(Text.contains("&")) {
            String[] v1 = Text.replace("&", "$&").split("\\$");
            List<String> result = new ArrayList<>();

            String subString = "";
            for (String s : v1) {
                if (s.equals("")) continue;
                if (s.charAt(0) == '&' && s.length() == 2) {
                    subString += s;
                } else {
                    result.add(subString + s);
                    subString = "";
                }
            }
            List<String> texts = new ArrayList<>();
            for(String t:result){
                List<String> fn = new ArrayList<>();
                List<String> colors = new ArrayList<>();

                if (t.charAt(0)=='&'){
                    String color = AChatMessage.ColorOf(t.charAt(1));
                    if(color != null){
                        colors.add(String.format("\"color\":\"%s\"", color));
                        t = t.substring(2);
                    }
                    if(t.charAt(0)=='&'){
                        String extra = AChatMessage.ExtraOf(t.charAt(1));
                        if(extra!=null){
                            colors.add(String.format("\"%s\":true", extra));
                            t = t.substring(2);
                        }
                    }
                }
                fn.add(String.format("\"text\":\"%s\"", t));
                fn.addAll(additional);
                fn.addAll(colors);

                texts.add("{"+String.join(",", fn).replace("%%", "%")+"}");
            }
            return String.join(",", texts);

        }
        return null;
    }

    public void setClickEvent(char action, String value){
        if(clickIndex>-1)additional.remove(clickIndex);
        additional.add(String.format("\"clickEvent\":{\"action\":\"%s\",\"value\":\"%s\"}", replace(action), value));
        clickIndex = additional.size()-1;
    }
}
interface ModeFormat{
    String parse(String str, Object pl);
}
class EnabledPapi implements ModeFormat {
    @Override
    public String parse(String str, Object pl) {
        //logic replace with papi
        return str;
    }
}
class DisabledPapi implements ModeFormat {
    @Override
    public String parse(String str, Object pl) {
        return str;
    }
}