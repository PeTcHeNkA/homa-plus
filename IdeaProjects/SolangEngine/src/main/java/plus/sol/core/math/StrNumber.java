package plus.sol.core.math;


public class StrNumber extends Number{
    private final String s;

    public StrNumber(String s) {
        this.s = s;
    }

    @Override
    public int intValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long longValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return s;
    }
}
