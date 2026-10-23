package plus.sol.utls.chr;


public class CharSetBase {
    protected final char[] chars;

    public CharSetBase(String s) {
        this(s.toCharArray());
    }

    public CharSetBase(char... chars) {
        this.chars = chars;
    }


    public int length() {
        return chars.length;
    }


    @Override
    public String toString() {
        return new String(chars);
    }
}