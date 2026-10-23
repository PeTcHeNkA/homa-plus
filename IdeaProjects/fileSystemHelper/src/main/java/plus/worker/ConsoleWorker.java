package plus.worker;

import plus.Main;

import java.io.Console;
import java.util.Scanner;

public class ConsoleWorker implements Worker{



    @Override
    public void run() {
        Console console = System.console();

        while(Main.enabled) {
            try {
                String s = console.readLine();
                if(Main.manager.getTask(s.split(" ")[0]).run(s)){
                    System.out.println("Successful!");
                } else {
                    System.out.println("Error!");
                }
            } catch (Exception e){
                System.out.println("Critical Error!");
                System.out.println("------------");
                System.out.println(e.getMessage());
                System.out.println("------------");
            }
        }
        System.out.println("------------");

    }
}
