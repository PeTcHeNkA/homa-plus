package plus.quest.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import plus.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.UUID;


public class UIDLogger {

    private final File file;
    private final ArrayList<UUID> ids = new ArrayList<>();

    protected UIDLogger(){
        this.file = new File(Main.instance.getDataFolder()+File.separator+"uid_cache.cache");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        } else {
            String[] s = new String[0];

            try {
                s = new String(Files.readAllBytes(file.toPath())).split("\n");
            } catch (IOException e) { e.printStackTrace(); }
            clearCache();
            for(String v:s){
                try {
                    Entity e = Bukkit.getEntity(UUID.fromString(v));
                    if(e==null)continue;
                    e.remove();
                } catch (Exception ignored){}
            }
        }
    }

    private void clearCache(){
        try {
            Files.newBufferedWriter(file.toPath() , StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void write(UUID data){
        try {
            Files.write(file.toPath(), data.toString().getBytes(), StandardOpenOption.APPEND);
            ids.add(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte n  = 0;
    protected void remove(UUID data){
        ids.remove(data);
        if(n>20) {
            if (ids.isEmpty()) clearCache();
            n=0;
        }
        n++;
    }
}