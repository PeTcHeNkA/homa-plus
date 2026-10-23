package plus.valcirias.avatar;


public class Avatar {

    private final String groupName;
    private final int id;
    private final String symbol;
    private final int cmd;

    public Avatar(String groupName, int id,char symbol, int cmd){
        this.groupName = groupName;
        this.symbol = String.valueOf(symbol);
        this.cmd = cmd;
        this.id = id;
    }


    public int getCmd() {
        return cmd;
    }


    public String getSymbol() {
        return symbol;
    }


    public int getId(){
        return id;
    }


    public String getGroupName(){
        return groupName;
    }


    public String toString(){
        return String.format("%s:%s",groupName, id);
    }
}
