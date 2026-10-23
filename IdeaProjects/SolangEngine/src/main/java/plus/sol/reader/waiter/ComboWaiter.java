package plus.sol.reader.waiter;


public class ComboWaiter implements IWaiter{
    private final char[] combo;
    private int cursor = 0;

    public ComboWaiter(String str) {
        this.combo = str.toCharArray();
    }

    @Override
    public void reset(){
        cursor = 0;
    }


    @Override
    public boolean wait(char c){
        if(cursor==-1)return false;
        if(c==combo[cursor]){
            if(cursor==combo.length-1){
                cursor = -1;
                return true;
            }
            cursor+=1;
            return false;
        }
        cursor = 0;
        return false;
    }


    @Override
    public boolean isActive() {
        return cursor>0;
    }


    @Override
    public String getValue() {
        return new String(combo);
    }
}
