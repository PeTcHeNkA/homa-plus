package plus.quest;

import org.bukkit.entity.Player;
import plus.quest.memory.QuestMemory;
import plus.quest.script.quest.listener.CommandListener;
import plus.quest.script.quest.listener.MMKillListener;
import plus.quest.script.quest.listener.RegionListener;
import plus.quest.script.quest.trigger.STrigger;
import plus.quest.script.quest.trigger.Trigger;
import plus.util.FastRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class QuestSession implements AbstractQuestSession {

    private final int cameraUid = FastRandom.r.nextInt(10000, Integer.MAX_VALUE);
    private final EHashMap history;
    private int dungeonQuestId;
    private Quest quest;
    private Player player;
    private final QuestMemory memory;
    private final EHashSet triggers;
    private boolean pastLoaded = false;

    public QuestSession() {
        history = new EHashMap();
        triggers = new EHashSet();
        memory = new QuestMemory();
        dungeonQuestId = -1;
    }


    public QuestSession(String rawData) {
        String[] data = rawData.split("&");
        history = new EHashMap(data[0]);
        int questId = Integer.parseInt(data[1]);
        if(questId != -1) quest = Quest.valueOf(questId);
        triggers = new EHashSet(data[2]);
        memory = new QuestMemory(data[3]);
        if(data.length==4){
            dungeonQuestId = -1;
        } else {
            dungeonQuestId = Integer.parseInt(data[4]);
        }
    }


    public String toString(){
        return history.toString()+"&"+(quest==null?-1:quest.ID)+"&"+triggers.toString()+"&"+memory.toString()+"&"+dungeonQuestId;
    }


    @Override
    public Quest getActiveQuest(){
        return quest;
    }


    @Override
    public boolean attemptTakeQuest(Quest quest){/*return has quest*/
        if(this.quest != null)return true;
        if(quest==null)return false;
        Long time = history.get(quest.getId());
        if(time != null){
            if(quest.repeatTime()>0){
                long nowTime = System.currentTimeMillis();
                if(time+quest.repeatTime()>nowTime){
                    for(int id: quest.needCompleteQuests){
                        if(!history.containsKey(id)){
                            quest.onNeedCompete.runScript(this);
                            return false;
                        }
                    }
                    this.quest = quest;
                    onTakeQuest();
                    return true;
                }
            }
            return false;
        } else {
            for(int id: quest.needCompleteQuests){
                if(!history.containsKey(id)){
                    quest.onNeedCompete.runScript(this);
                    return false;
                }
            }
            this.quest = quest;
            onTakeQuest();
            return true;
        }
    }


    @Override
    public void completeActiveQuest(){
        if(quest != null){
            history.put(quest.getId(), System.currentTimeMillis());
        }
        RegionListener.clearFor(player.getName());
        CommandListener.clearFor(player.getName());
        MMKillListener.clearFor(player.getName());
        triggers.clear();
        memory.clear();
        quest = null;
    }


    @Override
    public void onTakeQuest(){
        quest.onTake(this);
    }


    @Override
    public boolean loadTrigger(STrigger trigger){
        if(pastLoaded){
            return triggers.add(trigger.toString());
        } else {
            triggers.add(trigger.toString());
            return false;
        }
    }


    @Override
    public boolean unloadTrigger(STrigger trigger){
        if(triggers.remove(trigger.toString())) {
            if (triggers.isEmpty()) {
                if(quest != null){
                    history.put(quest.getId(), System.currentTimeMillis());
                    memory.clear();
                    quest = null;
                }
            }
            return true;
        } else return false;
    }


    @Override
    public Player getPlayer(){
        return player;
    }


    @Override
    public void setPlayer(Player player){
        pastLoaded = false;
        this.player = player;
    }


    @Override
    public int getCameraUid(){
        return cameraUid;
    }


    @Override
    public QuestMemory getMemory(){
        return memory;
    }


    @Override
    public void onPostLoad(){
        if(!pastLoaded)
        pastLoaded = true;
        for(String t:triggers) {
            STrigger trigger = Trigger.getOf(t);
            if(trigger==null)continue;
            trigger.onLoad(this);
        }
    }

    @Override
    public void setDungeonQuest(int id) {
        dungeonQuestId = id;
    }

    @Override
    public int getDungeonQuest() {
        return dungeonQuestId;
    }


    @Override
    public HashMap<Integer, Long> getQuestHistory() {
        return history;
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


class EHashMap extends HashMap<Integer, Long> {

    public EHashMap(){}

    public EHashMap(String rawData){
        if(rawData==null || rawData.isEmpty())return;
        for(String s:rawData.split(",")){
            String[] s2 = s.split(":");
            super.put(Integer.parseInt(s2[0]), Long.parseLong(s2[1])*100000);
        }
    }


    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        for(Integer key: super.keySet()){
            s.append(key.toString()).append(":").append((super.get(key)/100000)).append(",");
        }
        return s.toString();
    }
}


class EHashSet extends HashSet<String> {

    public EHashSet(){}

    public EHashSet(String rawData){
        String[] strings = rawData.split(",");
        Collections.addAll(this, strings);
    }


    public String toString(){
        StringBuilder result = new StringBuilder();
        for(String str:this){
            result.append(str).append(",");
        }
        return result.toString();
    }
}