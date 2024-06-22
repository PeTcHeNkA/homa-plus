package net.minecraftforge.event.entity.living;

import javax.annotation.Nullable;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;


public class LivingSpawnEvent extends LivingEvent {
    private World world;
    private float x;
    private float y;
    private float z;

    public LivingSpawnEvent(EntityLiving entity, World world, float x, float y, float z) {
        super(entity);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public LivingSpawnEvent(){
        super(null);
    }


    public void initLivingSpawnEvent(EntityLiving entity, World world, float x, float y, float z){
        initLiving(entity);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        setResult(Result.DEFAULT);
    }

    public World getWorld() { return world; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }


    @HasResult
    public static class CheckSpawn extends LivingSpawnEvent {
        private boolean isSpawner; // TODO: remove in 1.13
        @Nullable
        private MobSpawnerBaseLogic spawner;

        public CheckSpawn(){}

        public void initCheckSpawn(EntityLiving entity, World world, float x, float y, float z, @Nullable MobSpawnerBaseLogic spawner){
            initLivingSpawnEvent(entity, world, x, y, z);
            this.isSpawner = spawner != null;
            this.spawner = spawner;
        }


        public CheckSpawn(EntityLiving entity, World world, float x, float y, float z, @Nullable MobSpawnerBaseLogic spawner) {
            super(entity, world, x, y, z);
            this.isSpawner = spawner != null;
            this.spawner = spawner;
        }


        @Deprecated // TODO: Remove in 1.13
        public CheckSpawn(EntityLiving entity, World world, float x, float y, float z, boolean isSpawner) {
            super(entity, world, x, y, z);
            this.isSpawner = isSpawner;
            spawner = null;
        }


        @Deprecated // TODO: Remove in 1.13
        public CheckSpawn(EntityLiving entity, World world, float x, float y, float z) {
            this(entity, world, x, y, z, true);
        }

        public boolean isSpawner() {
            return isSpawner;
            // TODO: replace with spawner null check in 1.13
        }


        @Nullable
        public MobSpawnerBaseLogic getSpawner() {
            return spawner;
        }
    }


    @Cancelable
    public static class SpecialSpawn extends LivingSpawnEvent {
        @Nullable
        private final MobSpawnerBaseLogic spawner;

        @Deprecated // TODO: remove in 1.13
        public SpecialSpawn(EntityLiving entity, World world, float x, float y, float z) {
            super(entity, world, x, y, z);
            spawner = null;
        }


        public SpecialSpawn(EntityLiving entity, World world, float x, float y, float z, @Nullable MobSpawnerBaseLogic spawner) {
            super(entity, world, x, y, z);
            this.spawner = spawner;
        }


        public SpecialSpawn initSpecialSpawn(EntityLiving entity, World world, float x, float y, float z) {
            initLivingSpawnEvent(entity, world, x, y, z);
            setCanceled(false);
            return this;
        }


        @Nullable
        public MobSpawnerBaseLogic getSpawner() {
            return spawner;
        }
    }


    @HasResult
    public static class AllowDespawn extends LivingSpawnEvent {
        public AllowDespawn(EntityLiving entity) {
            super(entity, entity.world, (float)entity.posX, (float)entity.posY, (float)entity.posZ);
        }
    }
}