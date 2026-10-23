package plus.tacks;

import plus.Main;

import java.io.File;

public class TaskGetDir implements Task{

    public TaskGetDir(){
        File currentDirectory = new File(new File("").getAbsolutePath());
        Main.directory = currentDirectory.getPath()+File.separator;
        run(null);
    }

    @Override
    public boolean run(String value) {
        System.out.println(Main.directory);
        return true;
    }

    @Override
    public String getName() {
        return "dir";
    }
}
