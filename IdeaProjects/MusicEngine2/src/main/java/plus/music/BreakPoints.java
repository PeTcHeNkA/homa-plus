package plus.music;


public abstract class BreakPoints {

    public abstract Point[] values();

    public final boolean isEmpty(){
        Point[] r = values();
        return r==null || r.length==0;
    }


    public static BreakPoints build(BSound s, String rawData){
        if(rawData==null || rawData.isEmpty() || rawData.equals("-1"))return build();
        try {
            if(rawData.contains("&&=")){
                return new BreakPointsE(s, rawData);
            }
            return new BreakPointsA(s, rawData);
        } catch (Exception e){
            if(e instanceof NullPointerException er){
                System.out.println("Error in creating BreakPoints! -> "+er.getMessage());
            } else e.printStackTrace();
            return build();
        }
    }


    public static BreakPoints build(){
        return new BreakPoints() {
            @Override
            public Point[] values() {
                return new Point[0];
            }
        };
    }


    static int parseTime(String data){
        if(data.contains(":")){
            String[] d = data.split(":");
            if(d.length==2){
                return Integer.parseInt(d[0])*60000 + Integer.parseInt(d[1])*1000;
            } else if(d.length==3){
                return + Integer.parseInt(d[0])*3600000 + Integer.parseInt(d[1])*60000 + Integer.parseInt(d[2])*1000;
            } else throw new NullPointerException("Wrong format! use [%s:%s:%s]");
        } else {
            return Integer.parseInt(data);
        }
    }
}


class BreakPointsE extends BreakPoints{

    private final Point[] points;

    public BreakPointsE(BSound s, String rawData){
        int count = 0;
        int delay = parseTime(rawData.split("=")[1]);
        int time = delay;
        while (time<s.duration){
            count+=1;
            time+=delay;
        }
        this.points = new Point[count];
        boolean allowWaiting = delay<s.allowedWaiting;

        for(int i=0;i<count;i++){
            this.points[i] = Point.build(delay, allowWaiting);
        }
    }


    @Override
    public Point[] values() {
        return points;
    }
}


class BreakPointsA extends BreakPoints{

    private final Point[] points;

    public BreakPointsA(BSound s,String rawData){
        String[] data = rawData.split(";");
        points = new Point[data.length];

        int sTime = BreakPoints.parseTime(data[0]);
        points[0] = Point.build(sTime, sTime<s.getAllowedWaiting());

        for(int i=1;i<points.length;i++){
            sTime = BreakPoints.parseTime(data[i]);
            int range = points[i-1].intValue-sTime;
            points[i] = Point.build(range, range<s.getAllowedWaiting());
        }
    }


    @Override
    public Point[] values() {
        return points;
    }
}


class Point{
    public final int intValue;

    protected static Point build(int intValue, boolean allowWaiting){
        if(allowWaiting){
            return new Point(intValue);
        } else return new NPoint(intValue);
    }

    protected Point(int intValue) {
        this.intValue = intValue;
    }

    public boolean allowWaiting(){
        return true;
    }
}


class NPoint extends Point{

    protected NPoint(int intValue) {
        super(intValue);
    }

    @Override
    public boolean allowWaiting(){
        return false;
    }
}