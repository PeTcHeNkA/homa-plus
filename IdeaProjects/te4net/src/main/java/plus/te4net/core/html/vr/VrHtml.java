package plus.te4net.core.html.vr;

import plus.asyutl.buf.FastBuffer;
import plus.te4net.utl.CharNum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static plus.te4net.core.html.vr.VrCss.isEmpty;


public class VrHtml extends HtmlNode{
    private static final HashSet<String> notClose = new HashSet<>(Arrays.asList(
        "area","base","br","col","embed","hr","img","input","link","meta","param","source","track","wbr"
    ));
    private static final HashSet<String> strNode = new HashSet<>(Arrays.asList(
            "style", "css", "js", "script"
    ));

    private static boolean needClose(String tagName){
        return !notClose.contains(tagName);
    }
    private static boolean isNodeStr(String tagName){
        return strNode.contains(tagName);
    }


    public static class Attribute implements ICitem{
        public String key;
        private char bound;
        private ICitem value;
        @Override
        public void toString(StringBuilder b) {
            if(value==null){
                b.append(key);
            } else {
                b.append(key).append('=').append(bound);
                value.toString(b);
                b.append(bound);
            }
        }
        public void setValue(ICitem value){
            this.value = value;
        }
        public void setValue(String value){
            this.value = new SField(value.replace(String.valueOf(bound), "\\"+bound));
        }
        public String dicValue(){
            StringBuilder b = new StringBuilder();
            value.toString(b);
            return b.toString().replace("\\"+bound, String.valueOf(bound));
        }
        public ICitem getValue() {
            return value;
        }
    }


    public static class Style extends ArrayList<String> implements ICitem{
       public Style() {}

        public Style(String src) {
            super(Arrays.asList(src.split(" ")));
        }

        @Override
        public void toString(StringBuilder b) {
            for (String s:this){
                b.append(s).append(' ');
            }
            b.setLength(b.length()-1);
        }
    }


    private char[] chars;
    private int cursor;

    public VrHtml(String code){
        this.chars = code.toCharArray();
        parseNodes();
        chars = null;
    }


    private char cur(){
        return chars[cursor];
    }


    private void skipToNext(){
        while (cursor<chars.length){
            if(!isEmpty(cur()))return;
            ++cursor;
        }
    }


    private void parseNodes(){
        FastBuffer<HtmlNode> buf = new FastBuffer<>();
        buf.put(this);
        while (cursor<chars.length) {
            parseNode(buf);
        }
    }


    private void parseNode(FastBuffer<HtmlNode> parent){
        skipToNext();
        StringBuilder builder = new StringBuilder();
        if(cur()=='<'){
            ++cursor;
            boolean isClose = cur()=='/';
            String tagName = null;

            while (cursor<chars.length){
                char cur = cur();
                ++cursor;
                if(isEmpty(cur)){
                    break;
                } else if (cur=='>') {
                    if(isClose){
                        parent.flush();
                        return;
                    } else {
                        tagName = builder.toString();
                        builder.setLength(0);
                        break;
                    }
                }
                builder.append(cur);
            }
            ArrayList<Attribute> attributes = new ArrayList<>();

            if(tagName==null) {
                tagName = builder.toString();
                builder.setLength(0);
                skipToNext();
                if(cur()!='>') {
                    while (cursor < chars.length) {
                        attributes.add(parseAttribute(builder));
                        skipToNext();
                        char cur = cur();
                        if (cur == '>') {
                            break;
                        }
                    }
                    ++cursor;
                }
            }
            HtmlObject obj;
            if (needClose(tagName)) {
                HtmlNode obj0 = new HtmlNode();
                obj = obj0;
                parent.get().items.add(obj);
                if(isNodeStr(tagName)){
                    char[] wait = ("</"+tagName).toCharArray();

                    while (cursor<chars.length && !check(wait)){
                        builder.append(cur());
                        ++cursor;
                    }
                    builder.setLength(builder.length());
                    String code = builder.toString();
                    obj0.items.add(new SField(code));

                    while (cursor<chars.length && cur() != '>'){
                        ++cursor;
                    }
                    ++cursor;
                } else {
                    parent.put(obj0);
                }
            } else {
                obj = new HtmlObject();
                parent.get().items.add(obj);
            }
            obj.attributes = attributes;
            obj.name = tagName;
            return;
        } else {
            while (cursor<chars.length){
                char cur = cur();
                if(cur=='<'){
                    parent.get().items.add(new SField(builder.toString().trim()));
                    return;
                }
                builder.append(cur);
                ++cursor;
            }
        }
        throw error();
    }


