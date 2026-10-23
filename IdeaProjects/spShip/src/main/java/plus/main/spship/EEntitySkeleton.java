package plus.main.spship;

import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySkeleton;
import net.minecraft.world.level.World;


public class EEntitySkeleton extends EntitySkeleton {

    protected EEntityBoat Platform;


    public EEntitySkeleton(World world, double x, double y, double z) {
        this(world, x, y, z, null);
    }
    public EEntitySkeleton(World world, double x, double y, double z, EEntityBoat platform) {
        super(EntityTypes.aB, world);
        setPosition(x, y, z);
        super.setNoAI(true);
        setShouldBurnInDay(false);
        world.addEntity(this);
        if (platform == null) {
            Platform = new EEntityBoat(this, world, x, y, z);
            world.addEntity(Platform);
        } else {
            Platform = platform;
        }
        startRiding(Platform);
    }
    public EEntitySkeleton(EEntitySkeleton e) {
        this(e.t, e.locX(), e.locY(), e.locZ(), e.Platform);
    }


    public void tick() {}
    public void passengerTick() {}
    public void movementTick() {}

    public void setPose(EntityPose pose) {

    }

    public void m(float bodyYaw) {}

//    public void setYawPitch(float yaw, float pith){}
//    public void setYRot(float f){}
//    public float getYRot(){return 0;};
//    public void setXRot(float f){}
//    public float getXRot() {return 0;}
//    public float getHeadRotation() {return 0;}
//    public void setHeadRotation(float headYaw) {}
}
