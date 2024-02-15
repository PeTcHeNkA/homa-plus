package net.minecraftforge.event.entity;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;


public class EntityEvent extends Event {
    private Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

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
    }


    public static class CanUpdate extends EntityEvent {
        private boolean canUpdate = false;
        public CanUpdate(Entity entity) {
            super(entity);
        }

        public boolean getCanUpdate() {
            return canUpdate;
        }

        public void setCanUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
        }
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
    }
}