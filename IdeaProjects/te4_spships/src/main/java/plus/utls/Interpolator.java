package plus.utls;

public class Interpolator {
    private double inn;
    public double calc(double v, float partialTicks){
        double result = inn + MathUtils.range(inn, v) * partialTicks;
        if(inn != v){
            inn = v;
        }
        return result;
    }
}