package plus.util.cons;


public interface ConsoleCommandExucator {
    String getName();


    default String help(){return null;};


    void onCommand(String... args);
}