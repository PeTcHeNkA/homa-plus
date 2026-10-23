package plus.block;

import plus.util.YandeRePlugin;
import plus.util.cfg.Config;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AirBoxManager {

    public List<AirBox> boxes = new ArrayList<>();
    public boolean enabled = true;
    private int realTime;


    public AirBoxManager(YandeRePlugin p){
        Config data = p.getOrCreateYandeReConfig("rpg_chests.yml");
        if(data.getConfigurationSection("airboxes")!=null) {
            String[] keys = data.getConfigurationSection("airboxes").getKeys(false).toArray(new String[0]);
            for (String key : keys) {
                String patch = "airboxes."+key;
                boxes.add(
                        new AirBox(
                                data.getOrSetString(patch+"chest", "none"),
                                data.getOrSetInt(patch+"time_exist", 10),
                                data.getOrSetString(patch+"cords", "900;-900;900;-900"),
                                data.getOrSetString(patch+"world", "world"),
                                p,
                                data.getOrSetString("activation", "cd:1-10")
                        )
                );
            }
            Date d = new Date();
            System.out.println(String.format("Timer was activated! [%s]", d.toString()));
            realTime = ((d.getHours()*60)+d.getMinutes());

            Runnable task = () ->{
                while (enabled) {
                    for(AirBox box:boxes){
                        if(box.active(realTime))
                            box.SpawnBlock();
                    }

                    realTime += 1;
                    if(realTime>1440){
                        realTime=0;
                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ignored) {}
                }
            };
            Thread t = new Thread(task);
            t.start();
        } else {
            System.out.println("Air Boxes is null, disabled this module.");
            enabled = false;
        }
    }


    public void disable(){
        if(enabled)enabled=false;
        for(AirBox b:boxes){
            b.onDisable();
        }
    }
}