    private Attribute parseAttribute(StringBuilder builder){
        int mode = 0;
        Attribute atr = new Attribute();
        while (cursor<chars.length){
            char cur = cur();
            ++cursor;
            if(mode==0) {
                if(isEmpty(cur)){
                    continue;
                }
                if (cur == '=') {
                    atr.key = builder.toString();
                    builder.setLength(0);
                    mode = 1;
                    continue;
                } else if (cur=='>') {
                    --cursor;
                    atr.key = builder.toString();
                    builder.setLength(0);
                    return atr;
                }
            } else if (mode==1) {
                if(isEmpty(cur)){
                    continue;
                } else {
                    if(cur=='>'){
                        --cursor;
                        builder.setLength(0);
                        return atr;
                    } else if (cur == '\'' || cur=='"' || cur=='`') {
                        atr.bound = cur;
                        mode = 2;
                        continue;
                    } else {
                        throw error("Not valid char: "+cur);
                    }
                }
            } else {
                if(mode==2){
                    if(cur=='\\'){
                        mode = 3;
                    } else if (cur == atr.bound) {
                        String value = builder.toString();
                        if(atr.key.equals("class")){
                            atr.value = new Style(value);
                        } else {
                            atr.value = new SField(value);
                        }
                        builder.setLength(0);
                        return atr;
                    }
                } else {
                    mode = 2;
                }
            }
            builder.append(cur);
        }
        throw error();
    }


    private boolean check(char... chars0){
        int lim = Math.min(cursor+chars0.length, chars.length);
        int n = 0;
        for (int i=cursor;i<lim;i++){
            if(chars[i]!=chars0[n])return false;
            n++;
        }
        return true;
    }


    public VrHtml uptCss(){
        VrCss vrCss = new VrCss();
        //"_" + CharNum.Default.getCharNumOf(i)
        ArrayList<Style> linkStyles = new ArrayList<>();

        forItems((parent, index, cur) -> {
            ICitem attr = cur.getAttr("class");
            if(attr!=null) {
                Style style;
                if (attr instanceof SField) {
                    style = new Style(((SField) attr).value);
                    cur.setAttribute("class", style);
                } else {
                    style = (Style) attr;
                }
                linkStyles.add(style);
            }
        });

        AtomicReference<HtmlNode> first = new AtomicReference<>();

        forItems("style", (parent, i, cur) -> {
            HtmlNode node = (HtmlNode) cur;
            if(first.get()==null){
                first.set(node);
            } else {
                parent.remove(i);
            }
            vrCss.join(((SField)node.get(0)).value);
        });

        first.get().items.set(0, vrCss);

        vrCss.unCollide();

        HashMap<String,String> names = new HashMap<>();

        int num = 0;

        HashSet<String> styles = vrCss.getAllStyles();

        for(Style style:linkStyles){
            for (String orig:style){
                if(!styles.contains("."+orig))continue;
                String twName = names.get(orig);
                if(twName==null){
                    twName = "_" + CharNum.Default.getCharNumOf(num);
                    names.put(orig, twName);
                    ++num;
                }
            }
        }

        for(Style style:linkStyles) {
            int index = 0;
            for (String orig : new ArrayList<>(style)) {
                String tweak = names.get(orig);
                if(tweak==null)continue;
                vrCss.renameStyle(".", orig, tweak);
                style.set(index, tweak);
                ++index;
            }
        }


        return this;
    }


    @Override
    public void toString(StringBuilder b) {
        for (ICitem obj : items) {
            obj.toString(b);
        }
    }


    private RuntimeException error(Object... o){
        return new RuntimeException("Syntax error: "+Arrays.toString(o));
    }
}