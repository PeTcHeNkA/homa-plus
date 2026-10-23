package plus.music;

import plus.Main;
import plus.utils.config.Sector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MCategory {

    protected static final Random r = new Random();
    public final int id;
    private final int priority;
    private final boolean priorityEntry;
    private final boolean priorityOut;
    protected final ArrayList<BSound> sounds = new ArrayList<>();


    protected MCategory(Sector c, String path, int defId) {
        this.id = c.getOrSetInt(path+".id", 0);
        m.put(id, this);
        this.priority = c.getOrSetInt(path+".priority", defId);
        this.priorityEntry = c.getOrSetBoolean(path+".priority_entry", false);
        this.priorityOut = c.getOrSetBoolean(path+".priority_out", false);

        if(!c.contains("music")){
            c.getOrSetInt("music.example.category_id", 0);
        }

        for(String key:c.getConfigurationSection("music").getKeys(false)){
            if(c.getOrSetInt("music."+key+".category_id", 0)==id){
                sounds.add(BSound.build(c,"music."+key));
            }
        }
        System.out.printf("| loaded category [%s], id=%s, sounds=%s%n", path, id, sounds.size());
    }


    public final ArrayList<BSound> getSounds(){
        return sounds;
    }


    public BSound getRandomSound(){
        return sounds.get(r.nextInt(sounds.size()));
    }


    public int getPriority(){
        return priority;
    }


    public boolean isPriorityEntry(){
        return priorityEntry;
    }


    public boolean isPriorityOut(){
        return priorityOut;
    }


    private static final HashMap<Integer, MCategory> m = new HashMap<>();
    public static MCategory Default;


    public static MCategory valueOf(Integer i){
        return m.get(i);
    }


    public static void loadStatic(){
        Main.c.reload();
        Sector c = Main.c;
        m.clear();

        if(!c.contains("categories")){
            c.getOrSetInt("categories.example.id", 1);
        }
        if(!c.contains("default_category")){
            c.getOrSetInt("default_category.id", 0);
        }
        Default = build(c, "default_category", 0);

        int id = 0;
        for(String key:c.getConfigurationSection("categories").getKeys(false)){
            String path = "categories."+key;
            MCategory mc = build(c, path, id);
            id = mc.id;
        }
    }


    public static MCategory build(Sector c, String path, int defId){
        if(c.getOrSetBoolean(path+".use_time", false)){
            return new TCategory(c, path, defId);
        } else return new MCategory(c, path, defId);
    }
}


class TCategory extends MCategory{

    private final TimeController timeController;

    TCategory(Sector c, String path, int defId) {
        super(c, path, defId);
        timeController = new TimeController(c, path);
        System.out.printf(
                "| build-in time controller %n|    - morning=%s/%s%n|    - day=%s/%s%n|    - evening=%s/%s%n|    - night=%s/%s%n",
                timeController.m, timeController.mId,
                timeController.d, timeController.dId,
                timeController.e, timeController.eID,
                timeController.n, timeController.nID
        );
    }


    @Override
    public BSound getRandomSound(){
        int tId = timeController.getTimeId();
        if(id == tId){
            return super.getRandomSound();
        } else {
            MCategory c = MCategory.valueOf(tId);
            return c.sounds.get(r.nextInt(c.sounds.size()));
        }
    }
}