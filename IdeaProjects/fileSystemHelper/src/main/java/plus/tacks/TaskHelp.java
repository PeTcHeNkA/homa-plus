package plus.tacks;

import plus.Main;

public class TaskHelp implements Task{

    @Override
    public boolean run(String value) {
        System.out.println("Tasks ===");
        for(String s:Main.manager.tasks.keySet()){
            System.out.println("| "+s);
        }
        System.out.println("=========");
        return true;
    }

    @Override
    public String getName() {
        return "help";
    }
}
