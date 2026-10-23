package plus.item;

public final class BoostStats {

    public final double BOOST_ATTACK;
    public final double BOOST_CRIT;
    public final double BOOST_SHIELD;
    public final double BOOST_CRIT_RATE;
    public final double ADDITIONAL_ATTACK;
    public final double ADDITIONAL_SHIELD;
    public final double ADDITIONAL_ENERGY;

    public BoostStats(String arg){
        if(arg.charAt(arg.length()-1)=='&')arg = arg+0;
        String[] args = arg.split("&");
        for(int i=0;i<args.length;i++){
            if(args[i].isEmpty())args[i]="0";
        }
        BOOST_ATTACK = Double.parseDouble(args[0]);
        BOOST_CRIT = Double.parseDouble(args[1]);
        BOOST_SHIELD = Double.parseDouble(args[2]);
        BOOST_CRIT_RATE = Double.parseDouble(args[3]);
        ADDITIONAL_ATTACK = Double.parseDouble(args[4]);
        ADDITIONAL_SHIELD = Double.parseDouble(args[5]);
        if(args.length>6)
        ADDITIONAL_ENERGY = Double.parseDouble(args[6]);
        else ADDITIONAL_ENERGY = 0.0;
    }

    public BoostStats(double... args){
        BOOST_ATTACK = args[0];
        BOOST_CRIT = args[1];
        BOOST_SHIELD = args[2];
        BOOST_CRIT_RATE = args[3];
        ADDITIONAL_ATTACK = args[4];
        ADDITIONAL_SHIELD = args[5];
        ADDITIONAL_ENERGY = args[6];
    }

    public String toString(){
        return String.join("&",
                new String[]{
                format(BOOST_ATTACK),
                format(BOOST_CRIT),
                format(BOOST_SHIELD),
                format(BOOST_CRIT_RATE),
                format(ADDITIONAL_ATTACK),
                format(ADDITIONAL_SHIELD),
                format(ADDITIONAL_ENERGY)}
                );
    }
    private String format(double i){
        return i>0.001?String.valueOf( ((float) ((int) (i * 1000))) / 1000):"";
    }
}