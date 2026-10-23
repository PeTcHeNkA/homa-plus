package plus.utl;

public final class Location {

    public final int X;
    public final int Y;
    public final int Z;
    public final float YAW;
    public final float PITH;

    public Location(String rawArgs){
        String[] args = rawArgs.split(";");
        if(args.length>2){
            X = Integer.parseInt(args[0]);
            Y = Integer.parseInt(args[1]);
            Z = Integer.parseInt(args[2]);
            if(args.length>4){
                YAW = Float.parseFloat(args[3]);
                PITH = Float.parseFloat(args[3]);
            } else {
                YAW = 90;
                PITH = 10;
            }
        } else throw new NullPointerException("Empty args! use [x;y;z] or [x;y;z;yaw;pith]");
    }
}
