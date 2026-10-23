package plus.quest.motion;


public class BasicPos {
    public double X;
    public double Y;
    public double Z;

    public BasicPos(double x, double y, double z){
        X = x;
        Y = y;
        Z = z;
    }


    public BasicPos(String rawData){
        String[] hs = rawData.split(";");
        X = Double.parseDouble(hs[0]);
        Y = Double.parseDouble(hs[1]);
        Z = Double.parseDouble(hs[2]);
    }


    public void edit(double x, double y, double z){
        this.X = x;
        this.Y = y;
        this.Z = z;
    }
}