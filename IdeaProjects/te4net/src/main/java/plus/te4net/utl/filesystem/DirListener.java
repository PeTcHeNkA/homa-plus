package plus.te4net.utl.filesystem;

import plus.te4net.core.node.HWorker;
import plus.te4net.utl.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.nio.file.StandardWatchEventKinds.*;


public class DirListener extends Thread{
    private static final Logger Log = Logger.get();
    public enum Event{
        CREATE, DELETE, MODIFY
    }
    public interface Listener{
        void onCall(HWorker worker, Event e, String name);
    }

    private final WatchService watchService;
    private final HWorker worker;
    private Listener[] listeners = new Listener[0];


    public DirListener(HWorker worker, String dir)  {
        this(worker, new File(dir));
    }

    public DirListener(HWorker worker, File dir)  {
        this.worker = worker;
        Path path = dir.toPath();
        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setName("SYSTEM");
        setDaemon(true);
        start();
    }


    public void addListener(Listener l){
        List<Listener> newl = new ArrayList<>(Arrays.asList(listeners));
        newl.add(l);
        listeners = newl.toArray(new Listener[0]);
    }


    @Override
    public void run() {
        while (true) {
            WatchKey key = null;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                if (ENTRY_CREATE == event.kind()) {
                    callEvent(Event.CREATE, event.context().toString());
                } else if (ENTRY_DELETE == event.kind()) {
                    callEvent(Event.DELETE, event.context().toString());
                } else if (ENTRY_MODIFY == event.kind()) {
                    callEvent(Event.MODIFY, event.context().toString());
                }
            }
            key.reset();
        }
    }


    private void callEvent(Event e, String s){
        Log.info(String.format("%s %s",e,s));
        for(Listener l:listeners){
            l.onCall(worker, e, s);
        }
    }
}
