package plus.util.cmd;


public class CommandArgs{
    private final String[] args;
    private int shift = 0;

    public CommandArgs(String[] args) {
        this.args = args;
    }

    public String get(){
        if(shift>=args.length)return null;
        return args[shift];
    }

    public String get(int index){
        index = index + shift;
        if(shift>=args.length || shift<0)return null;
        return args[index];
    }


    public String getAndShift(){
        if(shift>=args.length)return null;
        return args[shift++];
    }


    public int size(){
        return (args.length-shift)+1;
    }


    public void shift(){
        ++shift;
    }


    public boolean isEmpty(){
        return args.length==0;
    }


    public boolean isLast(){
        return shift == args.length;
    }


    public int getShift(){
        return shift;
    }


    public String[] getValues(){
        return args;
    }
}