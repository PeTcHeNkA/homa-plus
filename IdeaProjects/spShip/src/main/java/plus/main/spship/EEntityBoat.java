package plus.main.spship;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;


public class EEntityBoat extends EntityBoat {
    protected EEntitySkeleton head;
    private int liveTime = 0;
    private int lastMovedTime = 0;
    protected VecMask moveMask = VecMask.EMPTY;
    protected VecMask moveDnMask = new MoveDnMask(this);
    private final AutoPilot pilot = new AutoPilot() {
        @Override
        public Vec3D next(int time) {
            currentStartPoint = new Vec3D(currentStartPoint.b+0.01, currentStartPoint.c + 0.03, currentStartPoint.d);
            return currentStartPoint;
        }
    };

    EEntityBoat(EEntitySkeleton head, World world, final double x, final double y, final double z) {
        super(world, x, y, z);

//        System.out.println(EnumMoveType.a.name());
//        System.out.println(EnumMoveType.b.name());
//        System.out.println(EnumMoveType.c.name());
//        System.out.println(EnumMoveType.d.name());
//        System.out.println(EnumMoveType.e.name());
//        super.setNoGravity(true);

        this.head = head;
    }

    public void tick() {
        ++liveTime;
        if (pilot.isActive) {
            super.move(EnumMoveType.a, pilot.next());
        }
    }

    private double currentCren = 0;
    public void cren(double f){
        if(f==currentCren)return;
        currentCren = f;
        head.setXRot(90 + (float)(f*5));
    }


    @Override
    public boolean addPassenger(Entity passenger) {
        head.setYRot(this.getYRot());
        head.setXRot(90);//вертикальный наклон корабля
        return super.addPassenger(passenger);
    }



    @Override
    public boolean removePassenger(Entity passenger, boolean suppressCancellation) {
        if (passenger instanceof EntityHuman p) {
            if (liveTime - lastMovedTime < 5) {
                System.out.println("NEN");
//                pilot.active(Vec3D.a, 100);
                super.move(EnumMoveType.c, new Vec3D(0.2f,0.3f, 0f));
                return false;
            } else {
                //code

            }
        }
        return super.removePassenger(passenger, suppressCancellation);
    }


    private Vec3D latestVec = null;

    //    private final Vec3D vec0 = new Vec3D(0,0,0);
    @Override
    public void move(EnumMoveType movementType, Vec3D movement) {
        if (movement.equals(latestVec)) {
            return;
        }
//        if(movementType == EnumMoveType.b){
//            System.out.println("NEN");
//            return;
//        }
//        System.out.println(movementType.name());
        movement = moveMask.apply(movement);

        cren(movement.c);

        latestVec = movement;
        lastMovedTime = liveTime;
        super.move(movementType, movement);
    }


    @Override
    public void i(Entity passenger) {}
}

class MoveDnMask extends VecMask{
    private final EEntityBoat link;
    public MoveDnMask(EEntityBoat link){
        this.link = link;
    }

    @Override
    public Vec3D apply(Vec3D vec) {
        link.moveMask = VecMask.EMPTY;
        return new Vec3D(vec.b, vec.c - 0.3f, vec.d);
    }
}