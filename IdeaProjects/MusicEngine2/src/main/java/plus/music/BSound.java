package plus.music;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import plus.provider.SoundProvider;
import plus.utils.config.Sector;


public abstract class BSound {

    protected final BreakPoints points;
    protected final String category;
    protected final String sound;
    protected final MCategory mCategory;
    protected final int duration;
    protected final RFloat pith;
    protected final int allowedWaiting;

    public static BSound build(Sector c, String path){
        if(c.getOrSetBoolean(path+".is_background", true)){
            return new BlackGroundSound(c, path);
        } else {
            return new NormalSound(c, path);
        }
    }


    protected BSound(Sector c, String path){
        category = parseCategory(c.getOrSetString(path+".sound_category", "master")).name();
        sound = c.getOrSetString(path+".sound", "your sound");
        mCategory = MCategory.valueOf(c.getOrSetInt(path+".category_id",0));
        duration = BreakPoints.parseTime(c.getOrSetString(path+".duration", "1:00"));
        pith = RFloat.build(c.getOrSetString(path+".pith", "*-*"));

        points = BreakPoints.build(this, c.getOrSetString(path+".break_points", "-1"));

        allowedWaiting = c.getOrSetInt(path+".waiting_out", 0);
    }


    private static SoundCategory parseCategory(String data){
        if(data.equalsIgnoreCase("ambient")){
            return SoundCategory.AMBIENT;
        } else if(data.equalsIgnoreCase("blocks")){
            return SoundCategory.BLOCKS;
        } else if (data.equalsIgnoreCase("hostile")){
            return SoundCategory.HOSTILE;
        } else if (data.equalsIgnoreCase("music")){
            return SoundCategory.MUSIC;
        } else if(data.equalsIgnoreCase("neutral")){
            return SoundCategory.NEUTRAL;
        } else if(data.equalsIgnoreCase("players")){
            return SoundCategory.PLAYERS;
        } else if(data.equalsIgnoreCase("records")){
            return SoundCategory.RECORDS;
        }
        return SoundCategory.MASTER;
    }


    public abstract void play(Player p);


    public MCategory getCategory(){
        return mCategory;
    }


    public BreakPoints getBreakPoints(){
        return points;
    }


    public int getDuration(){
        return duration;
    }


    public String getSound(){
        return sound;
    }


    public void stopSound(Player player){
        SoundProvider.G.StopSound(player, sound, category);
    }


    public int getAllowedWaiting(){
        return allowedWaiting;
    }
}


class BlackGroundSound extends BSound{

    public BlackGroundSound(Sector c, String path) {
        super(c, path);
    }

    @Override
    public void play(Player p) {
        SoundProvider.G.playBgSound(p, sound, category, pith.Float());
    }
}


class NormalSound extends BSound{

    private final RFloat volume;

    public NormalSound(Sector c, String path) {
        super(c, path);
        volume = RFloat.build(c.getOrSetString(path+".volume", "*-*"));
    }

    @Override
    public void play(Player p) {
        SoundProvider.G.playNormalSound(p, p.getLocation(), sound, category, volume.Float(), pith.Float());
    }
}