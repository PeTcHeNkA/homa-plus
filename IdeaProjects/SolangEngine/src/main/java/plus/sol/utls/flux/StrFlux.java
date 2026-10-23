package plus.sol.utls.flux;

import plus.sol.utls.chr.CharSetBase;


public class StrFlux extends CharSetBase {
    protected int cursor = 0;

    public StrFlux(String s) {
        super(s);
    }


    public int getCursor(){
        return cursor;
    }


    public void setCursor(int cursor){
        this.cursor = cursor;
    }


    public boolean hasNext() {
        return cursor < chars.length;
    }


    public char current(){
        return chars[cursor];
    }


    public char next() {
        return chars[cursor++];
    }
}