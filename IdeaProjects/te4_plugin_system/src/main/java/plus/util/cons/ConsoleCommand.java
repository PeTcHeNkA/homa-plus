package plus.util.cons;


public abstract class ConsoleCommand implements ConsoleCommandExucator{
    protected final String name;
    public ConsoleCommand(String name){
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }
}