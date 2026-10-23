package plus;

import plus.tacks.*;
import plus.worker.ConsoleWorker;
import plus.worker.Standart;
import plus.worker.Worker;

import java.io.Console;
import java.util.Scanner;

public class Main {

    public static boolean enabled = true;

    public static TaskManager manager = new TaskManager();

    public static String directory = "";

    public static void Stop(){
        enabled = false;
    }

    public static void main(String[] args){

        registreTasks();
        System.out.println("------------");

        Worker w;
        if(System.console()==null){
            System.out.println("Enabled Standart");
            w = new Standart();
        }
        else {
            System.out.println("Enabled Console");
            w = new ConsoleWorker();
        }
        w.run();
    }
    public static void registreTasks(){
        manager.addNewTask(new TaskGetDir());
        manager.addNewTask(new TaskChangeDir());
        manager.addNewTask(new TaskGetFiles());
        manager.addNewTask(new TaskDirIsFile());
        manager.addNewTask(new TaskCopyTo());
    }

}
