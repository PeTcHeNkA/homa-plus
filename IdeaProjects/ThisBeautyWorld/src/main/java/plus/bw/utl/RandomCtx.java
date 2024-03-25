package plus.bw.utl;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;


/**
 * Stable pseudo-rand, on the basis of an algorithm XorShift
 * Includes methods for the accelerated calculation of pseudorandom numbers
 * It is used for filling of a matrix with random numbers
 */
public class RandomCtx {
    private int def, x, y, z, w;

    public RandomCtx(){
        reset(1);
    }


    public RandomCtx(int n){
        reset(n);
    }


    /**
     * @return The following pseudorandom number Limited to number
     */
    public int next(int max){
        return next() % max;
    }


    /**
     * @return true or false
     */
    public boolean nextB(){
        return (next() & 1) == 1;
    }


    /**
     * @return 1 or 0
     */
    public int next1(){
        return next() & 1;
    }


    /**
     * @return Function which will generate random numbers in the specified range
     */
    public Int2IntFunction rand(int max){
        return key -> next(max);
    }


    /**
     * @return The following pseudorandom number Limited to number, degree 2 - 1 (15, 31, 63, and others)
     */
    public int next2(int max){
        return next() & max;
    }


    /**
     * @return Following pseudorandom number. It is not recommended to use without restrictions
     */
    public int next(){
        int t = x^(x<<11);
        x = y;
        y = z;
        z = w;
        return w = (w^(w>>19)) ^ (t^(t>>8));
    }


    /**
     * @return Initial value for calculation of pseudorandom numbers
     */
    public int defSeed(){
        return def;
    }


    /**
     * Dumps rand to an initial state
     */
    public void reset(){
        reset(def);
    }


    /**
     * Establishes initial value and dumps
     */
    public void reset(int n){
        def = n;
        x = n;y = 123; z = 456; w = 768;
    }
}