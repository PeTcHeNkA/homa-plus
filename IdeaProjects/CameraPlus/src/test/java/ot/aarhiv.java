package ot;

import java.util.ArrayList;
import java.util.List;

public class aarhiv {

    private final String format;
    private static ModeFormat mode = new DisabledPapi();

    public aarhiv(List<String> args){
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


        List<TextModule2> components = new ArrayList();

        int flag = 0;
        for (String newcomps : args) {
            String newcomp = newcomps.replace('+','\n');

            if (newcomp.charAt(0) == '@') {

                TextModule2 edit = components.get(flag - 1);
                if (newcomp.charAt(1) != 'H') {
                    edit.setClickEvent( newcomp.charAt(1),
                            newcomp.substring(2)
                    );
                } else {
                    edit.setHoverEvent(newcomp.substring(2));
                }


            } else {
//                if(newcomp.charAt(0) == '&'){
//                    String color = ColorOf(newcomp.charAt(1));
//                    String extra = ExtraOf(newcomps.charAt(3));
//                    if(color!= null) {
//                        if(extra == null) {
//                            ot.TextModule2 m = new ot.TextModule2(newcomp.substring(2));
//                            m.setColor(color);
//                            components.add(m);
//                        } else {
//                            ot.TextModule2 m = new ot.TextModule2(newcomp.substring(4));
//                            m.setColor(color);
//                            m.setExtra(extra);
//                            components.add(m);
//                        }
//                    } else {
//                        components.add(new ot.TextModule2(newcomp));
//                    }
//
//                } else
                components.add(new TextModule2(newcomp));
                flag = flag + 1;
            }
        }


        List<TextModule2> fixColor = new ArrayList<>();
        for(TextModule2 m:components){
            if(m.hasClickEvent() || m.hasHoverEvent()){
                String[] parts = m.getText().replace("&", "$&").split("\\$");
                String fClick = m.getFinalClickEvent();
                String fHover = m.getFinalHoverEvent();

                for(String p:parts){
                    TextModule2 nm;
                    if(p.equals(""))continue;
                    if(p.charAt(0)=='&') {
                        String color = ColorOf(p.charAt(1));

                        if(p.charAt(2)=='@') {
                            String extra = ExtraOf(p.charAt(3));
                            nm = new TextModule2(p.substring(4), null, null, fHover, fClick);
                            nm.setExtra(extra);
                        } else {
                            nm = new TextModule2(p.substring(2), null, null, fHover, fClick);
                        }
                        nm.setColor(color);

                    }else {
                        nm = new TextModule2(p, null, null, fHover, fClick);
                    }
                    fixColor.add(nm);
                }
            }

        }

        String s = "";
        for(TextModule2 m:fixColor){
            s += (","+m.toString());
        }
        return s = "[\"\""+s+"]";
        //return String.join(",", );
        //return components.toArray(new TextComponent[components.size()]);
    }

    public static String ColorOf(char s){
        //      ,"color":"%s"
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
    }//  #7EC45C

    public static String ExtraOf(char s){
        //      ,"%s":true
        return switch (s) {
            case 'k' -> "obfuscated"; //
            case 'm' -> "strikethrough";
            case 'o' -> "italic";
            case 'l' -> "bold";
            case 'n' -> "underlined";
            default -> null;
        };
    }

    //remove this
    public static String parseColorToJson(String value){
        String[] coloredParts1 = value.replace("&", "$&").split("\\$");
        ArrayList<String> coloredParts = new ArrayList<>();
        for(String ss:coloredParts1){
            if(!ss.equals(""))coloredParts.add(ss);
        }
        for(int i = 0;i<coloredParts.size();i++){
            String ii = coloredParts.get(i);
            if(ii.charAt(0)=='&') {
                String cl = t2.ColorOf(ii.charAt(1));
                if(ii.charAt(2)!='@') {
                    if (cl != null) {
                        ii = String.format("{\"text\":\"%s\",\"color\":\"%s\"}", ii.substring(2), cl);
                    } else {
                        ii = String.format("{\"text\":\"%s\"}", ii.substring(2));
                    }
                } else {
                    String extra = t2.ExtraOf(ii.charAt(3));

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

        return String.join(",", coloredParts);
    }


}

class TextModule2{

    private final List<String> additional = new ArrayList<>();

    private int hoverIndex = -1;
    private int clickIndex = -1;
    private int colorIndex = -1;
    private int extraIndex = -1;
    private String Text;

    private static String replace(char input){
        if(input == 'C')return "run_command";
        if(input == 'U')return "open_url";
        if(input == 'S')return "suggest_command";
        if(input == 'I')return "insertion";
        else return "copy_to_clipboard";
    }

    TextModule2(String value){
        //System.out.println("init!");
        //Text = value;
        additional.add(String.format("\"text\":\"%s\"", value));
    }

    TextModule2(String text,String finalColor, String finalExtra, String finalHover, String finalClick){
        //additional.add(finalText);
        additional.add(String.format("\"text\":\"%s\"", text));
        int index = 1;
        if(finalColor != null){
            additional.add(finalColor);
            colorIndex = index;
            index+=1;
        }
        if(finalExtra != null){
            additional.add(finalExtra);
            extraIndex = index;
            index+=1;
        }
        if(finalHover != null){
            additional.add(finalHover);
            hoverIndex = index;
            index+=1;
        }
        if(finalClick != null){
            additional.add(finalClick);
            clickIndex = index;
        }
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
                String cl = t2.ColorOf(ii.charAt(1));
                if(ii.charAt(2)!='@') {
                    if (cl != null) {
                        ii = String.format("{\"text\":\"%s\",\"color\":\"%s\"}", ii.substring(2), cl);
                    } else {
                        ii = String.format("{\"text\":\"%s\"}", ii.substring(2));
                    }
                } else {
                    String extra = t2.ExtraOf(ii.charAt(3));

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
    public void removeHoverEvent(){
        if(hoverIndex>-1) {
            additional.remove(hoverIndex);
            hoverIndex = -1;
        }
    }
    public boolean hasHoverEvent(){
        return hoverIndex>-1;
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
    public boolean hasClickEvent(){
        return clickIndex>-1;
    }

    public void setColor(String color){
        if(colorIndex>-1)additional.remove(colorIndex);
        additional.add(String.format("\"color\":\"%s\"", color));
        colorIndex = additional.size()-1;
    }
    public void removeColor(){
        if(colorIndex>-1) {
            additional.remove(colorIndex);
            clickIndex = -1;
        }
    }
    public boolean hasColor(){
        return colorIndex>-1;
    }

    public void setExtra(String color){
        if(extraIndex>-1)additional.remove(extraIndex);
        additional.add(String.format("\"%s\":true", color));
        extraIndex = additional.size()-1;
    }
    public void removeExtra(){
        if(extraIndex>-1) {
            additional.remove(extraIndex);
            clickIndex = -1;
        }
    }
    public boolean hasExtra(){
        return extraIndex>-1;
    }

    public String getFinalColor(){
        if(hasColor()){
            return additional.get(clickIndex);
        } else return null;
    }
    public String getFinalClickEvent(){
        if(hasClickEvent()){
            return additional.get(clickIndex);
        } else return null;
    }
    public String getFinalHoverEvent(){
        if(hasHoverEvent()){
            return additional.get(hoverIndex);
        } else return null;
    }
    public String getFinalText(){
        return additional.get(0);
    }
    public String getText(){
        return Text;
    }

}