package plus.client.camera;

import net.minecraft.util.MouseFilter;


public class AMouseFilter extends MouseFilter {
    private final float boost;

    private float targetValue = 0;
    private float remainingValue = 0;
    private float lastAmount = 0;

    public AMouseFilter(){
        this(0.5f);
    }

    public AMouseFilter(float boost){
        this.boost = boost;
    }

    public float smooth(float num1, float num2) {
        this.targetValue += num1;
        num1 = (this.targetValue - this.remainingValue) * num2;
        this.lastAmount += (num1 - this.lastAmount) * 0.5;

        if (num1 > 0.0F && num1 > this.lastAmount || num1 < 0.0F && num1 < this.lastAmount) {
            num1 = this.lastAmount;
        }

        this.remainingValue += num1;
        return num1;
    }

    public void reset() {
        this.targetValue = 0.0F;
        this.remainingValue = 0.0F;
        this.lastAmount = 0.0F;
    }
}
