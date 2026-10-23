package plus;

import plus.tacks.NullTask;
import plus.tacks.Task;
import plus.tacks.TaskHelp;
import plus.tacks.TaskStop;

import java.util.HashMap;

public class TaskManager {

    public final HashMap<String, Task> tasks = new HashMap<>();
    private final NullTask nullTask = new NullTask();

    public TaskManager(){
        addNewTask("stop", new TaskStop());
        addNewTask(new TaskHelp());
    }
    public void addNewTask(String s, Task t){
        tasks.put(s, t);
    }

    public void addNewTask(Task t){
        tasks.put(t.getName(), t);
    }

    public Task getTask(String s){
        return tasks.getOrDefault(s, nullTask);
    }
}
