package plus.inv.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;


public class ItemManager {
    public static final ItemManager INST = new ItemManager();

    public String getSpLore(ItemStack item){
        String rawLore = item.getOrCreateSubCompound("tag").getString("SLORE");
        return parse(rawLore);
    }


    public void insertSpLore(ItemStack item, String s){
        item.getTagCompound().setString("SLORE", s);
    }


    public String parse(String raw){
        ArrayList<String> list = new ArrayList<>();
        raw = QWReplacer.INST.replaceAll(raw);
        if(!raw.contains("},\\{")){
            if(raw.startsWith("{") && raw.endsWith("}")){
                return raw.substring(1, raw.length() - 2);
            }
            return raw;
        }
        StringBuilder b = new StringBuilder();

        for(String s: raw.substring(1, raw.length() - 2).split("},\\{")){
            b.append(parseAnnotation(s));
        }
        return b.toString();
    }


    private String parseAnnotation(String s){
        if(!s.startsWith("@"))return s;
        String[] s2 = s.split("@", 1);
        s2[1] = s2[1].trim();
        String result;
        switch (s2[0].trim()){
            default: result = s2[1];break;
            case "@KEY": result = I18n.translateToLocal(s2[1]);break;
        }

        return result;
    }
}

class QWReplacer {
    public static final QWReplacer INST = new QWReplacer();
    public String replaceAll(String s){
        for(Replacer r:replacers){
            s = r.replace(s);
        }
        return s;
    }
    private final Replacer[] replacers = new Replacer[]{
            new Replacer("{nw}") {
                @Override
                public String genStr() {
                    return "\n";
                }
            },
            new Replacer("{name}") {
                @Override
                public String genStr() {
                    return Minecraft.getMinecraft().player.getDisplayNameString();
                }
            },
            new Replacer("{user_id}") {
                @Override
                public String genStr() {
                    return Minecraft.getMinecraft().player.getUniqueID().toString();
                }
            },
            new Replacer("{x_pos}") {
                @Override
                public String genStr() {
                    return String.format("%.2f",Minecraft.getMinecraft().player.posX);
                }
            },
            new Replacer("{y_pos}") {
                @Override
                public String genStr() {
                    return String.format("%.2f",Minecraft.getMinecraft().player.posY);
                }
            },
            new Replacer("{z_pos}") {
                @Override
                public String genStr() {
                    return String.format("%.2f",Minecraft.getMinecraft().player.posZ);
                }
            }
    };
}


abstract class Replacer{
    private final String from;
    public Replacer(String from) {
        this.from = from;
    }
    public abstract String genStr();
    public String replace(String string){
        return string.replace(from, genStr());
    }
}