import java.util.Random;

public class testBlock {

    private static final Random r = new Random();

    public testBot getRandomBoostStats(int Power, testBot baseStats){
        double[] stats;

        int CountLines = 0;
        int[] boostedIndexes = new int[]{-1, -1, -1};


        if(baseStats==null){
            stats = new double[]{0 , 0, 0, 0, 0, 0, 0};
        } else {
            stats = new double[]{
                    baseStats.BOOST_ATTACK,
                    baseStats.BOOST_CRIT,
                    baseStats.BOOST_SHIELD,
                    baseStats.BOOST_CRIT_RATE,
                    baseStats.ADDITIONAL_ATTACK,
                    baseStats.ADDITIONAL_SHIELD,
                    baseStats.ADDITIONAL_ENERGY
            };

            for(int i=0;i<7;i++){
                if(CountLines>2)stats[i]=0;
                if(stats[i]>0.01){
                    boostedIndexes[CountLines]=i;
                    CountLines+=1;
                }
            }
        }

        do {
            int Ench = r.nextInt(Power+1);
            Power -= Ench;

            if(CountLines<3) {
                int Stat = r.nextInt(7);

                if(stats[Stat] < 0.01){
                    boostedIndexes[CountLines]=Stat;
                    CountLines += 1;
                }
                stats[Stat] += Ench;
            } else {
                int Stat = boostedIndexes[r.nextInt(3)];
                stats[Stat] += Ench;
            }
        } while (Power>0);

        return new testBot(
                stats[0]>0.01?(stats[0]*3.5+r.nextDouble())*0.01:0,
                stats[1]>0.01?(stats[1]*3+r.nextDouble())*0.01:0,
                stats[2]>0.01?(1.2*stats[2]*4.5+r.nextDouble())*0.01:0,
                stats[3]>0.01?(stats[3]*1.5+r.nextDouble())*0.01:0,
                stats[4]>0.01?stats[4]*2.5+r.nextDouble():0,
                stats[5]>0.01?stats[5]*30+30*r.nextDouble():0,
                stats[6]>0.01?stats[6]*15+15*r.nextDouble():0
        );
    }

}