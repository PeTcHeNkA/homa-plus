package plus.regions.worldgaurd;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import plus.music.MCategory;
import plus.music.MusicSession;
import plus.regions.RegionProvider;

import java.util.HashMap;
import java.util.Set;


public class MusicHandler extends FlagValueChangeHandler<Integer> {

    public static final IntegerFlag SOUND_ID_FLAG = new IntegerFlag("emusicid");
    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<MusicHandler> {
        @Override
        public MusicHandler create(Session session) {
            return new MusicHandler(session);
        }
    }


    public static HashMap<String, Integer> cache = new HashMap<>();


    public static class RProvider implements RegionProvider{
        private final RegionQuery query;

        public RProvider(){
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            query = container.createQuery();
        }

        @Override
        public MCategory getCategory(Player player) {
            Integer categoryId = cache.get(player.getName());
            if(categoryId==null){
                LocalPlayer pl = WorldGuardPlugin.inst().wrapPlayer(player);
                Location loc = pl.getLocation();
                int priority = -1;
                MCategory category = MCategory.valueOf(categoryId);
                for(ProtectedRegion r:query.getApplicableRegions(loc).getRegions()){
                    Integer i = r.getFlag(SOUND_ID_FLAG);
                    if(i==null)continue;
                    MCategory checked = MCategory.valueOf(i);

                    if (checked.getPriority() > priority) {
                        category = checked;
                        priority = checked.getPriority();
                    }
                }
                if(category==null)return MCategory.Default;
                return category;
            }
            return MCategory.valueOf(categoryId);
        }
    }

    protected MusicHandler(Session session) {
        super(session, SOUND_ID_FLAG);
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, Integer integer) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, Integer integer, Integer t1, MoveType moveType) {
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, Integer integer, MoveType moveType) {
        return true;
    }


    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

        MCategory category = null;
        int priority = -1;

        for(ProtectedRegion r:entered){
            Integer i = r.getFlag(SOUND_ID_FLAG);
            if(i==null)continue;
            MCategory checked = MCategory.valueOf(i);

            if (checked.getPriority() > priority) {
                category = checked;
                priority = checked.getPriority();
            }
        }

        if(category != null){
            cache.put(player.getName(), category.id);
            if(category.isPriorityEntry()){
                MusicSession.updateFor(player.getName());
            }
        } else {
            cache.put(player.getName(), MCategory.Default.id);

            for(ProtectedRegion r:exited){
                Integer i = r.getFlag(SOUND_ID_FLAG);
                if(i==null)continue;
                MCategory checked = MCategory.valueOf(i);

                if (checked.getPriority() > priority) {
                    category = checked;
                    priority = checked.getPriority();
                }
            }

            if(category != null && category.isPriorityOut()){
                MusicSession.updateFor(player.getName());
            }
        }
        return true;
    }
}