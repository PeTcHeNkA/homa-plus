package plus.quest.script.quest.listener;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.quest.Quest;

import java.util.HashMap;
import java.util.Set;


public class RegionListener extends FlagValueChangeHandler<Integer> {

    private static final HashMap<String, HashMap<Integer, RgnListener>> cache = new HashMap<>();
    public static final IntegerFlag QUEST_TRIGGER_ID = new IntegerFlag("quest-trigger-id");
    public static final IntegerFlag QUEST_TAKE_ID = new IntegerFlag("quest-take-id");
    public static final RegionListener.Factory1 FACTORY1 = new Factory1();
    public static final RegionListener.Factory2 FACTORY2 = new Factory2();

    public static class Factory1 extends Handler.Factory<RegionListener> {
        @Override
        public RegionListener create(Session session) {
            return new RegionListener(session);
        }
    }


    public static class Factory2 extends Handler.Factory<AutoTakeQuest> {
        @Override
        public AutoTakeQuest create(Session session) {
            return new AutoTakeQuest(session);
        }
    }


    protected RegionListener(Session session) {
        super(session, QUEST_TRIGGER_ID);
    }


    public static void define(){
        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(FACTORY1, null);
        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(FACTORY2, null);
    }


    public static void addTrigger(String player, int triggerID, RgnListener listener){
        HashMap<Integer, RgnListener> listeners = cache.computeIfAbsent(player, k -> new HashMap<>());
        listeners.put(triggerID, listener);
    }


    public static void clearFor(String player){
        cache.remove(player);
    }


    public static void onEnter(String player, int region){
        HashMap<Integer, RgnListener> listeners = cache.get(player);
        if(listeners==null)return;
        RgnListener listener = listeners.get(region);
        if(listener==null)return;

        AbstractQuestSession session = Main.floader.getValkiria(player).getQuestSession();
        if(listener.onEnter(session)){
            listener.unload(session);
        }
    }


    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, Integer s) {}


    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, Integer s, Integer t1, MoveType moveType) {
        return true;
    }


    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, Integer s, MoveType moveType) {
        return true;
    }


    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

        for(ProtectedRegion r:entered){
            Integer s = r.getFlag(QUEST_TRIGGER_ID);
            if(s!=null) onEnter(player.getName(), s);

        }
        return true;
    }
}

class AutoTakeQuest extends FlagValueChangeHandler<Integer>{

    protected AutoTakeQuest(Session session) {
        super(session, RegionListener.QUEST_TAKE_ID);
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
        for (ProtectedRegion r : entered) {
            Integer s = r.getFlag(RegionListener.QUEST_TAKE_ID);
            if (s != null) {
                if (Main.floader.getValkiria(player.getName())
                        .getQuestSession().attemptTakeQuest(Quest.valueOf(s))) break;
            }
        }
        return true;
    }
}