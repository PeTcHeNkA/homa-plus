package plus.sol.reader.waiter;


public class SmWaither {
    protected final char start;
    protected final char end;
    protected boolean active = false;
    protected StringBuilder buffer;

    public SmWaither(char start, char end){
        this.start = start;
        this.end = end;
        reset();
    }


    public boolean isActive(){
        return active;
    }


    public void reset(){
        buffer = new StringBuilder();
        active = false;
    }


    public boolean wait(char c){
        if(c==start && !active){
            reset();
            active = true;
            return false;
        }
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


    public String getValue(){
        return buffer.toString();
    }
}
