package plus.fmsg.msg;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import plus.fserver.api.TextComponentAuto;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import static plus.tson.ext.TsonBoolUtils.*;


public class TsonCompUtls {
    public static <T> T tryCast(TsonObj obj){
        if(!obj.isCustom())throw new RuntimeException("Only custom objects is supported");
        return (T) (obj.getField());
    }


    public static ITextComponent msgFromTson(TsonObj obj){
        if(obj.isString())return new TextComponentAuto(obj.getStr());
        if(obj.isMap()) {
            Style style = styleFromTson(obj);
            String text = obj.getMap().getStr("text");
            return new TextComponentAuto(text, style);
        }
        if(obj.isList()){
            TextComponentString comp = new TextComponentString("");
            for(int i = 0; i < obj.getList().size(); i++){
                comp.appendSibling(msgFromTson(obj.getList().get(i)));
            }
            return comp;
        }
        throw new RuntimeException("Illegal type: " + obj);
    }


    public static Style styleFromTson(TsonObj obj){
        Style style = new Style();
        if(obj != null){
            if(obj.isMap()){
                applyStyle(style, obj.getMap());
            }
        }
        return style;
    }


    public static void applyStyle(Style style, TsonMap map){
        style.setBold(          calc(map, "bold"         , style.getBold())          );
        style.setStrikethrough( calc(map, "strikethrough", style.getStrikethrough()) );
        style.setUnderlined(    calc(map, "underlined"   , style.getUnderlined())    );
        style.setItalic(        calc(map, "italic"       , style.getItalic())        );

        map.ifContainsStr("color" , s -> style.setColor(fromStr(s)));
        map.ifContainsStr("insert", style::setInsertion);

        map.ifContains("hover", c -> style.setHoverEvent(buildHover(c)));
        map.ifContains("click", c -> style.setClickEvent(buildClick(c)));
    }


    private static HoverEvent.Action hoverActionFromStr(String str){
        if(str == null) return HoverEvent.Action.SHOW_TEXT;
        try {
            return HoverEvent.Action.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e){
            return HoverEvent.Action.SHOW_TEXT;
        }
    }


    private static ClickEvent.Action clickActionFromStr(String str){
        if(str == null) return ClickEvent.Action.SUGGEST_COMMAND;
        try {
            return ClickEvent.Action.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e){
            return ClickEvent.Action.SUGGEST_COMMAND;
        }
    }


    private static HoverEvent buildHover(TsonObj obj){
        if(obj.isMap()){
            return new HoverEvent(hoverActionFromStr(obj.getMap().getStrSafe("action")), msgFromTson(obj));
        } else {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, msgFromTson(obj));
        }
    }


    private static ClickEvent buildClick(TsonObj obj){
        if(obj.isString()){
            return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, obj.getStr());
        } else if(obj.isMap()){
            return new ClickEvent(clickActionFromStr(obj.getMap().getStrSafe("action")), obj.getMap().getStrSafe("value"));
        }
        throw new RuntimeException("Illegal type: " + obj.type());
    }


    private static TextFormatting fromStr(String str){
        return TextFormatting.valueOf(str.toUpperCase());
    }
}

