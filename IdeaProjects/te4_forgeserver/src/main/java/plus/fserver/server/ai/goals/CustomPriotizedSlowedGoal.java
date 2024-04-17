package plus.fserver.server.ai.goals;

import net.minecraft.entity.ai.*;
import org.jetbrains.annotations.NotNull;


public class CustomPriotizedSlowedGoal implements Comparable<CustomPriotizedSlowedGoal>{
    public final EntityAIBase goal;
    private final int shouldExecuteRate;
    private final int tickRate;
    private final int priority;
    private int shouldExecuteCounter;
    private int tickCounter;
    public boolean isrunning;

    public CustomPriotizedSlowedGoal(final int priority, final EntityAIBase goal, final int shouldExecuteRate, final int tickRate) {
        this.shouldExecuteCounter = 1;
        this.tickCounter = 1;
        this.isrunning = false;
        this.priority = priority;
        this.goal = goal;
        this.tickRate = Math.max(1, tickRate);
        this.shouldExecuteRate = Math.max(1, shouldExecuteRate);
    }
    
    public int getPriority() {
        return this.priority;
    }


    public boolean shouldExecute() {
        if (this.shouldExecuteCounter == this.shouldExecuteRate) {
            this.shouldExecuteCounter = 1;
            return this.goal.shouldExecute();
        }
        ++this.shouldExecuteCounter;
        return false;
    }


    public boolean shouldContinueExecuting() {
        return this.tickCounter != this.tickRate || this.goal.shouldContinueExecuting();
    }


    public void updateTask() {
        if (tickCounter++ == this.tickRate) {
            tickCounter = 1;
            goal.updateTask();
        }
    }


    public boolean isRunning() {
        return this.isrunning;
    }


    @Override
    public int compareTo(@NotNull CustomPriotizedSlowedGoal o) {
        return Integer.compare(priority, o.priority);
    }
}