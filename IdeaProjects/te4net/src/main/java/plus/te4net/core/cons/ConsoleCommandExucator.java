package plus.te4net.core.cons;


public interface ConsoleCommandExucator {
    String getName();


    default String help(){return null;};


    void onCommand(String... args);
}