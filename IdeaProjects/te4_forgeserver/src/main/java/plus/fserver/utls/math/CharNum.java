package plus.fserver.utls.math;


public final class CharNum {
    public static final CharNum Default = new CharNum(
            "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM!=+-_*&^:;.,/#@%?~()[]{}<>"
    );
    private final char[] charLib;
    private final char[] codes;


    public CharNum(String lib){
        charLib = lib.toCharArray();
        char max = 0;
        for (char cur : charLib) {
            if (cur > max) max = cur;
        }
        codes = new char[max+1];
        for(char i=0;i<charLib.length;i++){
            codes[charLib[i]] = i;
        }
    }


    public int len(){
        return charLib.length;
    }


    public int numOfStr(String str){
        return numOfChars(str.toCharArray());
    }


    public int numOfChars(char... chars){
        int num = 0;
        if(chars.length==0)return num;
        for(char c:chars){
            num = (num * charLib.length) + codes[c];
        }
        return num;
    }


    public String getCharNumOf(int i){
        if(i==0) return String.valueOf(charLib[0]);
        StringBuilder s = new StringBuilder();
        while (i>0){
            int index = i % charLib.length;
            s.insert(0, charLib[index]);
            i = i / charLib.length;
        }
        return s.toString();
    }
}