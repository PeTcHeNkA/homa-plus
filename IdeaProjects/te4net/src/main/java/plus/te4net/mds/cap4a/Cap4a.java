package plus.te4net.mds.cap4a;

import plus.te4net.mds.crypt.CryptKey;
import plus.te4net.utl.FastRandom;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Cap4a extends CryptKey {
    private static final byte[] spp = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".getBytes(StandardCharsets.UTF_8);
    public final String htmlCode, cssCode, keyWord;

    public static void fillRandomId(byte[] data){
        for(int i=0;i<data.length;i++){
            data[i] = spp[FastRandom.INST.nextInt(spp.length)];
        }
    }


    public Cap4a(long time) {
        super(time, (short) 32766);
        fillRandomId(super.key);
        char[] code = CharLib.INST.genCode(8);
        StyleMap styles = new StyleMap();
        SArray elms = new SArray(code.length);

        for(int i=0;i<code.length;i++){
            Element em = new Element();
            em.chrk = code[i];
            em.tab = i;
            elms.add(em);
        }
        for (int i=0;i<4;i++){
            Element em = new Element();
            em.chrk = 0;
            em.tab = FastRandom.INST.nextInt(code.length);
            elms.add(em);
        }
        Collections.shuffle(elms);

        for (int i=0;i<6;i++){
            int len = elms.size();
            int index = FastRandom.INST.nextInt(len);
            Element em =  elms.get(index);
            if(em.chrk==0)continue;
            int index2 = FastRandom.INST.nextInt(len);
            Element e2 = elms.get(index2);
            if(index==index2 || e2.elms.size()>0)continue;
            elms.remove(index2);

            e2.tab = e2.tab-1-em.tab;
            em.elms.add(e2);
        }

        String selfCode = super.toString();

        //TODO FIX
        htmlCode = elms.toString(styles) +"<script id=\"cap4aid\">"+selfCode
                +"</script><link href=\"/auth/css?cap4a-id=" +selfCode+"&cap4a-mtd=css\" rel=\"stylesheet\">";
        cssCode = styles.toString();
        keyWord = CharLib.INST.code(new String(code));
    }


    public boolean check(String answer){
        if(use()){
            return keyWord.equals(answer);
        }
        return false;
    }
}


final class K{
    final int k;
    K(int k) {
        this.k = k;
    }
    @Override
    public boolean equals(Object o) {
        return k == ((K) o).k;
    }
    @Override
    public int hashCode() {
        return k;
    }
}


final class StyleMap extends HashMap<K, Character> {
    private static final char[] def = "mkplnjiubgytcxdreszqw".toCharArray();
    private static final char[] hiden = {'h','o','v','f',0,1};
    private static int usages = 0;
    char getOrCreate(int tab){
        Character c = get(new K(tab));
        if(c==null){
            if(usages>=def.length)usages=0;
            c = def[usages++];
            put(new K(tab), c);
            return c;
        }
        return c;
    }


    String getRStyle(int tab){
        if(FastRandom.INST.nextBoolean()){
            return getOrCreate(tab)+" a";
        } else {
            return "a "+getOrCreate(tab);
        }
    }


    HashSet<String> hStyles = new HashSet<>();
    String getHStyle(int tab){
        char c = hiden[FastRandom.INST.nextInt(hiden.length)];
        if(c==0){
            return getRStyle(90 + tab);
        }
        if(c==1){
            return getRStyle(-90 - tab);
        }
        switch (c) {
            case 'h':hStyles.add(".h{display:none}");break;
            case 'o':hStyles.add(".o{visibility:hidden}");break;
            case 'v':hStyles.add(".v{opacity:0}");break;
            case 'f':hStyles.add(".f{font-size:0}");break;
        }
        if(FastRandom.INST.nextBoolean()){
            return getOrCreate(tab)+" "+c;
        } else {
            return c+" "+getOrCreate(tab);
        }
    }


    @Override
    public String toString() {
        StringBuilder styles = new StringBuilder();
        for(String s:hStyles){
            styles.append(s);
        }
        super.forEach((key, ch) -> {
            styles.append('.')
                    .append(ch).append("{margin-left:")
                    .append(key.k * 12).append("px}");
        });
        return styles.toString();
    }
}


final class SArray extends ArrayList<Element> {
    public SArray(){}
    public SArray(int size){
        super(size);
    }

    public String toString(StyleMap styleMap) {
        if(size()==0)return "";
        StringBuilder data = new StringBuilder();
        for (Element o : this) {
            o.toString(styleMap, data);
        }
        return data.toString();
    }
}


class Element{
    int tab;
    char chrk;
    SArray elms = new SArray();

    public void toString(StyleMap styleMap, StringBuilder s) {
        if(chrk==0){
            s.append("<div class=\""+styleMap.getHStyle(tab)+"\">"+CharLib.INST.nextRChar()+"</div>");
        } else {
            s.append("<div class=\"" + styleMap.getRStyle(tab) + "\">" + chrk + elms.toString(styleMap) + "</div>");
        }
    }
}
