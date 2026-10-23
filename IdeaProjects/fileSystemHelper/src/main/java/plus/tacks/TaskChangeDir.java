package plus.tacks;

import plus.Main;

import java.io.File;

public class TaskChangeDir implements Task{
    @Override
    public boolean run(String value) {
        //System.out.println(value);
        String value2 = value.substring(3);
        value2 = value2.replace("/", File.separator);
        Main.directory = value2;
        return true;
    }

    @Override
    public String getName() {
        return "cd";
    }
}

