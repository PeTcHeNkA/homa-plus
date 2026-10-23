package plus;

import plus.utls.Te4hnoConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Timer {

    private Te4hnoConfig c;

    private int timeMinutes;

    //private int coolDown;
    //private int timeSave;
    //private int timeToSave;
    private boolean Enabled = true;

    //private int timeTrigger;
    private List<Integer> triggers = new ArrayList<>();

    public Timer(){
        c = main.config;

        Date d = new Date();
        System.out.println(String.format("Timer was activated! [%s]", d.toString()));
        timeMinutes = ((d.getHours()*60)+d.getMinutes());

        for(String hh:c.getString("trigger").split(";")) {
            String[] h = hh.split(":");
            triggers.add((Integer.parseInt(h[0]) * 60) + Integer.parseInt(h[1]));
        }

        Runnable task = () ->{
            while (Enabled) {
                //timeToSave += 1;
                //if (timeToSave >= timeSave) {
                //    ExternalSave();
                //}
                if(triggers.contains(timeMinutes)){
                    timeMinutes=0;
                    main.m.SpawnBlock();
                }
                timeMinutes += 1;
                if(timeMinutes>1440){
                    timeMinutes=0;
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }
        };

    }
    public void Disable(){
        Enabled = true;
    }
    //public void ExternalSave(){
        //c.set("time", timeMinutes);
    //    c.save();
        //timeToSave = 0;
    //}
}
