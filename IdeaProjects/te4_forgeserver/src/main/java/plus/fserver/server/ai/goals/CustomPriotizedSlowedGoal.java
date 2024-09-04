package plus.fserver.server.ai.goals;

import net.minecraft.entity.ai.*;
import org.jetbrains.annotations.NotNull;


public class CustomPriotizedSlowedGoal implements Comparable<CustomPriotizedSlowedGoal>{
    public final EntityAIBase goal;
    private final short shouldExecuteRate;
    private final short tickRate;
    private final byte priority;
    private short shouldExecuteCounter;
    private short tickCounter;
    public boolean isrunning;

    public CustomPriotizedSlowedGoal(final int priority, final EntityAIBase goal, final int shouldExecuteRate, final int tickRate) {
        this.shouldExecuteCounter = 1;
        this.tickCounter = 1;
        this.isrunning = false;
        if(priority > 127) {
            this.priority = 127;
        } else {
            this.priority = (byte) priority;
        }
        this.goal = goal;
        this.tickRate          = (short) Math.max(1, tickRate);
        this.shouldExecuteRate = (short) Math.max(1, shouldExecuteRate);
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
        return priority - o.priority;
    }
}