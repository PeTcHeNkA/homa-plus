package plus.utls.bin;

public interface BinNum {
    int getMaxStabileValue(int i);
    int getVal(int i);
    void setVal(int i, int value);
    void setData(int data);
    int toInt();


    static BinNum create(int... patern){
        return new BinNumImp(patern);
    }


    static int[] genPatern(int... patern){
        int summ = 0;
        for (int i = 0; i < patern.length; i++) {
            int shift = 1;
            int dest = 0;
            while (shift < patern[i]) {
                shift = shift << dest;
                dest += 1;
            }
            patern[i] = summ;
            summ += dest;
        }
        if (summ >= 32) {
            throw new IndexOutOfBoundsException("Max = 32!");
        }
        return patern;
    }


    abstract class Provider implements BinNum {
        protected abstract int getV();
        protected abstract void setV(int v);
        protected abstract int[] patern();

        protected int patern(int i){
            return patern()[i];
        }
        protected int paternLen(){
            return patern().length;
        }


        @Override
        public int getMaxStabileValue(int i) {
            int[] patern = patern();
            if (i == patern.length - 1) {
                return (1 << (31 - patern[i]));
            }
            return (1 << (patern[i + 1] - patern[i])) - 1;
        }


        @Override
        public int getVal(int i) {
            return (getV() & getMask(i + 1)) >> (patern(i));
        }


        private int getMask(int i) {
            int[] patern = patern();
            if (i < patern.length) {
                int mask = (1 << patern[i]) - 1;
                if (i > 0) {
                    return mask ^ ((1 << patern[i - 1]) - 1);
                }
                return mask;
            } else {
                return ~0 ^ ((1 << patern[i - 1]) - 1);
            }
        }


        @Override
        public void setVal(int i, int value) {
            int mask = getMask(i + 1);
            int val = getV() & ~mask;
            if (value > 0) {
                val = val | (mask & (value << patern(i)));
            }
            setV(val);
        }


        @Override
        public void setData(int data) {
            setV(data);
        }


        @Override
        public int toInt() {
            return getV();
        }
    }
}
