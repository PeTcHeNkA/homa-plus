package plus.fserver.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import plus.fserver.utls.collections.ArrayListE10;


public class AsyEntityTracker extends EntityTracker {
//    private ArrayListE10<Runnable> tasks = new ArrayListE10<>();
//    private ArrayListE10<Runnable> swap  = new ArrayListE10<>();

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


//    public void runTasks(){
//        ArrayListE10<Runnable> tasks;
//        synchronized (this) {
//            tasks = this.tasks;
//            this.tasks = this.swap;
//            this.swap = tasks;
//        }
//        tasks.forEach(Runnable::run);
//        tasks.clear();
//    }


    @Override
    public synchronized void tick() {
        //runTasks();
        super.tick();
    }


    @Override
    public synchronized void onTrack(EntityTrackerEntry entry) {
        super.onTrack(entry);
        //tasks.addF((Runnable) () -> entry.updatePlayerEntities(world.playerEntities));
    }


    @Override
    public synchronized void untrack(Entity entityIn) {
        super.untrack(entityIn);
        //tasks.addF((Runnable) () -> AsyEntityTracker.super.untrack(entityIn));
    }


    @Override
    public synchronized void updateVisibility(EntityPlayerMP player) {
        super.updateVisibility(player);
        //tasks.addF((Runnable) () -> AsyEntityTracker.super.updateVisibility(player));
    }
}