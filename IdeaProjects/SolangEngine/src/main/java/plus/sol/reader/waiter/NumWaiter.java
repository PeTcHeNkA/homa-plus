package plus.sol.reader.waiter;

import plus.sol.utls.chr.CharSet;


public class NumWaiter extends SmWaither{
    private static final CharSet whiteList = new CharSet("0123456789");
    public NumWaiter(char start, char end) {
        super(start, end);
    }


    public boolean wait(char c){
        if(active){
            if(!whiteList.contains(c)){

            }
        }
        return false;
    }
}
