package plus.tacks;

import plus.Main;

import java.io.File;

public class TaskGetFiles implements Task{
    @Override
    public boolean run(String value) {
        File f = new File(Main.directory);
        if(f.isDirectory()){
            System.out.println("Files ===");
            for (String ff:f.list()){
                System.out.println(ff);
            }
            System.out.println("=========");
            return true;
        } else {
            System.out.println(String.format("Patch not directory! [%s]", Main.directory));
            return false;
        }
    }

    @Override
    public String getName() {
        return "fls";
    }
}
