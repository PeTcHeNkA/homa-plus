package plus.quest;

import org.bukkit.entity.Player;
import plus.quest.memory.QuestMemory;
import plus.quest.script.quest.trigger.STrigger;

import java.util.HashMap;

public interface AbstractQuestSession {
    Quest getActiveQuest();

    boolean attemptTakeQuest(Quest quest);

    void completeActiveQuest();

    void onTakeQuest();

    boolean loadTrigger(STrigger trigger);

    boolean unloadTrigger(STrigger trigger);

    Player getPlayer();

    void setPlayer(Player player);

    int getCameraUid();

    QuestMemory getMemory();

    void onPostLoad();


    boolean inHistory(int questId);


    long getQuestTime(int questId);


    void setDungeonQuest(int id);


    int getDungeonQuest();


    HashMap<Integer, Long> getQuestHistory();
}
