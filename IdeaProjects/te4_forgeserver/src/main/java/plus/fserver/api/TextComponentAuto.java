package plus.fserver.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.text.*;


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
}