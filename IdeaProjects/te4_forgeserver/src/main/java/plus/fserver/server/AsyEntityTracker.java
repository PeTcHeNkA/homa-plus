package plus.fserver.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import plus.fserver.utls.collections.ArrayListE10;


public class AsyEntityTracker extends EntityTracker {
    private ArrayListE10<Runnable> tasks = new ArrayListE10<>();
    private ArrayListE10<Runnable> swap  = new ArrayListE10<>();

    public AsyEntityTracker(WorldServer world) {
        super(world);
    }


    @Override
    public AsyEntityTracker toAsync(){
        return this;
    }


    @Override
    public EntityTracker toSync(){
        EntityTracker tracker = new EntityTracker(world);
        tracker.maxTrackingDistanceThreshold = maxTrackingDistanceThreshold;
        return tracker;
    }


    @Override
    public boolean isAsync() {
        return true;
    }


    public void runTasks(){
        ArrayListE10<Runnable> swap = this.swap;
        this.swap = tasks;
        this.tasks = swap;

        this.swap.forEach(Runnable::run);
        this.swap.clear();
    }


    @Override
    public void tick() {
        runTasks();
        super.tick();
    }


    @Override
    public void onTrack(EntityTrackerEntry entry) {
        tasks.addF((Runnable) () -> entry.updatePlayerEntities(world.playerEntities));
    }


    @Override
    public void untrack(Entity entityIn) {
        tasks.addF((Runnable) () -> AsyEntityTracker.super.untrack(entityIn));
    }


    @Override
    public void updateVisibility(EntityPlayerMP player) {
        tasks.addF((Runnable) () -> AsyEntityTracker.super.updateVisibility(player));
    }
}