package plus.util.cons;


public interface AbstractParentableConsoleCommand extends ConsoleCommandExucator{

    void onSelfCommand(String... args);


    void addChildCommand(ConsoleCommandExucator cmd);


    void addChildCommand(ConsoleCommandExucator cmd, int priority);
}
