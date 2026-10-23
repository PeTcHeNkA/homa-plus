package plus.tacks;

import plus.Main;

import java.io.File;
import java.nio.file.Files;

public class TaskCopyTo implements Task{
    @Override
    public boolean run(String value) {
        try {
            Files.copy(new File(Main.directory).toPath(), new File(value.substring(5)).toPath());
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "copy";
    }
}
