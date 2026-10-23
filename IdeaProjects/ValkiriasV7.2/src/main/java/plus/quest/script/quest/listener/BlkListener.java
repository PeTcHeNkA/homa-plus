package plus.quest.script.quest.listener;

import org.bukkit.Location;
import plus.quest.AbstractQuestSession;
import plus.quest.script.quest.trigger.STrigger;


public interface BlkListener extends STrigger {

    Location[] getLocations();

    void onClick(AbstractQuestSession session);
}