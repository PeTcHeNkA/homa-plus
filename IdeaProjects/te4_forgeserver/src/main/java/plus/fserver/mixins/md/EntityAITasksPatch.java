package plus.fserver.mixins.md;

import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.patch.PEntityAITasks;
import java.util.Set;


@Mixin(EntityAITasks.class)
public class EntityAITasksPatch implements PEntityAITasks {
    @Shadow @Final private Profiler profiler;
    @Shadow @Final @Mutable public Set<EntityAITasks.EntityAITaskEntry> taskEntries;
    @Shadow private int disabledControlFlags;

    @Override
    public Profiler getProfiler() {
        return profiler;
    }


    @Override
    public void setTaskEntries(Set<EntityAITasks.EntityAITaskEntry> taskEntries) {
        this.taskEntries = taskEntries;
    }


    @Override
    public int getDisabledControlFlags() {
        return disabledControlFlags;
    }


    @Override
    public void setDisabledControlFlags(int disabledControlFlags) {
        this.disabledControlFlags = disabledControlFlags;
    }
}