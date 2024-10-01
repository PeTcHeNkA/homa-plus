package net.minecraftforge.event.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import javax.annotation.Nullable;
import java.util.List;


public class GetCollisionBoxesEvent extends WorldEvent {
    private Entity entity;
    private AxisAlignedBB aabb;
    private List<AxisAlignedBB> collisionBoxesList;

    public GetCollisionBoxesEvent(World world, @Nullable Entity entity, AxisAlignedBB aabb, List<AxisAlignedBB> collisionBoxesList) {
        super(world);
        this.entity = entity;
        this.aabb = aabb;
        this.collisionBoxesList = collisionBoxesList;
    }


    public GetCollisionBoxesEvent(World world) {
        super(world);
    }


    public GetCollisionBoxesEvent init(@Nullable Entity entity, AxisAlignedBB aabb, List<AxisAlignedBB> collisionBoxesList){
        this.entity = entity;
        this.aabb = aabb;
        this.collisionBoxesList = collisionBoxesList;
        return this;
    }


    public void reset(){
        this.entity = null;this.aabb = null;this.collisionBoxesList = null;
    }


    public Entity getEntity() {
        return entity;
    }


    public AxisAlignedBB getAabb() {
        return aabb;
    }


    public List<AxisAlignedBB> getCollisionBoxesList() {
        return collisionBoxesList;
    }


    //Elca-Forge event system
    //Start generated code
    private static ListenerList LISTENER_LIST;
    protected void setup() {
        super.setup();
        if (LISTENER_LIST == null)
            LISTENER_LIST = new ListenerList(super.getListenerList());
    }
    public ListenerList getListenerList() {
        return LISTENER_LIST;
    }
    //End generated code
}
