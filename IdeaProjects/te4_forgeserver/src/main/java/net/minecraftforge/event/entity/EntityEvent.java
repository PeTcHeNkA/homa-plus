package net.minecraftforge.event.entity;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;


public class EntityEvent extends Event {
    private Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }


    public EntityEvent(){}


    public Entity getEntity() {
        return entity;
    }


    public void setEntity(Entity entity) {
        this.entity = entity;
    }


    public static class EntityConstructing extends EntityEvent {
        public EntityConstructing(Entity entity) {
            super(entity);
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


    public static class CanUpdate extends EntityEvent {
        private boolean canUpdate = false;
        private IEventListener[] listeners;
        public CanUpdate(Entity entity) {
            super(entity);
        }

        public CanUpdate forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public boolean post(Entity entity){
            canUpdate = false;
            setEntity(entity);
            for (IEventListener listener:listeners)listener.invoke(this);
            return canUpdate;
        }


        public boolean getCanUpdate() {
            return canUpdate;
        }


        public void setCanUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
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


    public static class EnteringChunk extends EntityEvent {
        private int newChunkX;
        private int newChunkZ;
        private int oldChunkX;
        private int oldChunkZ;

        public EnteringChunk(Entity entity, int newChunkX, int newChunkZ, int oldChunkX, int oldChunkZ) {
            super(entity);
            this.setNewChunkX(newChunkX);
            this.setNewChunkZ(newChunkZ);
            this.setOldChunkX(oldChunkX);
            this.setOldChunkZ(oldChunkZ);
        }

        public EnteringChunk initEntering(Entity entity, int newChunkX, int newChunkZ, int oldChunkX, int oldChunkZ){
            super.setEntity(entity);
            this.setNewChunkX(newChunkX);
            this.setNewChunkZ(newChunkZ);
            this.setOldChunkX(oldChunkX);
            this.setOldChunkZ(oldChunkZ);
            return this;
        }

        public int getNewChunkX() { return newChunkX; }
        public void setNewChunkX(int newChunkX) { this.newChunkX = newChunkX; }
        public int getNewChunkZ() { return newChunkZ; }
        public void setNewChunkZ(int newChunkZ) { this.newChunkZ = newChunkZ; }
        public int getOldChunkX() { return oldChunkX; }
        public void setOldChunkX(int oldChunkX) { this.oldChunkX = oldChunkX; }
        public int getOldChunkZ() { return oldChunkZ; }
        public void setOldChunkZ(int oldChunkZ) { this.oldChunkZ = oldChunkZ; }


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