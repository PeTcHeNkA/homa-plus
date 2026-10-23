package plus.sol.reader.waiter;


public class ToEndWaiter implements IWaiter{
    private final char end;
    private boolean active = true;
    private StringBuilder buffer;

    public ToEndWaiter(char end){
        this.end = end;
        reset();
    }


    @Override
    public boolean isActive(){
        return active;
    }


    @Override
    public void reset(){
        buffer = new StringBuilder();
        active = true;
    }


    @Override
    public boolean wait(char c){
        if(c==end){
            if(active){
                active = false;
                return true;
            }
            return false;
        }
        if(active) buffer.append(c);
        return false;
    }


    @Override
    public String getValue(){
        return buffer.toString();
    }
}