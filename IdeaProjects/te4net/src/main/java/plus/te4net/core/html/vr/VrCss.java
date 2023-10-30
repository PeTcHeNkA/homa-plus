package plus.te4net.core.html.vr;

import java.util.*;


public class VrCss implements ICitem {
    static void last(StringBuilder builder, char c){
        builder.setCharAt(builder.length()-1, c);
    }


    private static class Group implements ICitem {
        private String header;
        private final ArrayList<Style> items = new ArrayList<>();
        @Override
        public void toString(StringBuilder b) {
            if(header!=null){
                b.append(header).append('{');
            }
            for (Style style:items){
                style.toString(b);
            }
            if(header!=null){
                b.append(header).append('}');
            }
        }
    }


    private static class Var implements ICitem {
        private String header;
        private ArrayList<ICitem> items;
        @Override
        public void toString(StringBuilder b) {
            if(items==null){
                b.append(header);
            } else {
                b.append(header).append('(');
                for (ICitem item : items) {
                    item.toString(b);
                    b.append(',');
                }
                last(b, ')');
            }
        }
        private void add(ICitem e){
            if(items==null){
                items = new ArrayList<>();
            }
            items.add(e);
        }
    }


    private static class Vp implements ICitem {
        private final String value;
        public Vp(String v){
            this.value = v;
        }
        @Override
        public void toString(StringBuilder b) {
            b.setLength(b.length()-1);
            b.append(value);
        }
    }


    private final ArrayList<Group> groups = new ArrayList<>();
    private char[] chars;
    private int cursor;

    public VrCss(String code) {
        this.chars = code.toCharArray();
        parse();
    }
    public VrCss(){}


    public VrCss join(String code){
        VrCss vcss = new VrCss(code);
        for (Group toAdd:vcss.groups){
            boolean add = true;
            for (Group main: groups){
                if(Objects.equals(main.header, toAdd.header)){
                    add = false;
                    break;
                }
            }
            if(add){
                groups.add(toAdd);
            }
        }

        for (Group toAdd: vcss.groups){
            for (Group main: groups){
                if(toAdd==main)continue;
                if(!Objects.equals(main.header, toAdd.header))break;

                for (Style addStyle: toAdd.items){
                    boolean add = true;
                    for (Style mainStyle: new ArrayList<>(main.items)){
                        if(mainStyle==addStyle)continue;
                        if(mainStyle.eqNames(addStyle)){
                            add = false;
                            for (StyleParam param:addStyle.items){
                                mainStyle.items.add(param);
                            }
                        }
                    }
                    if(add){
                        main.items.add(addStyle);
                    }
                }
            }
        }
        return this;
    }


    public static boolean isEmpty(char c){
        return c == ' ' || c == '\t' || c == '\n';
    }


    private void skipToNext(){
        while (cursor<chars.length){
            if(!isEmpty(cur()))return;
            ++cursor;
        }
    }


    private char cur(){
        return chars[cursor];
    }


    private String readGroupName(){
        if(
                check('@','m','e','d','i','a') ||
                check('@','c','o','n','t','a','i','n','e','r')
        ){
            StringBuilder builder = new StringBuilder();
            builder.append(cur());
            ++cursor;
            while (cursor<chars.length){
                char cur = cur();
                if(cur=='{')return builder.toString();
                if(isEmpty(cur)){
                    char prev = chars[cursor-1];
                    if(isEmpty(prev) || prev=='(' || prev==')' || prev==':'){
                        ++cursor;
                        continue;
                    } else {
                        char next = chars[cursor+1];
                        if(isEmpty(next) || next=='(' || next==')' || next=='{'){
                            ++cursor;
                            continue;
                        }
                    }
                }
                ++cursor;
                builder.append(cur);
            }
        }
        return null;
    }


    private void goTo(char c){
        while (cursor<chars.length){
            if(cur()==c)return;
            ++cursor;
        }
    }


    private String getErrorString(){
        int min = Math.max(0, cursor-50);
        int max = Math.min(cursor+50,chars.length-1);
        char[] bytes = new char[max-min];
        System.arraycopy(chars, min, bytes, 0, bytes.length);
        return new String(bytes);
    }


