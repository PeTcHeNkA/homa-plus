package plus.fserver.server.ai;

import com.google.common.collect.*;
import java.util.*;
import net.minecraft.entity.ai.*;
import org.jetbrains.annotations.NotNull;
import plus.fserver.patch.PEntityAITasks;


public class VanillaGoalSelectorMeasurement extends EntityAITasks implements PEntityAITasks {
    public Set<EntityAITasks.EntityAITaskEntry> taskEntries;
    private final Set<EntityAITasks.EntityAITaskEntry> executingTaskEntries;
    private int tickCount;
    private int tickRate;
    
    public VanillaGoalSelectorMeasurement(final EntityAITasks old) {
        super(((PEntityAITasks)old).getProfiler());
        this.taskEntries = Sets.newLinkedHashSet();
        this.executingTaskEntries = Sets.newLinkedHashSet();
        this.tickRate = 3;
        //this.profiler = ((PEntityAITasks)old).getProfiler();
        setTaskEntries(old.taskEntries);
        this.taskEntries = old.taskEntries;
        for (final EntityAITasks.EntityAITaskEntry en : old.taskEntries) {
            en.using = false;
        }
        setDisabledControlFlags(((PEntityAITasks)old).getDisabledControlFlags());
    }

    
    public void removeTask(@NotNull final EntityAIBase task) {
        final Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.taskEntries.iterator();
        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entry = iterator.next();
            final EntityAIBase entityaibase = entry.action;
            if (entityaibase == task) {
                if (entry.using) {
                    entry.using = false;
                    entry.action.resetTask();
                    this.executingTaskEntries.remove(entry);
                }
                iterator.remove();
            }
        }
    }


    public void onUpdateTasks() {
        if (this.tickCount++ % this.tickRate == 0) {
            for (final EntityAITasks.EntityAITaskEntry entry : this.taskEntries) {
                if (entry.using) {
                    if (this.canUse(entry) && this.canContinue(entry)) {
                        continue;
                    }
                    entry.using = false;
                    entry.action.resetTask();
                    this.executingTaskEntries.remove(entry);
                }
                else {
                    if (!this.canUse(entry) || !entry.action.shouldExecute()) {
                        continue;
                    }
                    entry.using = true;
                    entry.action.startExecuting();
                    this.executingTaskEntries.add(entry);
                }
            }
        } else {
            final Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.executingTaskEntries.iterator();
            while (iterator.hasNext()) {
                final EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry2 = iterator.next();
                if (!this.canContinue(entityaitasks$entityaitaskentry2)) {
                    entityaitasks$entityaitaskentry2.using = false;
                    entityaitasks$entityaitaskentry2.action.resetTask();
                    iterator.remove();
                }
            }
        }
        if (!this.executingTaskEntries.isEmpty()) {
            for (final EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry3 : this.executingTaskEntries) {
                entityaitasks$entityaitaskentry3.action.updateTask();
            }
        }
    }


    private boolean canContinue(final EntityAITasks.EntityAITaskEntry taskEntry) {
        return taskEntry.action.shouldContinueExecuting();
    }


//    private boolean canUse(final EntityAITasks.EntityAITaskEntry taskEntry, final int a) {
//        long d = System.nanoTime();
//        final boolean res = this.canUse(taskEntry, 1);
//        d = System.nanoTime() - d;
//        VanillaGoalSelectorMeasurement.canUseTime += d;
//        ++VanillaGoalSelectorMeasurement.canUseCount;
//        if (VanillaGoalSelectorMeasurement.canUseCount >= 100000L) {
//            Performant.LOGGER.warn("VANILLA:CANUSEAVG:" + VanillaGoalSelectorMeasurement.canUseTime);
//            VanillaGoalSelectorMeasurement.canUseCount = 0L;
//            VanillaGoalSelectorMeasurement.canUseTime = 0L;
//        }
//        return res;
//    }


    private boolean canUse(final EntityAITasks.EntityAITaskEntry taskEntry) {
        if (this.executingTaskEntries.isEmpty()) {
            return true;
        }
        if (this.isControlFlagDisabled(taskEntry.action.getMutexBits())) {
            return false;
        }
        for (final EntityAITasks.EntityAITaskEntry entry : this.executingTaskEntries) {
            if (entry != taskEntry) {
                if (taskEntry.priority >= entry.priority) {
                    if (!this.areTasksCompatible(taskEntry, entry)) {
                        return false;
                    }
                } else {
                    if (!entry.action.isInterruptible()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private boolean areTasksCompatible(final EntityAITasks.EntityAITaskEntry taskEntry1, final EntityAITasks.EntityAITaskEntry taskEntry2) {
        return (taskEntry1.action.getMutexBits() & taskEntry2.action.getMutexBits()) == 0x0;
    }


    public boolean isControlFlagDisabled(final int p_188528_1_) {
        return (getDisabledControlFlags() & p_188528_1_) > 0;
    }
}
