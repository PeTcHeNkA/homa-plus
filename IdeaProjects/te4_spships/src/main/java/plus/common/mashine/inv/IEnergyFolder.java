package plus.common.mashine.inv;

import java.util.List;


public interface IEnergyFolder {
    IEnergyFolder EMPTY = new Sell(0);

    boolean takeEnergy(int amount);


    boolean putEnergy(int amount);


    int getCurEnergy();


    int getMaxEnergy();


    default int takeMaxEnergy(){
        int curEnrg = getCurEnergy();
        takeEnergy(curEnrg);
        return curEnrg;
    }


    default int moveEnergy(IEnergyFolder to, int max){
        return moveEnergy(to, this, max);
    }


    static boolean takeMulti(IEnergyFolder[] group, int amount){
        int[] take = new int[group.length];
        int i=0;
        for(IEnergyFolder f:group){
            if(amount==0)break;
            else if (amount==-1) {
                take[i] = f.getCurEnergy();
            } else {
                int curE = f.getCurEnergy();
                if(curE>=amount){
                    take[i] = amount;
                    amount = 0;
                    break;
                } else {
                    amount = amount-curE;
                    take[i] = curE;
                }
            }
            ++i;
        }
        if(amount>0)return false;
        for(i=0;i<take.length;++i){
            group[i].takeEnergy(take[i]);
        }
        return true;
    }


    static boolean putMulti(IEnergyFolder[] group, int amount){
        int[] put = new int[group.length];
        int i=0;
        for(IEnergyFolder f:group){
            if(amount==0)break;
            else {
                int curE = f.getMaxEnergy()-f.getCurEnergy();
                if(curE > 0){
                    if(amount>curE){
                        amount = amount-curE;
                        put[i] = curE;
                    } else {
                        put[i] = amount;
                        amount = 0;
                        break;
                    }
                }
                i++;
            }

        }
        if(amount>0)return false;
        for(i=0;i<put.length;++i){
            group[i].putEnergy(put[i]);
        }
        return true;
    }


    static int getCurMulti(IEnergyFolder[] group){
        int curE = 0;
        for(IEnergyFolder f:group){
            curE+=f.getCurEnergy();
        }
        return curE;
    }


    static int getMaxMulti(IEnergyFolder[] group){
        int maxE = 0;
        for(IEnergyFolder f:group){
            maxE+=f.getMaxEnergy();
        }
        return maxE;
    }


    static int moveEnergy(IEnergyFolder in, IEnergyFolder out, int amn){
        if(amn>=0){
            return moveEnergy0(in, out, amn);
        } else {
            return moveEnergy0(out, in, -amn);
        }
    }


    static int moveEnergy0(IEnergyFolder in, IEnergyFolder out, int amn){
        int delta = in.getMaxEnergy()-in.getCurEnergy();
        if(delta>0){
            if(delta<amn){
                amn = delta;
            }
            int energy = out.getCurEnergy();
            if(energy > 0){
                if(energy>=amn){
                    energy = amn;
                }
                out.takeEnergy(energy);
                in.putEnergy(energy);
                return energy;
            }
        }
        return 0;
    }


    class Combined implements IEnergyFolder{
        private final IEnergyFolder[] group;

        public Combined(List<IEnergyFolder> group){
            this(group.toArray(new IEnergyFolder[0]));
        }


        public Combined(IEnergyFolder... group) {
            this.group = group;
        }


        public IEnergyFolder[] getMembers(){
            return group;
        }


        @Override
        public boolean takeEnergy(int amount) {
            return takeMulti(group, amount);
        }


        @Override
        public boolean putEnergy(int amount) {
            return putMulti(group, amount);
        }


        @Override
        public int getCurEnergy() {
            return getCurMulti(group);
        }


        @Override
        public int getMaxEnergy() {
            return getMaxMulti(group);
        }


        @Override
        public String toString() {
            return "cap= "+getCurEnergy()+"/"+getMaxEnergy();
        }
    }


    class Sell implements IEnergyFolder{
        private final int max;
        protected int cur;

        public Sell(int amount){
            this.max = amount;
            this.cur = amount;
        }


        public Sell(int cur, int max){
            this.max = max;
            this.cur = cur;
        }


        @Override
        public boolean takeEnergy(int amount) {
            if(cur>=amount){
                cur = cur - amount;
                return true;
            }
            return false;
        }


        @Override
        public boolean putEnergy(int amount) {
            if(cur+amount<=max){
                cur = cur+amount;
                return true;
            }
            return false;
        }


        @Override
        public int getCurEnergy() {
            return cur;
        }


        @Override
        public int getMaxEnergy() {
            return max;
        }


        @Override
        public String toString() {
            return "cap="+getCurEnergy()+"/"+getMaxEnergy();
        }
    }
}