    private RuntimeException syntaxError(Object... o){
        return new RuntimeException("Syntax error: "
                + Arrays.toString(o)+"\n----------\n"+getErrorString()+"\n----------\n"
        );
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


    private boolean readStyle(Group group){
        skipToNext();
        if(cursor>=chars.length)return false;
        if(cur()=='@')return false;
        Style style = new Style();
        while (cursor<chars.length){
            style.names.add(readStyleName());
            if(cur()==','){
                ++cursor;
            } else if (cur()=='{') {
                ++cursor;
                break;
            } else if(cur()=='}')return false;
        }
        StringBuilder builder = new StringBuilder();
        while (cursor<chars.length){
            skipToNext();
            if(cur()=='}'){
                ++cursor;
                break;
            }
            StyleParam param = readSParam(builder);
            if(param != null && param.key != null && param.value != null){
                style.items.add(param);
            }
            builder.setLength(0);
        }
        group.items.add(style);
        return true;
    }


    private StyleParam readSParam(StringBuilder builder){
        StyleParam param = new StyleParam();
        while (cursor<chars.length){
            char cur = cur();
            ++cursor;
            if(isEmpty(cur)) continue;
            if(cur==':'){
                param.key = builder.toString();
                builder.setLength(0);
                break;
            }
            builder.append(cur);
        }
        skipToNext();

        Var var = new Var();

        while (cursor<chars.length){
            if(cur()==')' || cur()==';' || cur()=='}')break;
            fillEntry(var, builder);
        }

        param.value = var;

        return param;
    }


    private void fillEntry(Var e, StringBuilder b){
        b.setLength(0);
        boolean waitEnd0 = false;
        while (cursor<chars.length){
            char cur = cur();
            ++cursor;
            if(cur=='('){
                e.header = b.toString();
                waitEnd0 = true;
                break;
            }
            if(cur==')' || cur==';' || cur=='}'||cur==','){
                e.header = b.toString();
                return;
            }
            b.append(cur);
        }
        if(waitEnd0){
            while (cursor<chars.length){
                int start = cursor;
                skipToNext();
                if(cur()==')' || cur()==';' || cur()=='}')return;
                Var e2 = new Var();
                fillEntry(e2, b);
                if(e2.items==null){
                    e.add(new SField(e2.header));
                } else {
                    e.add(e2);
                }
                if(start==cursor)throw new RuntimeException("Syntax error");
            }
        }
    }




//    private ICitem readSVal(StringBuilder b){
//        boolean isFunc = false;
//
//        while (cursor<chars.length){
//            char cur = cur();
//            ++cursor;
//            if(isEmpty(cur))continue;
//            if(cur == ';' || cur == '}'){
//                break;
//            }
//            if(cur=='('){
//                isFunc = true;
//                break;
//            }
//            b.append(cur);
//        }
//        if(!isFunc){
//            return new SField(b.toString());
//        }
//        String header = b.toString();
//        b.setLength(0);
//        ICitem item = readVar(header, b);
//        while (cursor<chars.length){
//            char cur = cur();
//            if(cur == '}')break;
//            ++cursor;
//            if(isEmpty(cur))continue;
//            if(cur == ';'){
//                break;
//            }
//        }
//        return item;
//    }


//    private ICitem readVar(String header, StringBuilder b){
//        Var var = new Var();
//        var.header = header;
//        boolean flag = true;
//        while (cursor<chars.length){
//            char cur = cur();
//            ++cursor;
//            if(isEmpty(cur))continue;
//            if(cur=='(' || cur==','){
//                flag = false;
//                String header0 = b.toString();
//                b.setLength(0);
//                var.items.add(readVar(header0, b));
//                continue;
//            } else if (cur==')') {
//                if(flag){
//                    var.items.add(new SField(b.toString()));
//                } else {
//                    if(!b.isEmpty()) {
//                        String str = b.toString();
//                        if(!var.items.isEmpty()){
//                            var.items.add(new Vp(str));
//                        } else {
//                            var.items.add(new SField(str));
//                        }
//                    }
//                }
//                b.setLength(0);
//                return var;
//            }
//            b.append(cur);
//        }
//        throw syntaxError();
//    }



    private StyleName readStyleName(){
        skipToNext();
        StyleName sName = new StyleName();

        StringBuilder builder = new StringBuilder();
        if(cur()=='.'){
            sName.selector = ".";
            ++cursor;
        } else if (cur()=='#') {
            sName.selector = "#";
            ++cursor;
        } else {
            sName.selector = "";
        }
        int mode = 0;//0 = name, 1 = params, 2 = wait ender, 3 ender

        while (cursor<chars.length){
            char cur = cur();
            if(cur=='{' || cur ==','){
                if(mode==0){
                    sName.name = builder.toString();
                } else if (mode==3) {
                    sName.ender = builder.toString();
                } else {
                    throw syntaxError();
                }
                return sName;
            }
            if(isEmpty(cur)){
                ++cursor;
                continue;
            }
            if(mode==0){
                if(cur=='['){
                    sName.name = builder.toString();
                    builder.setLength(0);
                    ++cursor;
                    mode = 1;
                    continue;
                } else if (cur==':') {
                    sName.name = builder.toString();
                    builder.setLength(0);
                    ++cursor;
                    mode = 3;
                    continue;
                }
            } else if (mode==1) {
                if(cur==']'){
                    sName.params = builder.toString();
                    builder.setLength(0);
                    ++cursor;
                    mode = 2;
                    continue;
                }
            } else if(mode==2){
                if(cur==':'){
                    mode = 3;
                } else {
                    throw syntaxError(cur);
                }
            }
            builder.append(cur);
            ++cursor;
        }
        throw syntaxError();
    }


    private void parseGroup(){
        skipToNext();
        Group group = new Group();
        group.header = readGroupName();
        if(group.header != null){
            ++cursor;
        }
        while (cursor<chars.length && readStyle(group)){}
        if(group.header==null){
            goTo('}');
        }
        groups.add(group);
    }


    private VrCss parse(){
        while (cursor<chars.length){
            parseGroup();
        }
        remEmpty();
        chars = null;
        return this;
    }


    private void remEmpty(){
        unCollide();
        Iterator<Group> e = groups.iterator();
        while (e.hasNext()){
            Group group = e.next();
            Iterator<Style> st = group.items.iterator();
            while (st.hasNext()){
                Style style = st.next();
                if(style.items.isEmpty()){
                    st.remove();
                }
            }
            if(group.items.isEmpty()){
                e.remove();
            }
        }
    }


    public void unCollide(){
        HashMap<String,Style> indexes = new HashMap<>();
        for (Group group: groups) {
            indexes.clear();
            Iterator<Style> curItr = group.items.iterator();
            while (curItr.hasNext()) {
                Style style = curItr.next();
                String fn = style.fullName();
                Style main = indexes.get(fn);
                if(main==null){
                    indexes.put(fn, style);
                } else {
                    if(main == style)continue;
                    curItr.remove();
                    for (StyleParam addParam: style.items){
                        boolean add = true;
                        for (StyleParam srcParam: new ArrayList<>(main.items)){
                            if(addParam.key.equals(srcParam.key)){
                                add = false;
                                srcParam.value = addParam.value;
                            }
                        }
                        if(add){
                            main.items.add(addParam);
                        }
                    }
                }
            }
        }
    }


    public void renameStyle(String selector, String from, String to){
        for (Group group:groups){
            for (Style style:group.items){
                for (StyleName name:style.names){
                    if(name.selector.equals(selector)){
                        if(name.name.equals(from)){
                            name.name = to;
                        }
                    }
                }
            }
        }
    }


    public HashSet<String> getAllStyles(){
        HashSet<String> snames = new HashSet<>();
        for (Group group:groups) {
            for (Style style : group.items) {
                for (StyleName sn:style.names){
                    snames.add(sn.selector+sn.name);
                }
            }
        }
        return snames;
    }


    public HashSet<String> getCssVars(){
        HashSet<String> cssVars = new HashSet<>();
        for (Group group:groups) {
            for (Style style : group.items) {
                for (StyleParam param:style.items){
                    if(param.key.startsWith("--")){
                        cssVars.add(param.key);
                    }
                    getCssVarRec(param.value, cssVars);
                }
            }
        }
        return cssVars;
    }


    private void getCssVarRec(ICitem item, HashSet<String> cssVars){
        if(item instanceof Var){
            for (ICitem item0:((Var) item).items){
                getCssVarRec(item0, cssVars);
            }
        } else if (item instanceof SField) {
            SField val = (SField) item;
            if(val.value.startsWith("--")){
                cssVars.add(val.value);
            }
        }
    }


    public void renameVar(String from, String to){
        for (Group group:groups) {
            for (Style style : group.items) {
                for (StyleParam param:style.items){
                    if(param.key.equals(from)){
                        param.key = to;
                    }
                    renVarRec(param.value, from, to);
                }
            }
        }
    }


    private void renVarRec(ICitem item, String from, String to){
        if(item instanceof Var){
            for (ICitem item0:((Var) item).items){
                renVarRec(item0, from, to);
            }
        } else if (item instanceof SField) {
            SField val = (SField) item;
            if(val.value.equals(from)){
                val.value = to;
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }


    @Override
    public void toString(StringBuilder b) {
        for (Group group:groups){
            group.toString(b);
        }
    }
}