package plus.fserver.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.ArrayList;

import static plus.tson.ext.TsonBoolUtils.calc;


public class TextComponentAuto extends TextComponentString {
    private static final Int2ObjectOpenHashMap<TextFormatting> byKey = new Int2ObjectOpenHashMap<>();
    static {
        for (TextFormatting fm:TextFormatting.values()){
            byKey.put(fm.toString().charAt(1), fm);
        }
        byKey.trim();
    }

    private final String nonColored;

    public TextComponentAuto(String str){
        this(str, new Style(), true);
    }


    public TextComponentAuto(String str, Style style){
        this(str, style, true);
    }


    private TextComponentAuto(String str, Style style, boolean init){
        super("");
        if(init){
            nonColored = str.replace("§","");
        } else {
            nonColored = str;
            return;
        }
        String[] items = str.split("§");
        if(str.charAt(0)!='§'){
            items[0] = "f"+items[0];
        }

        for (int i=0;i<items.length;i++){
            String item = items[i];
            if(item.isEmpty()) continue;

            TextFormatting fm = byKey.get(item.charAt(0));
            if(item.length()==1){
                if(fm != null){
                    if(fm == TextFormatting.RESET){
                        style = new Style();
                        continue;
                    }
                    if(fm.isColor()) style.setColor(fm);
                    else {
                        switch (fm){
                            case OBFUSCATED:
                                style.setObfuscated(true);break;
                            case UNDERLINE:
                                style.setUnderlined(true);break;
                            case BOLD:
                                style.setBold(true);break;
                            case STRIKETHROUGH:
                                style.setStrikethrough(true);break;
                            case ITALIC:
                                style.setItalic(true);break;
                        }
                    }
                }
            } else {
                if(fm != null){
                    style.setColor(fm);
                    item = item.substring(1);
                }
                siblings.add(new TextComponentString(item).setStyle(style));
                Style prev = style;
                style = prev.createDeepCopy();
            }
        }
    }


    @Override
    public String getUnformattedComponentText() {
        return "";
    }


    @Override
    public TextComponentAuto createCopy() {
        TextComponentAuto copy = new TextComponentAuto(nonColored, null, false);
        for (ITextComponent comp:siblings){
            copy.siblings.add(comp.createCopy());
        }
        return copy;
    }


    //tson builder
    public static ITextComponent compFromTson(TsonObj obj){
        if(obj.isString())return new TextComponentAuto(obj.getStr());
        if(obj.isMap()) {
            TsonMap map = obj.getMap();

            Style style = styleFromTson(map.get("style"));
            String text = map.getStr("text");

            String type = map.getOrDefaultStr("type", "text");
            switch (type){
                case "translation":{
                    TextComponentTranslation comp;
                    TsonObj args = map.get("args");
                    if(args == null) {
                        comp = new TextComponentTranslation(text);
                    } else {
                        if(args.isString()){
                            comp = new TextComponentTranslation(text, args.getStr());
                        } else if(args.isList()){
                            ArrayList<ITextComponent> comps = new ArrayList<>(args.getList().size());
                            for (TsonObj obj1:args.getList()){
                                comps.add(compFromTson(obj1));
                            }
                            comp = new TextComponentTranslation(text, comps.toArray());
                        } else {
                            throw new RuntimeException("Illegal type: " + args);
                        }
                    }
                    comp.setStyle(style);
                    return comp;
                }
                case "key":
                case "keybind":{
                    TextComponentKeybind comp = new TextComponentKeybind(text);
                    comp.setStyle(style);
                    return comp;
                }
                case "score":
                case "scoreboard":{
                    TextComponentScore comp = new TextComponentScore(text, map.getStr("objective"));
                    comp.setStyle(style);
                    return comp;
                }
            }
            return new TextComponentAuto(text, style);
        }
        if(obj.isList()){
            ITextComponent comp = null;
            for(int i = 0; i < obj.getList().size(); i++){
                ITextComponent newComp = compFromTson(obj.getList().get(i));
                if(comp == null)comp = newComp;
                else comp.appendSibling(newComp);
            }
            return comp;
        }
        throw new RuntimeException("Illegal type: " + obj);
    }


    private static Style styleFromTson(TsonObj obj){
        Style style = new Style();
        if(obj != null){
            if(obj.isMap()){
                applyStyle(style, obj.getMap());
            }
        }
        return style;
    }


    private static void applyStyle(Style style, TsonMap map){
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
            return new HoverEvent(hoverActionFromStr(obj.getMap().getStrSafe("action")), compFromTson(obj));
        } else {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, compFromTson(obj));
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