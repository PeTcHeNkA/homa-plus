package plus.music;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plus.regions.RegionProvider;

import java.util.HashMap;


public class MusicSession{

    private static final HashMap<String, MusicSession> activeSessions = new HashMap<>();

    private MusicThread T;
    private Player player;
    private boolean allowWaiting;
    private BSound last = null;


    public static void startNew(Player player){
        MusicSession s = activeSessions.get(player.getName());
        if(s != null){
            s.restart();
        } else {
            activeSessions.put(player.getName(), new MusicSession(player));
        }
    }


    public static void stopFor(Player player){
        MusicSession s = activeSessions.get(player.getName());
        if(s != null){
            s.stop();
        }
    }


    public static void updateFor(String player){
        MusicSession s = activeSessions.get(player);
        if(s == null){
            Player pl = Bukkit.getPlayer(player);
            if(pl==null){
                System.out.println("Can not update for null player!");
            } else startNew(pl);
        } else {
            s.update();
        }
    }


    public void update(){
        if(last == null || last.getCategory() != RegionProvider.getDefault().getCategory(player)) {
            if (allowWaiting) {
                T.restart();
            } else {
                if (T != null) T.stop();
                T = buildThread();
                T.start();
            }
        }
    }


    private MusicSession(Player pl){
        this.player = pl;
        T = buildThread();
    }


    private MusicThread buildThread(){
        return new MusicThread() {
            @Override
            public void runTask(){
                BSound s = RegionProvider.getDefault().getCategory(player).getRandomSound();
                if(last != null)last.stopSound(player);
                while (Enabled){
                    last = s;
                    s.play(player);
                    if(s.getBreakPoints().isEmpty()){
                        allowWaiting = false;
                        try { Thread.sleep(s.getDuration()); } catch (InterruptedException ignored) {}
                        s = RegionProvider.getDefault().getCategory(player).getRandomSound();

                    } else {

                        for (Point point : s.getBreakPoints().values()) {
                            allowWaiting = point.allowWaiting();
                            try { Thread.sleep(point.intValue); } catch (InterruptedException ignored) {}
                            MCategory category = RegionProvider.getDefault().getCategory(player);
                            if(
                                    category.id != s.getCategory().id &&
                                            (s.getCategory().isPriorityOut() || category.isPriorityEntry())
                            ){
                                s = category.getRandomSound();
                                last.stopSound(player);
                                break;
                            }
                        }
                    }
                }
            }
        };
    }


    public void stop(){
        if(last != null) last.stopSound(player);
        else return;
        T.stop();
        T = null;
        allowWaiting = false;
        last = null;
    }


    public void restart(){
        if(allowWaiting){
            T.restart();
        } else {
            if(T != null){
                T.stop();
            }
            if (player == null || !player.isOnline()) {
                player = Bukkit.getPlayer(player.getName());
                if (player == null) {
                    T = null;
                    allowWaiting = false;
                    last = null;
                    return;
                }
            }
            T = buildThread();
            T.start();
        }
    }
}


abstract class MusicThread extends Thread{
    protected boolean Enabled = true;
    private Runnable postFinishProcess;

    public void finish(Runnable postFinishProcess){
        Enabled = false;
        this.postFinishProcess = postFinishProcess;
    }

    public void restart(){
        finish(() -> {
            Enabled = true;
            reRun();
        });
    }

    abstract void runTask();

    private void reRun(){
        runTask();
        if(postFinishProcess!=null) postFinishProcess.run();
    }

    @Override
    public final void run(){
        reRun();
    }
}