package plus.te4net.core.cons;

import java.util.ArrayList;
import java.util.List;


public abstract class ParentableConsoleCommand extends ConsoleCommand implements AbstractParentableConsoleCommand{

    protected List<ConsoleCommandExucator> cmds = new ArrayList<>();
    public ParentableConsoleCommand(String name) {
        super(name);
    }


    @Override
    public void addChildCommand(ConsoleCommandExucator cmd){
        cmds.add(cmd);
    }


    @Override
    public void addChildCommand(ConsoleCommandExucator cmd, int priority){
        if(priority<cmds.size() && priority>-1){
            cmds.add(priority, cmd);
        } else cmds.add(cmd);
    }


    @Override
    public void onCommand(String... args){
        onSelfCommand();
        for(ConsoleCommandExucator c:cmds){
            c.onCommand(args);
        }
    }
}