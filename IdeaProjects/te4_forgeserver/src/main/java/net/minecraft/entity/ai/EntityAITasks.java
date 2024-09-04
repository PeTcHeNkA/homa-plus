package net.minecraft.entity.ai;

import java.util.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.api.MeanTicker;
import plus.fserver.server.ai.CustomGoalTypeData;
import plus.fserver.server.ai.goals.CustomPriotizedSlowedGoal;
import plus.fserver.utls.collections.ArrayListEV;
import plus.tson.utl.Te4HashSet;


public class EntityAITasks extends ArrayListEV<CustomPriotizedSlowedGoal>{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final CustomGoalTypeData       goalData = new CustomGoalTypeData();
    private final Set<CustomPriotizedSlowedGoal>    toAdd = new Te4HashSet<>(8),
                                                 toRemove = new Te4HashSet<>(8);

    private int runningFlags = 0, runningNoOverRuleFlags = 0;
    private int avgTickCount, pauseTicks, pauseTickDelay, counter;
    private boolean addedGoals = false, removedGoals = false;
    private long avgTick = 0;
    protected int disabledControlFlags;
    public final MeanTicker ticker;

    @Deprecated
    public EntityAITasks(Profiler profiler){
        this(MeanTicker.EMPTY);
    }


    public EntityAITasks(World world){
        this(world == null || !(world instanceof MeanTicker)?MeanTicker.EMPTY:((MeanTicker)world));
    }


    public EntityAITasks(MeanTicker ticker) {
        super(8);
        this.ticker = ticker;
    }


    public void addTask(int priority, EntityAIBase task) {
        if (this.sizeC() > 100) {
            LOGGER.warn("Tried to add over 100 AI tasks, pausing ai and aborting");
            this.pauseTicks = 500;
            return;
        }

        for (int i = 0, s = size; i < s; ++i) {
            if (this.getC(i).goal == task) return;
        }
        this.toAdd.add(goalData.getPriotizedGoalFor(priority, task));
        this.addedGoals = true;
    }


    public void removeTask(EntityAIBase task) {
        for (final CustomPriotizedSlowedGoal goal : this) {
            if (goal.goal == task) {
                this.resetGoal(goal);
                this.toRemove.add(goal);
                this.removedGoals = true;
            }
        }
    }


    public boolean isControlFlagDisabled(int flag) {
        return (this.disabledControlFlags & flag) > 0;
    }


    public void disableControlFlag(int flag) {
        this.disabledControlFlags |= flag;
    }


    public void enableControlFlag(int flag) {
        this.disabledControlFlags &= ~flag;
    }


    public void setControlFlag(int flag, boolean enable) {
        if (enable) {
            this.enableControlFlag(flag);
        } else {
            this.disableControlFlag(flag);
        }
    }


    private boolean goalContainsDisabledFlag(final CustomPriotizedSlowedGoal goal) {
        return (disabledControlFlags & goal.goal.getMutexBits()) != 0x0;
    }


    private boolean isPreemptedByAll(final CustomPriotizedSlowedGoal goal1) {
        return (this.runningFlags & goal1.goal.getMutexBits()) == 0x0 && (this.runningNoOverRuleFlags & goal1.goal.getMutexBits()) == 0x0;
    }


    private void resetGoal(final CustomPriotizedSlowedGoal goal) {
        if (goal.isrunning && !goal.goal.isInterruptible())
            this.runningNoOverRuleFlags &= ~goal.goal.getMutexBits();
        goal.goal.resetTask();
        goal.isrunning = false;
    }


    public void onUpdateTasks() {
        if (this.pauseTicks-- > 0) return;
        long slowest = 0L;
        ++this.counter;
        this.runningFlags = 0;

        long preTick = System.currentTimeMillis(),
             preTask = (this.pauseTicks = this.pauseTickDelay) >= 5 ? System.nanoTime() : 0;

        for (int i = 0, s = size; i < s; ++i) {
            final CustomPriotizedSlowedGoal cur;
            final EntityAIBase curGoal = (cur = this.getC(i)).goal;

            if (cur.isrunning && (goalContainsDisabledFlag(cur) || (curGoal.isInterruptible() && (runningFlags & curGoal.getMutexBits()) != 0x0) || !cur.shouldContinueExecuting())) {
                this.resetGoal(cur);
            }
            if (this.counter == 1 && !cur.isrunning && !goalContainsDisabledFlag(cur) && isPreemptedByAll(cur)) {
                if(cur.shouldExecute()) {
                    if (!curGoal.isInterruptible()) {
                        this.runningNoOverRuleFlags |= curGoal.getMutexBits();
                    }
                    curGoal.startExecuting();
                    cur.isrunning = true;
                }
            }
            if (cur.isrunning) {
                this.runningFlags |= curGoal.getMutexBits();
                cur.updateTask();
            }
            if (this.pauseTickDelay >= 5) {
                long tempTime = -preTask + (preTask = System.nanoTime());
                if (tempTime <= slowest) continue;
                slowest = tempTime;
            }
        }
        this.avgTick += System.currentTimeMillis() - preTick;
        ++this.avgTickCount;
        if (this.avgTickCount == 20) {
            if (ticker.meanTickTime() > ElcaConfig.MEAN_BALANCE_MOB) {
                if (this.avgTick > 0L) {
                    this.pauseTicks = (int)this.avgTick * 100 / ElcaConfig.MEAN_BALANCE_MOB;
                    if (this.pauseTicks > 200) this.pauseTicks = 200;
                }
                this.pauseTickDelay = this.pauseTicks;
            } else
                this.pauseTickDelay = 0;
            this.avgTickCount = 0;
            this.avgTick = 0L;
        }
        if (this.removedGoals) {
            for (final CustomPriotizedSlowedGoal goal : this.toRemove) {
                this.remove(goal);
            }
            this.sort(CustomPriotizedSlowedGoal::compareTo);
            this.removedGoals = false;
        }
        if (this.addedGoals) {
            this.addAll(this.toAdd);
            this.sort(CustomPriotizedSlowedGoal::compareTo);
            this.addedGoals = false;
        }
        if (this.counter > ElcaConfig.GOAL_TICK_RATE)
            this.counter = 0;
    }
}