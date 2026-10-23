package plus.tacks;

import plus.Main;

import java.io.File;

public class TaskDirIsFile implements Task{

    @Override
    public boolean run(String value) {
        File f = new File(Main.directory);
        if(!f.exists()){
            System.out.println("Patch is not exist!");
            return false;
        } else {
            if(f.isFile()){
                System.out.println("Patch is file");
            } else {
                System.out.println("Patch is catalog");
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "type";
    }
}
