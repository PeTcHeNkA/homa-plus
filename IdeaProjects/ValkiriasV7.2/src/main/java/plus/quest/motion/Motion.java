package plus.quest.motion;


public class Motion {
    public final short offsetX;
    public final short offsetY;
    public final short offsetZ;
    public final byte yaw;
    public final byte pith;

    public Motion(short offsetX, short offsetY, short offsetZ, byte yaw, byte pith) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.yaw = yaw;
        this.pith = pith;
    }


    public Motion(short[] offSet, byte[] yawPith){
        this(offSet[0], offSet[1], offSet[2], yawPith[0], yawPith[1]);
    }
}