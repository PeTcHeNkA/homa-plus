package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start handle...");

        String dir = "C:\\Users\\Te4hnoPlus\\AppData\\Local\\Temp";
        //String dir = "C:\\Users\\Te4hnoPlus\\IdeaProjects\\Mimick0\\src\\test\\resources";
        String res = "C:\\Users\\Te4hnoPlus\\IdeaProjects\\Mimick0\\result";

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(dir);
        //будем следить за созданием, изменение и удалением файлов.
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        boolean poll = true;
        int num = 0;
        while (poll) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Event: " + event.kind() + " : " + event.context());
                Path path0 = (Path) event.context();

                copyFileUsingChannel(new File(dir, path0.toString()), new File(res, num+"_"+event.context()));

                ++num;
            }

            poll = key.reset();
        }
    }


    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            if(!dest.exists()){
                dest.createNewFile();
            }
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (Exception e){
            System.err.println("ERROR COPY");
        } finally{
            try {
                if(sourceChannel != null) sourceChannel.close();
                if(destChannel != null) destChannel.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}