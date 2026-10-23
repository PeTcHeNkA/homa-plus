package plus.quest.motion;

import org.bukkit.Location;


public class TpMotion extends BasicPos{

    public final byte yaw;
    public final byte pith;

    public TpMotion(double X, double Y, double Z, byte yaw, byte pith) {
        super(X, Y, Z);
        this.yaw = yaw;
        this.pith = pith;
    }


    public TpMotion(String rawData){
        super(rawData);
        String[] hs = rawData.split(";");
        if(hs.length>3){
            yaw = Byte.parseByte(hs[3]);
            pith = Byte.parseByte(hs[4]);
        } else {
            yaw = 0;
            pith = 0;
        }
    }


    public static BasicPos build(String rawData){
        String[] hs = rawData.split(";");
        if(hs.length>3){
            return new TpMotion(
                    Double.parseDouble(hs[0]), Double.parseDouble(hs[1]), Double.parseDouble(hs[2]),
                    Byte.parseByte(hs[3]), Byte.parseByte(hs[4])
            );
        } else {
            return new BasicPos(
                    Double.parseDouble(hs[0]), Double.parseDouble(hs[1]), Double.parseDouble(hs[2])
            );
        }
    }


    public static BasicPos build(Location loc){
        byte yaw = (byte)(128 - (loc.getYaw() * (360 / 256)));
        byte pith = (byte)(loc.getPitch() * (360 / 256));
        return new TpMotion(loc.getX(), loc.getY(), loc.getZ(), yaw, pith);
    }
}