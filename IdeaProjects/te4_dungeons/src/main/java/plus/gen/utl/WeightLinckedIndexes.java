package plus.gen.utl;

public class WeightLinckedIndexes {
    private final int[] indexes;
    private final float[] weights;

    public WeightLinckedIndexes(int[] indexes, float[] weights){
        this.indexes = indexes;
        this.weights = new float[indexes.length+1];
        this.weights[0] = 0;
        for(int i=1;i<this.weights.length;i+=1){
            this.weights[i] = getOr1(weights, i-1) + this.weights[i-1];
        }
        float summ = this.weights[this.weights.length-1];
        if(summ*summ-1f>0.00001){
            float coef = 1f/summ;
            for(int i=1;i<this.weights.length;i++){
                this.weights[i] = this.weights[i]*coef;
            }
        }
    }


    private float getOr1(float[] w, int i){
        return i<w.length ? w[i] : 1;
    }

    public int getIndexOfWeight(float weight){
        for(int i=0;i<indexes.length;i++){
            if(weight>weights[i] && weight <weights[i+1]){
                return indexes[i];
            }
        }
        return indexes[indexes.length-1];
    }
}