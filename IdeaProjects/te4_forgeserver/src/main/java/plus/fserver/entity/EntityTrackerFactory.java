package plus.fserver.entity;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.projectile.*;


public class EntityTrackerFactory {
    private final TrackInfo empty = new TrackInfo(0,0,false);
    private final IntObjectHashMap<TrackInfo> map = new IntObjectHashMap<>();

    private TrackInfo getDef(Entity entityIn){
        if (entityIn instanceof EntityArrow) {
            return new TrackInfo(64, 20, false);
        } else if (entityIn instanceof EntitySmallFireball) {
            return new TrackInfo(64, 10, false);
        }
        else if (entityIn instanceof EntityFireball) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntitySnowball) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityLlamaSpit) {
            return new TrackInfo(64, 10, false);
        }
        else if (entityIn instanceof EntityEnderPearl) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityEnderEye) {
            return new TrackInfo(64, 4, true);
        }
        else if (entityIn instanceof EntityEgg) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityPotion) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityExpBottle) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityFireworkRocket) {
            return new TrackInfo(64, 10, true);
        }
        else if (entityIn instanceof EntityItem) {
            return new TrackInfo(64, 20, true);
        }
        else if (entityIn instanceof EntityMinecart) {
            return new TrackInfo(80, 3, true);
        }
        else if (entityIn instanceof EntityBoat) {
            return new TrackInfo(80, 3, true);
        }
        else if (entityIn instanceof EntitySquid) {
            return new TrackInfo(64, 3, true);
        }
        else if (entityIn instanceof EntityWither) {
            return new TrackInfo(80, 3, false);
        }
        else if (entityIn instanceof EntityShulkerBullet) {
            return new TrackInfo(80, 3, true);
        }
        else if (entityIn instanceof EntityBat) {
            return new TrackInfo(80, 3, false);
        }
        else if (entityIn instanceof EntityDragon) {
            return new TrackInfo(160, 3, true);
        }
        else if (entityIn instanceof IAnimals) {
            return new TrackInfo(80, 3, true);
        }
        else if (entityIn instanceof EntityTNTPrimed) {
            return new TrackInfo(160, 10, true);
        }
        else if (entityIn instanceof EntityFallingBlock) {
            return new TrackInfo(160, 20, true);
        }
        else if (entityIn instanceof EntityHanging) {
            return new TrackInfo(160, Integer.MAX_VALUE, false);
        }
        else if (entityIn instanceof EntityArmorStand) {
            return new TrackInfo(160, 3, true);
        }
        else if (entityIn instanceof EntityXPOrb) {
            return new TrackInfo(160, 20, true);
        }
        else if (entityIn instanceof EntityAreaEffectCloud) {
            return new TrackInfo(160, Integer.MAX_VALUE, true);
        }
        else if (entityIn instanceof EntityEnderCrystal) {
            return new TrackInfo(256, Integer.MAX_VALUE, false);
        }
        else if (entityIn instanceof EntityEvokerFangs) {
            return new TrackInfo(160, 2, false);
        }
        return empty;
    }


    public TrackInfo getFor(Entity entityIn){
        int hs = entityIn.getClass().hashCode();
        TrackInfo inf = map.get(hs);
        if(inf==null) map.put(hs, inf = getDef(entityIn));
        return inf == empty?null:inf;
    }


    public static final class TrackInfo{
        public final int range,  updates;
        public final boolean sendVelocity;
        TrackInfo(int trackingRange, int updateFrequency, boolean sendVelocityUpdates) {
            this.range = trackingRange;
            this.updates = updateFrequency;
            this.sendVelocity = sendVelocityUpdates;
        }
    }
}