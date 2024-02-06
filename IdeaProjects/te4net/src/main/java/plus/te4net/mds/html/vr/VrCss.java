package plus.te4net.mds.html.vr;

import plus.tson.utl.CharStrBuilder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;


public class VrCss implements ICitem {
    private static final HashSet<String> groupNames = new HashSet<>(Arrays.asList(
            "@media", "@layer", "@keyframes", "@document", "@container"
    ));

    static void last(StringBuilder builder, char c){
        builder.setCharAt(builder.length()-1, c);
    }


    private static class Group implements ICitem {
        private String header;
        private final ArrayList<ICitem> items = new ArrayList<>();
        @Override
        public void toString(StringBuilder b) {
            if(items.isEmpty())return;
            if(header!=null){
                b.append(header).append('{');
            }
            for (ICitem style:items){
                style.toString(b);
            }
            if(header!=null){
                b.append('}');
            }
        }

        public boolean isEmpty(){
            if(items.isEmpty())return true;
            for (ICitem item:items){
                if(item instanceof Style style){
                    if(!style.isEmpty())return false;

                } else {
                    return false;
                }
            }
            return true;
        }


        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            toString(b);
            return b.toString();
        }
    }

    private final ICitem comma = b -> b.append(',');

    static class Var implements ICitem {
        ArrayList<ArrayList<ICitem>> items;
        String header;

        @Override
        public void toString(StringBuilder b) {
            if(items==null) b.append(header);
            else {
                b.append(header).append('(');
                for (ArrayList<ICitem> item : items){
                    for (ICitem item0:item){
                        item0.toString(b);
                    }
                }
                b.append(')');
            }
        }


        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            toString(b);
            return b.toString();
        }


        private ArrayList<ICitem> add(ICitem e){
            if(items==null)
                items = new ArrayList<>();
            ArrayList<ICitem> res = new ArrayList<>();
            res.add(e);
            items.add(res);
            return res;
        }
    }

    private final ArrayList<Group> groups;
    private char[] chars;
    private int cursor;

    public VrCss(){
        groups = new ArrayList<>();
    }
    private VrCss(ArrayList<Group> groups){
        this.groups = groups;
    }


    public VrCss(String code) {
        groups = new ArrayList<>();
        chars = code.toCharArray();
        uncommentAll();
        parse();
        chars = null;
    }


    private void uncommentAll(){
        CharStrBuilder builder = new CharStrBuilder(chars.length);
        while (cursor < chars.length) {
            if(cursor+1>=chars.length)break;
            char cur = cur(), next = chars[cursor+1];
            if(cur=='/' && next=='*'){
                while (cursor+1 < chars.length){
                    if(cur()=='*'&&chars[cursor+1]=='/')break;
                    ++cursor;
                }
                cursor += 2;
                continue;
            }
            builder.append(cur);
            ++cursor;
        }
        chars = builder.toString().toCharArray();
        cursor = 0;
    }


    public static AnStyle readInline(String code){
        AnStyle style = new AnStyle();
        new VrCss(code, style);
        return style;
    }

    //inline
    private VrCss(String code, AnStyle style){
        groups = null;
        this.chars = code.toCharArray();

        StringBuilder builder = new StringBuilder();
        while (cursor<chars.length){
            int start = cursor;
            skipToNext();
            StyleParam param = readSParam(builder);
            if(param==null){
                ++cursor;
                continue;
            }
            if(param.key != null && param.value != null){
                style.items.add(param);
            }
            builder.setLength(0);
            if(start==cursor)throw syntaxError("Infinity loop");
        }
        if(1==1)return;
    }


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

                for (ICitem item: toAdd.items){
                    if((item instanceof Style addStyle)) {
                        boolean add = true;
                        for (ICitem mainItem : new ArrayList<>(main.items)) {
                            if (!(mainItem instanceof Style mainStyle)) continue;
                            if (mainStyle == addStyle) continue;
                            if (mainStyle.eqNames(addStyle)) {
                                add = false;
                                for (StyleParam param : addStyle.items) {
                                    mainStyle.items.add(param);
                                }
                            }
                        }
                        if (add) {
                            main.items.add(addStyle);
                        }
                    } else {
                        main.items.add(item);
                    }
                }
            }
        }
        return this;
    }


    public static boolean isEmpty(char c){
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }


    private static boolean isMatchUsed(char c){
        return c =='+'||c=='*'||c=='/';
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
//
//
//    private String readGroupName(){
//        if(
//                check('@','m','e','d','i','a') ||
//                check('@','c','o','n','t','a','i','n','e','r')
//        ){
//            StringBuilder builder = new StringBuilder();
//            builder.append(cur());
//            ++cursor;
//            while (cursor<chars.length){
//                char cur = cur();
//                if(cur=='{')return builder.toString();
//                if(isEmpty(cur)){
//                    char prev = chars[cursor-1];
//                    if(isEmpty(prev) || prev=='(' || prev==')' || prev==':'){
//                        ++cursor;
//                        continue;
//                    } else {
//                        char next = chars[cursor+1];
//                        if(isEmpty(next) || next=='(' || next==')' || next=='{'){
//                            ++cursor;
//                            continue;
//                        }
//                    }
//                }
//                ++cursor;
//                builder.append(cur);
//            }
//        }
//        return null;
//    }
//
//
//    private boolean check(char... chars0){
//        int lim = Math.min(cursor+chars0.length, chars.length);
//        int n = 0;
//        for (int i=cursor;i<lim;i++){
//            if(chars[i]!=chars0[n])return false;
//            n++;
//        }
//        return true;
//    }


    private void goTo(char c){
        while (cursor<chars.length){
            if(cur()==c)return;
            ++cursor;
        }
    }


    private String getErrorString(){
        int min = Math.max(0, cursor-70);
        int max = Math.min(cursor+70,chars.length);
        char[] bytes = new char[max-min];
        System.arraycopy(chars, min, bytes, 0, bytes.length);
        return new String(bytes);
    }


    private int getStrCode(){
        int count = 0, cur = 0;
        while (cur<cursor){
            if(chars[cur++]=='\n')++count;
        }
        return count;
    }


    private RuntimeException syntaxError(Object... o){
        return new RuntimeException("["+getStrCode()+"] Syntax error: "
                + Arrays.toString(o)+"\n----------\n"+getErrorString()+"\n----------\n"
        );
    }


    private boolean readStyle(Group group){
        if(cursor>=chars.length)return false;
        Style style = new Style();
        while (cursor<chars.length){
            int start = cursor;
            skipToNext();
            if(cur()=='}')return false;
            style.names.add(readStyleName());
            if(cur()==','){
                ++cursor;
            } else if (cur()=='{') {
                ++cursor;
                break;
            }
            if(start==cursor)throw syntaxError("Infinity loop");
        }
        StringBuilder builder = new StringBuilder();
        while (cursor<chars.length){
            int start = cursor;
            skipToNext();
            if(cursor>=chars.length)break;
            if(cur()=='}'){
                ++cursor;
                break;
            }
            StyleParam param = readSParam(builder);
            if(param==null){
                ++cursor;
                continue;
            }
            if(param.key != null && param.value != null){
                style.items.add(param);
            }
            builder.setLength(0);
            if(start==cursor)throw syntaxError("Infinity loop");
        }
        group.items.add(style);
        return true;
    }


    //todo
    private StyleParam readSParam(StringBuilder builder){
        skipToNext();
        if(cur()==';')return null;
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
        fillEntry(true, builder, new Var(), var, new AtomicBoolean(false));
        if(var.header != null && !var.header.isEmpty()){
            param.value.add(var);
        }
        return param;
    }


    public static Var parseVar(String str){
        VrCss vrcss = new VrCss((ArrayList<Group>) null);
        vrcss.chars = str.toCharArray();
        Var cur = new Var();

        vrcss.fillEntry(true, new StringBuilder(), new Var(), cur, new AtomicBoolean(false));

        return cur;
    }


    private void fillEntry(boolean root0, StringBuilder b, Var root, Var curv, AtomicBoolean signal){
        skipToNext();
        if(cur()=='}')return;
        b.setLength(0);
        while (cursor<chars.length){
            char cur = cur();
            ++cursor;
            if(cur=='('){
                curv.header = b.toString().trim();
                fillEntry(false, b, curv, new Var(), signal);
                if(signal.get())break;
                b.setLength(0);
                root.add(curv);
                curv = new Var();
                continue;
            }
            if(cur==';'||cur=='}') {
                if(cur=='}') --cursor;
                if(curv.header==null){
                    curv.header = b.toString().trim();
                }
                signal.set(true);
                break;
            }
            if(cur==')') {
                curv.header = b.toString().trim();
                root.add(curv);
                break;
            }
            if(root0){
                if(cur==' '){
                    char other = cur();
                    if(other==','||other==' ')continue;
                    if(b.length()>0) {
                        other = b.charAt(b.length() - 1);
                        if (other == ',' || other == ' ') continue;
                    }
                }
            } else {
                if (cur == ',') {
                    curv.header = b.toString().trim();
                    b.setLength(0);
                    root.add(curv);
                    curv = new Var();
                    root.add(comma);
                    continue;
                }
            }
            if(isMatchUsed(chars[cursor-1])&&isEmpty(cur))continue;
//            if(cur=='"'||cur=='\''){
//                b.append('\\');
//            }
            b.append(cur);
        }
        if(groups==null && cursor>=chars.length){
            if(curv.header==null){
                curv.header = b.toString().trim();
            }
            signal.set(true);
        }
        if(curv.items != null){
            if(curv.items.get(curv.items.size()-1)==comma){
                curv.items.remove(curv.items.size()-1);
            }
        }
    }


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


    private void readGroup0(){
        skipToNext();
        if(cursor>=chars.length)return;
        char cur = cur();
        if(cur=='@'){
            int start = cursor;
            StringBuilder b = new StringBuilder("@");
            boolean first = true;
            boolean flag = false;
            ++cursor;
            int type = 0;
            char prev = 0;
            while (cursor<chars.length){
                cur = cur();
                ++cursor;
                if(isEmpty(cur)){
                    if(isEmpty(prev))continue;
                    if(prev==':'||prev=='('||prev==')')continue;
                    if(first){
                        first = false;
                        flag = groupNames.contains(b.toString());
                    }
                }
                if(cur==';'){
                    type = 2;
                    break;
                }
                if(cur=='{'){
                    if(first){
                        flag = groupNames.contains(b.toString());
                    }
                    if(flag){
                        type = 1;
                    } else {
                        type = 3;
                        cursor = start;
                    }
                    break;
                }
                b.append(cur);
                prev = cur;
            }
            if(type==2){
                root().items.add(new SField(b.toString()));
                return;
            }
            if(type==1){
                Group group = new Group();
                group.header = b.toString();
                b.setLength(0);
                while (cursor<chars.length){
                    skipToNext();
                    if(cursor>=chars.length)throw syntaxError();
                    if(cur()=='}'){
                        ++cursor;
                        break;
                    }
                    readStyle(group);
                }
                groups.add(group);
                return;
            }
            if(type==3){
                readStyle(root());
            }
        } else {
            readStyle(root());
            skipToNext();
        }
    }


    private Group root(){
        for (Group group:groups){
            if(group.header==null)return group;
        }
        Group g;
        groups.add(g = new Group());
        return g;
    }


    private VrCss parse(){
        while (cursor<chars.length){
            int start = cursor;
            readGroup0();
            if(start==cursor)throw syntaxError("Infinity loop");
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
            Iterator<ICitem> st = group.items.iterator();
            while (st.hasNext()){
                ICitem item = st.next();
                if(!(item instanceof Style))continue;
                Style style = (Style) item;
                if(style.isEmpty()) st.remove();
            }
            if(group.isEmpty()){
                e.remove();
            }
        }
    }


    public void unCollide(){
        HashMap<String,Style> indexes = new HashMap<>();
        for (Group group: groups) {
            indexes.clear();
            Iterator<ICitem> curItr = group.items.iterator();
            while (curItr.hasNext()) {
                ICitem item = curItr.next();
                if(!(item instanceof Style))continue;
                Style style = (Style) item;
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
            for (ICitem item:group.items){
                if(!(item instanceof Style style))continue;
                for (StyleName name:style.names){
                    if(name.selector.equals(selector)){
                        if(Objects.equals(name.name, from)){
                            name.name = to;
                            break;
                        }
                    }
                }
            }
        }
    }


    void extractStyle(String selector, String from, AnStyle result){
        Style result0 = null;
        for (Group group:groups){
            for (ICitem item:group.items){
                if(!(item instanceof Style style))continue;
                if(style.names.size()>1)continue;
                StyleName name0 = style.names.get(0);
                if(name0.selector.equals(selector)) {
                    if (Objects.equals(name0.name, from)) {
                        if(result0 != null)return;
                        result0 = style;
                    }
                }
            }
        }
        if(result0==null)return;
        result0.names.get(0).name = null;
        for (StyleParam p:result0.items){
            result.addParam(p);
//            if(result.getParam(p.key)==null){
//                result.items.add(p);
//            }
        }
    }


    public void forStyles(BiConsumer<Group,Style> consumer){
        for (Group group:groups) {
            for (ICitem item:group.items){
                if(!(item instanceof Style style))continue;
                consumer.accept(group, style);
            }
        }
    }


    boolean isSingle(String selector, String name){
        //if(1==1)return false;
        int count = 0;
        for (Group group:groups) {
            for (ICitem item : group.items) {
                if(!(item instanceof Style style))continue;
                if(style.names.size()==1){
                    StyleName name0 = style.names.get(0);
                    if(name0.selector.equals(selector) && name0.name.equals(name)){
                        ++count;
                    }
                }
            }
        }
        return count == 1;
    }


    public HashSet<String> getAllStyles(){
        HashSet<String> sNames = new HashSet<>();
        for (Group group:groups) {
            for (ICitem item : group.items) {
                if(!(item instanceof Style style))continue;
                for (StyleName sn:style.names){
                    sNames.add(sn.selector+sn.name);
                }
            }
        }
        return sNames;
    }


    public HashSet<String> getCssVars(){
        HashSet<String> cssVars = new HashSet<>();
        for (Group group:groups) {
            for (ICitem item : group.items) {
                if(!(item instanceof Style style))continue;
                for (StyleParam param:style.items){
                    if(param.key.startsWith("--")){
                        cssVars.add(param.key);
                    }
                    for (ICitem item0:param.value){
                        getCssVarRec(item0, cssVars);
                    }
                }
            }
        }
        return cssVars;
    }


    private void getCssVarRec(ICitem item, HashSet<String> cssVars){
        if(item instanceof Var){
            ArrayList<ArrayList<ICitem>> items1 = ((Var) item).items;
            if(items1==null)return;
            for (ArrayList<ICitem> item0:items1){
                for (ICitem iCitem1:item0){
                    getCssVarRec(iCitem1, cssVars);
                }
            }
        } else if (item instanceof SField val) {
            if(val.value.startsWith("--")){
                cssVars.add(val.value);
            }
        }
    }


    public void renameVar(String from, String to){
        renameVar(from, to, to);
    }


    public void renameVar(String from, String keyTo, String varTo){
        for (Group group:groups) {
            for (ICitem item : group.items) {
                if(!(item instanceof Style style))continue;
                style.renameVar(from, keyTo, varTo);
            }
        }
    }


    public Style get(ArrayList<StyleName> names){
        for (Group group:groups) {
            for (ICitem item : group.items) {
                if(!(item instanceof Style style))continue;
                if(style.eqNames(names)){
                    return style;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }


    public boolean isEmpty(){
        if(groups.isEmpty())return true;
        for (Group group:groups){
            if(!group.isEmpty())return false;
        }
        return true;
    }


    @Override
    public void toString(StringBuilder b) {
        for (Group group:groups){
            group.toString(b);
        }
    }
}