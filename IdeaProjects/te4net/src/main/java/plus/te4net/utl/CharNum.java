package plus.te4net.utl;


public final class CharNum {
    public static final CharNum OnlyChars = new CharNum("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
    public static final CharNum Default = new CharNum("0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
    private final char[] charLib;


    public CharNum(String lib){
        charLib = lib.toCharArray();
    }


    public int len(){
        return charLib.length;
    }


    public char[] genCode(int size){
        char[] chars = new char[size];
        for(int i=0;i<chars.length;i++){
            chars[i] = nextRChar();
        }
        return chars;
    }


    private char nextRChar(){
        return charLib[FastRandom.INST.nextInt(charLib.length)];
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