package ot;

import java.util.ArrayList;
import java.util.List;

public class TextComp {
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

    public TextComp(String value){
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

                while (t.charAt(0)=='&'){
                    String color = t2.ColorOf(t.charAt(1));
                    if(color != null) colors.add(String.format("\"color\":\"%s\"", color));
                    t = t.substring(2);
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
