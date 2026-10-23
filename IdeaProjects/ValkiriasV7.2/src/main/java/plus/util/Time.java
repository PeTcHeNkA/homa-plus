package plus.util;


public class Time {
    public final int SEC;
    public final int MIN;
    public final int HOR;
    public final int DAY;


    public Time(int l){
        l = l/1000;
        SEC = l%60;
        l = l/60;
        MIN = l%60;
        l = l/60;
        HOR = l%24;
        DAY = l/24;
    }


    public Time(long l){
        l = l/1000;
        SEC = (int) (l%60);
        l = l/60;
        MIN = (int) (l%60);
        l = l/60;
        HOR = (int) (l%24);
        DAY = (int) l/24;
    }


    public Time(){
        this(System.currentTimeMillis());
    }


    public String toString(){
        return String.format("%ssec, %smin, %shours, %sday", SEC, MIN, HOR, DAY);
    }


    public long toLong(){
        return (((DAY* 24L +HOR)*60+MIN)*60+SEC)*1000;
    }
}