package plus.te4net.mds.html.vr;


public class FastJsOptimizer {
    private StringBuilder builder;
    private char[] chars;
    int cursor = 0;

    public FastJsOptimizer(String src) {
        init(src);
    }


    private void init(String src){
        this.chars = src.toCharArray();
        builder = new StringBuilder();
        cursor = 0;
    }


    public String doScan(){
        uncommentAll();

        //if(1==1)return toString();
        init(toString());
        doSkan0();

        return fixLines();
    }


    private String fixLines(){
        String str = toString();
        builder = new StringBuilder();
        for (String str0:str.split("\n")){
            str0 = str0.trim();
            if(!str0.isEmpty()) {
                builder.append(str0);
                if(str0.charAt(str0.length()-1)!=';'){
                    builder.append('\n');
                }
            }
        }
        return builder.toString().trim();
    }


    private void doSkan0(){
        skipToNextE();
        try {
            while (cursor < chars.length) {
                int last = -1;
                while (last != cursor) {
                    last = cursor;
                    checkDoScan();
                }
                write1l();
            }
        } catch (ArrayIndexOutOfBoundsException e){}
    }


    private void checkDoScan(){
        checkKeyChar();
        checkString();
        checkRex();
        checkStartBlock();
    }


    private void uncommentAll(){
        while (cursor < chars.length) {
            checkString();
            if(cursor+1>=chars.length)return;
            char cur = cur(), next = chars[cursor+1];
            if(cur=='/'&&next=='/'){
                while (cursor<chars.length && cur() != '\n'){
                    ++cursor;
                }
                continue;
            }
            if(cur=='/' && next=='*'){
                while (!check('*','/')){
                    ++cursor;
                }
                cursor += 2;
                continue;
            }
            checkRex();
            write1l();
        }
    }


    private void skipToNext(){
        while (cursor<chars.length && isEmpty(cur())){
            ++cursor;
        }
    }


    private void skipToNextE(){
        while (cursor<chars.length && isEmptyE(cur())){
            ++cursor;
        }
    }


    private void checkStartBlock(){
        if(cursor>=chars.length)return;
        if(cur()=='{' || cur()=='['){
            write1();
            skipToNextE();
        }
    }


    private void checkKeyChar(){
        checkString();
        checkRex();
        if(cursor>=chars.length)return;
        switch (cur()){
            case '=':
            case '-':
            case '>':
            case '<':
            case '+':
            case ':':
            case ',':
            case '!':
            case ';':
            case '|':
            case '&':
                remEmptyE();
                write1();
                skipToNext();
                return;
            case '\n':
                checkNewLine();
                return;
            case '}':
                remEmptyE();
                checkEndBlock();
        }
    }


    private void checkNewLine(){
        remEmpty();
        int cur = cursor-1;
        char cr = ' ';
        while (cur>0){
            cr = chars[cur];
            //????
            if(!isEmptyE(cr))break;
            --cur;
        }
        if(cur<=0)return;
        switch (cr){
            case ';':
            case '>':
            case '<':
            case '=':
            case '-':
            case '.':
            case ',':
            case '{':
                skipToNextE();
                break;
            case '}':
                checkEndBlock();
                break;
            case ')':
                write1();
                skipToNextE();
                break;
            default:
                write1();
                skipToNextE();
                break;
        }
    }


    private void remEmptyE(){
        int cursor2 = builder.length()-1;
        while (cursor2>0){
            if(isEmptyE(builder.charAt(cursor2))){
                builder.deleteCharAt(cursor2);
            } else {
                break;
            }
            --cursor2;
        }
    }


    private void remEmpty(){
        int cursor2 = builder.length()-1;
        while (cursor2>0){
            if(isEmptyE(builder.charAt(cursor2))){
                builder.deleteCharAt(cursor2);
            } else {
                break;
            }
            --cursor2;
        }
    }


    private void checkString(){
        if(cursor>=chars.length)return;
        char c = cur();
        switch (c) {
            case '\'', '"', '`' -> {
                write1();
                before(c, this::beforeEndStr);
                if (cursor >= chars.length) return;
                write1();
                skipToNext();
            }
        }
    }
    private void checkRex(){
        if(cursor>=chars.length)return;
        char c = cur();
        if(c=='/'){
            write1();
            beforeRex();
            if (cursor >= chars.length) return;
            write1();
            skipToNext();
        }
    }


    private void beforeRex(){
        while (cursor<chars.length){
            app1();
            char prev = chars[cursor-1];
            if(chars[cursor] == '/' && (prev=='$' || prev==')')) {
                ++cursor;
                break;
            }
            if(cur()=='\\'){
                char next = chars[cursor+1];
                if(next=='\'' || next=='/'){
                    cursor++;
                    app1();
                }
            }
            ++cursor;
        }
    }


    private void beforeEndStr(){
        app1();
        if(cur()=='\\'){
            char next = chars[cursor+1];
            if(next=='\'' || next=='`' || next=='"'){
                cursor++;
                app1();
            }
        }
    }


    private void checkEndBlock(){
        write1();
        if(cursor>=chars.length)return;
        write1();
        skipToNext();
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


    private char cur(){
        return chars[cursor];
    }


    private void before(char c, Runnable r){
        while (cursor<chars.length && c != chars[cursor]){
            r.run();
            ++cursor;
        }
    }


    private void write1l(){
        if(cursor>=chars.length)return;
        if(!(isEmpty(chars[cursor]) && cursor > 0 && isEmpty(chars[cursor-1]))){
            app1();
        }
        ++cursor;
    }


    private void app1(){
        builder.append(chars[cursor]);
    }


    private void write1(){
        app1();
        ++cursor;
    }


    private boolean isEmpty(char c){
        return c == ' ' || c == '\t';
    }


    private boolean isEmptyE(char c){
        return c == ' ' || c == '\t' || c == '\n';
    }


    @Override
    public String toString() {
        return builder.toString();
    }
}