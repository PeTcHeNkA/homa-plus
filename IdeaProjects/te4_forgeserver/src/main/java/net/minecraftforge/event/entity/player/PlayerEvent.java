package net.minecraftforge.event.entity.player;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;


public class PlayerEvent extends LivingEvent {
    public PlayerEvent(EntityPlayer player) {
        super(player);
    }


    public PlayerEvent(){}


    public EntityPlayer getEntityPlayer() {
        return (EntityPlayer) getEntity();
    }


    public static class HarvestCheck extends PlayerEvent {
        private IEventListener[] listeners;
        private IBlockState state;
        private boolean success;

        public HarvestCheck(EntityPlayer player, IBlockState state, boolean success) {
            super(player);
            this.state = state;
            this.success = success;
        }

        public HarvestCheck(){}

        public boolean post(EntityPlayer player, IBlockState state, boolean success) {
            initLiving(player);
            this.state = state;
            this.success = success;

            for (IEventListener listener:listeners)listener.invoke(this);

            return success;
        }


        public HarvestCheck forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public IBlockState getTargetBlock() {
            return this.state;
        }


        public boolean canHarvest() {
            return this.success;
        }


        public void setCanHarvest(boolean success){
            this.success = success;
        }
    }


    @Cancelable
    public static class BreakSpeed extends PlayerEvent {
        private IEventListener[] listeners;

        private IBlockState state;
        private float originalSpeed;
        private float newSpeed = 0.0f;
        private BlockPos pos;

        public BreakSpeed(){
            super();
        }

        public BreakSpeed(EntityPlayer player, IBlockState state, float original, BlockPos pos) {
            super(player);
            this.state = state;
            this.originalSpeed = original;
            this.setNewSpeed(original);
            this.pos = pos;
        }


        public BreakSpeed forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public boolean post(EntityPlayer player, IBlockState state, float original, BlockPos pos) {
            initLivingCancelable(player);
            this.state = state;
            this.originalSpeed = original;
            this.setNewSpeed(original);
            this.pos = pos;

            for (IEventListener listener:listeners)listener.invoke(this);
            return isCanceled();
        }


        public IBlockState getState() {
            return state;
        }


        public float getOriginalSpeed() {
            return originalSpeed;
        }


        public float getNewSpeed() {
            return newSpeed;
        }


        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }


        public BlockPos getPos() {
            return pos;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }
    }


    public static class NameFormat extends PlayerEvent {
        private final String username;
        private String displayname;

        public NameFormat(EntityPlayer player, String username) {
            super(player);
            this.username = username;
            this.setDisplayname(username);
        }


        public String getUsername() {
            return username;
        }


        public String getDisplayname() {
            return displayname;
        }


        public void setDisplayname(String displayname) {
            this.displayname = displayname;
        }
    }


    public static class Clone extends PlayerEvent {
        private final EntityPlayer original;
        private final boolean wasDeath;

        public Clone(EntityPlayer _new, EntityPlayer oldPlayer, boolean wasDeath) {
            super(_new);
            this.original = oldPlayer;
            this.wasDeath = wasDeath;
        }


        public EntityPlayer getOriginal() {
            return original;
        }


        public boolean isWasDeath() {
            return wasDeath;
        }
    }


    public static class StartTracking extends PlayerEvent {
        private IEventListener[] listeners;
        private Entity target;

        public StartTracking(EntityPlayer player, Entity target) {
            super(player);
            this.target = target;
        }

        public StartTracking(){}


        public StartTracking forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public void post(EntityPlayer player, Entity target){
            initLiving(player);
            this.target = target;
            for (IEventListener listener:listeners)listener.invoke(this);
        }


        public Entity getTarget() {
            return target;
        }
    }


    public static class StopTracking extends PlayerEvent {
        private IEventListener[] listeners;
        private Entity target;

        public StopTracking(EntityPlayer player, Entity target) {
            super(player);
            this.target = target;
        }

        public StopTracking(){}


        public StopTracking forBus(EventBus bus){
            listeners = getListenerList().getListeners(bus.id());
            return this;
        }


        public void post(EntityPlayer player, Entity target){
            initLiving(player);
            this.target = target;
            for (IEventListener listener:listeners)listener.invoke(this);
        }


        public Entity getTarget() {
            return target;
        }
    }


    public static class LoadFromFile extends PlayerEvent {
        private final File playerDirectory;
        private final String playerUUID;

        public LoadFromFile(EntityPlayer player, File originDirectory, String playerUUID) {
            super(player);
            this.playerDirectory = originDirectory;
            this.playerUUID = playerUUID;
        }


        public File getPlayerFile(String suffix) {
            if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
            return new File(this.getPlayerDirectory(), this.getPlayerUUID() +"."+suffix);
        }


        public File getPlayerDirectory() {
            return playerDirectory;
        }


        public String getPlayerUUID() {
            return playerUUID;
        }
    }

    public static class SaveToFile extends PlayerEvent {
        private final File playerDirectory;
        private final String playerUUID;

        public SaveToFile(EntityPlayer player, File originDirectory, String playerUUID) {
            super(player);
            this.playerDirectory = originDirectory;
            this.playerUUID = playerUUID;
        }


        public File getPlayerFile(String suffix) {
            if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
            return new File(this.getPlayerDirectory(), this.getPlayerUUID() +"."+suffix);
        }


        public File getPlayerDirectory() {
            return playerDirectory;
        }


        public String getPlayerUUID() {
            return playerUUID;
        }
    }


    public static class Visibility extends PlayerEvent {
        private double visibilityModifier = 1D;

        public Visibility(EntityPlayer player) {
            super(player);
        }


        public void modifyVisibility(double mod) {
            visibilityModifier *= mod;
        }


        public double getVisibilityModifier() {
            return visibilityModifier;
        }
    }
}
