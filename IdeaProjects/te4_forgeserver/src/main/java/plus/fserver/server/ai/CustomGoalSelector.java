package plus.fserver.server.ai;

import net.minecraft.profiler.*;
import net.minecraft.entity.ai.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.api.MeanTicker;
import plus.fserver.patch.PEntityAITasks;
import plus.fserver.server.ai.goals.CustomPriotizedSlowedGoal;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.ElcaConfig;
import plus.tson.utl.Te4HashSet;

import java.util.*;


@Deprecated
public final class CustomGoalSelector extends EntityAITasks implements PEntityAITasks{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final CustomGoalTypeData goalData = new CustomGoalTypeData();
    public static final int SHOULD_EXECUTE_INTERVAL;
    public static int timeForAI;
    private final Set<CustomPriotizedSlowedGoal>  toAdd    = new Te4HashSet<>(8),
                                                  toRemove = new Te4HashSet<>(8);
    private final List<CustomPriotizedSlowedGoal> goals    = new ArrayList<>();
    private int runningFlags = 0, runningNoOverRuleFlags = 0;
    private boolean addedGoals = false, removedGoals = false;
    private long avgTick = 0, preTick = 0, tempTime = 0, slowest = 0, preTask;
    private int avgTickCount, pauseTicks, pauseTickDelay, counter;
    private final MeanTicker ticker;

    public CustomGoalSelector(final EntityAITasks old, Profiler profiler, MeanTicker ticker) {
        super(profiler);
        this.ticker = ticker;
        this.avgTickCount = 1;
        this.pauseTicks = 0;
        this.pauseTickDelay = 0;
        importFrom(old);
        setTaskEntries(old.taskEntries);
        this.counter = FastRandom.INST.nextInt(CustomGoalSelector.SHOULD_EXECUTE_INTERVAL + 1);
    }


    private void importFrom(final EntityAITasks selector) {
        if (selector == null) return;

        for (final EntityAITasks.EntityAITaskEntry task : selector.taskEntries) {
            final CustomPriotizedSlowedGoal goal = goalData.getPriotizedGoalFor(task.priority, task.action, this);
            goal.isrunning = task.using;
            if (task.using && !task.action.isInterruptible()) {
                this.runningNoOverRuleFlags |= task.action.getMutexBits();
            }
            this.goals.add(goal);
        }
        this.goals.sort(Comparator.comparingInt(CustomPriotizedSlowedGoal::getPriority));
        this.setDisabledControlFlags(((PEntityAITasks)selector).getDisabledControlFlags());
    }


    @Override
    public void addTask(final int priority, @NotNull final EntityAIBase task) {
        if (this.goals.size() > 100) {
            LOGGER.warn("Tried to add over 100 AI tasks, pausing ai and aborting");
            this.pauseTicks = 500;
            return;
        }
        for (final CustomPriotizedSlowedGoal goal : this.goals) {
            if (goal.goal == task) return;
        }
        this.toAdd.add(goalData.getPriotizedGoalFor(priority, task, this));
        this.addedGoals = true;
        super.addTask(priority, task);
    }


    @Override
    public void removeTask(@NotNull final EntityAIBase task) {
        for (final CustomPriotizedSlowedGoal goal : this.goals) {
            if (goal.goal == task) {
                this.resetGoal(goal);
                this.toRemove.add(goal);
                this.removedGoals = true;
            }
        }
        super.removeTask(task);
    }


    private boolean goalContainsDisabledFlag(final CustomPriotizedSlowedGoal goal) {
        return (this.getDisabledControlFlags() & goal.goal.getMutexBits()) != 0x0;
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

        this.pauseTicks = this.pauseTickDelay;
        this.slowest = 0L;
        ++this.counter;
        this.runningFlags = 0;
        this.preTick = System.currentTimeMillis();

        this.preTask = this.pauseTickDelay >= 5?System.nanoTime():0;

        for (final CustomPriotizedSlowedGoal currentGoal : this.goals) {
            if (currentGoal.isRunning() && (this.goalContainsDisabledFlag(currentGoal) || (currentGoal.goal.isInterruptible() && (this.runningFlags & currentGoal.goal.getMutexBits()) != 0x0) || !currentGoal.shouldContinueExecuting())) {
                this.resetGoal(currentGoal);
            }
            if (this.counter == 1 && !currentGoal.isRunning() && !this.goalContainsDisabledFlag(currentGoal) && this.isPreemptedByAll(currentGoal) && currentGoal.shouldExecute()) {
                if (!currentGoal.goal.isInterruptible()) {
                    this.runningNoOverRuleFlags |= currentGoal.goal.getMutexBits();
                }
                currentGoal.goal.startExecuting();
                currentGoal.isrunning = true;
            }
            if (currentGoal.isRunning()) {
                this.runningFlags |= currentGoal.goal.getMutexBits();
                currentGoal.updateTask();
            }
            if (this.pauseTickDelay >= 5) {
                this.tempTime = - this.preTask + (this.preTask = System.nanoTime());
                if (this.tempTime <= this.slowest) continue;
                this.slowest = this.tempTime;
                EntityAIBase slowestGoal = currentGoal.goal;
            }
        }
        this.avgTick += System.currentTimeMillis() - this.preTick;
        ++this.avgTickCount;
        if (this.avgTickCount == 20) {
            if (ticker.meanTickTime() > ElcaConfig.MEAN_BALANCE_MOB) {
                if (this.avgTick > 0L) {
                    this.pauseTicks = (int)this.avgTick * 100 / CustomGoalSelector.timeForAI;
                    if (this.pauseTicks > 20) this.pauseTicks = 200;
                }
                this.pauseTickDelay = this.pauseTicks;
            } else
                this.pauseTickDelay = 0;
            this.avgTickCount = 0;
            this.avgTick = 0L;
        }
        if (this.removedGoals) {
            for (final CustomPriotizedSlowedGoal goal : this.toRemove) {
                this.goals.remove(goal);
            }
            this.goals.sort(Comparator.comparingInt(CustomPriotizedSlowedGoal::getPriority));
            this.removedGoals = false;
        }
        if (this.addedGoals) {
            this.goals.addAll(this.toAdd);
            this.goals.sort(Comparator.comparingInt(CustomPriotizedSlowedGoal::getPriority));
            this.addedGoals = false;
        }
        if (this.counter > CustomGoalSelector.SHOULD_EXECUTE_INTERVAL)
            this.counter = 0;
    }


    static {
        CustomGoalSelector.timeForAI = ElcaConfig.MEAN_BALANCE_MOB * 12;
        SHOULD_EXECUTE_INTERVAL = ElcaConfig.GOAL_TICK_RATE;
    }
}