package plus.tacks;

import plus.Main;

public class TaskStop implements Task{
    @Override
    public boolean run(String value) {
        System.out.println("Disabled main thread.");
        Main.Stop();
        return true;
    }

    @Override
    public String getName() {
        return "stop";
    }
}
