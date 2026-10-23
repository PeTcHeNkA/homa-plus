package plus.quest;

import org.bukkit.entity.Player;
import plus.quest.memory.QuestMemory;
import plus.quest.script.dungeon.SimplePartyQuest;
import plus.quest.script.party.PartySelector;
import plus.quest.script.quest.listener.CommandListener;
import plus.quest.script.quest.listener.MMKillListener;
import plus.quest.script.quest.listener.RegionListener;
import plus.quest.script.quest.trigger.STrigger;
import plus.util.FastRandom;

import java.util.HashMap;


public class DungeonQuestSession implements AbstractQuestSession{

    private final int cameraUid = FastRandom.r.nextInt(10000, Integer.MAX_VALUE);
    private final EHashMap history;
    private final String rawQuestId;
    private final String rawTriggers;
    private final QuestMemory memory;
    private final EHashSet triggers = new EHashSet();
    private Quest quest;
    private Player player;
    private boolean pastLoaded = false;
    private int dungeonQuestId;
    private PartySelector selector;


    public DungeonQuestSession(String rawData){
        String[] data = rawData.split("&");
        history = new EHashMap(data[0]);
        rawQuestId = data[1];
        rawTriggers = data[2];
        memory = new QuestMemory(data[3]);

        if(data.length==4){
            dungeonQuestId = -1;
        } else {
            int questId = Integer.parseInt(data[4]);
            if(questId != -1) quest = Quest.valueOf(questId);
            dungeonQuestId = questId;
        }
    }


    public String toString(){
        return history.toString()+"&"+rawQuestId+"&"+rawTriggers+"&"+memory.toString()+"&"+dungeonQuestId;
    }


    @Override
    public Quest getActiveQuest() {
        return quest;
    }


    @Override
    public boolean attemptTakeQuest(Quest quest) {
        this.quest = quest;
        onTakeQuest();
        return true;
    }


    @Override
    public void completeActiveQuest(){
        if(this.quest instanceof SimplePartyQuest singleQuest){
            singleQuest.unLock();
        }
        //history.put(quest.getId(), System.currentTimeMillis());
        dungeonQuestId = -1;
        RegionListener.clearFor(player.getName());
        CommandListener.clearFor(player.getName());
        MMKillListener.clearFor(player.getName());
        triggers.clear();
        quest = null;
    }


    @Override
    public void onTakeQuest(){
        quest.onTake(this);
    }


    @Override
    public boolean loadTrigger(STrigger trigger){
        return triggers.add(trigger.toString());
    }


    @Override
    public boolean unloadTrigger(STrigger trigger){
        if(triggers.remove(trigger.toString())) {
            if (triggers.isEmpty()) {
                quest = null;
                memory.clear();
            }
            return true;
        } else return false;
    }


    @Override
    public Player getPlayer() {
        return player;
    }


    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }


    @Override
    public int getCameraUid() {
        return cameraUid;
    }


    @Override
    public QuestMemory getMemory() {
        return memory;
    }


    @Override
    public void onPostLoad(){
        if(!pastLoaded) {
            pastLoaded = true;
            if(quest != null) quest.onTake(this);
        }
    }


    @Override
    public void setDungeonQuest(int id) {}


    @Override
    public int getDungeonQuest() {
        return dungeonQuestId;
    }


    @Override
    public HashMap<Integer, Long> getQuestHistory() {
        return history;
    }


    public PartySelector getSelector(){
        return selector;
    }


    public void setSelector(PartySelector selector){
        this.selector = selector;
    }


    @Override
    public boolean inHistory(int questId){
        return history.containsKey(questId);
    }


    @Override
    public long getQuestTime(int questId){
        return history.getOrDefault(questId, -1L);
    }
}