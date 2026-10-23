package plus.utls.bin;


public class BinNumImp implements BinNum {
    int val = 0;
    private final int[] patern;

    public BinNumImp(int... patern) {
        this.patern = BinNum.genPatern(patern);
    }


    public int getMaxStabileValue(int i) {
        if (i == patern.length - 1) {
            return (1 << (31 - patern[i]));
        }
        return (1 << (patern[i + 1] - patern[i])) - 1;
    }


    public int getVal(int i) {
        return (val & getMask(i + 1)) >> (patern[i]);
    }


    public void setVal(int i, int value) {
        int mask = getMask(i + 1);
        val = val & ~mask;
        if (value > 0) {
            val = val | (mask & (value << patern[i]));
        }
    }


    private int getMask(int i) {
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


    public void setData(int data) {
        this.val = data;
    }


    public int toInt() {
        return val;
    }


    @Override
    public int hashCode() {
        return val;
    }


    @Override
    public String toString() {
        return Integer.toString(val);
    }
}