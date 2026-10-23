package plus.quest.script.scene;

import org.bukkit.entity.Player;
import plus.util.cfg.Sector;
import plus.quest.motion.BasicPos;
import plus.quest.motion.TpMotion;
import plus.versions.SoundPlayer;


public class ScriptPlayMusic implements Script{

    private final boolean isRelative;
    private final BasicPos musicLocation;
    public final String Music;
    public final String Category;
    public final float volume;
    public final float pith;

    public ScriptPlayMusic(Sector c, String path){
        isRelative = c.getOrSetBoolean(path+".relative", true);
        musicLocation = TpMotion.build(c.getOrSetString(path+".location", "0;0;0"));
        if(SoundPlayer.G==null) SoundPlayer.loadForCurrentBukkitVersion();

        Music = c.getOrSetString(path+".music", "your_music_key");
        Category = c.getOrSetString(path+".category", "master");
        volume = (float) c.getOrSetDouble(path+".volume", 1.0f);
        pith = (float) c.getOrSetDouble(path+".volume", 1.0f);
    }


    @Override
    public void run(Player player, int uid, BasicPos pos) {
        BasicPos mPos;
        if(isRelative){
            mPos = new BasicPos(
                    pos.X+musicLocation.X,
                    pos.Y+musicLocation.Y,
                    pos.Z+musicLocation.Z
            );
        } else mPos = musicLocation;
        SoundPlayer.G.play(this, player, mPos);
    }
}