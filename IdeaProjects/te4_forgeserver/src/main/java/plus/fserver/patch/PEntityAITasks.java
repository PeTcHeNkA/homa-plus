package plus.fserver.patch;

import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.profiler.Profiler;
import java.util.Set;


public interface PEntityAITasks {
    default Profiler getProfiler(){throw new UnsupportedOperationException();};
    default void setTaskEntries(Set<EntityAITasks.EntityAITaskEntry> taskEntries){};
    default int getDisabledControlFlags(){throw new UnsupportedOperationException();}
    default void setDisabledControlFlags(int i){};
}
