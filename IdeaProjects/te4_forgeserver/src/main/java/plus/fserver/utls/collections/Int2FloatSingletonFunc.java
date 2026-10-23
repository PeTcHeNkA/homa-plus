package plus.fserver.utls.collections;

import it.unimi.dsi.fastutil.ints.Int2FloatFunctions;


public class Int2FloatSingletonFunc extends Int2FloatFunctions.Singleton {
    public Int2FloatSingletonFunc(int key, float value) {
        super(key, value);
        defaultReturnValue(Float.NaN);
    }

    public int key(){
        return key;
    }


    public float value(){
        return value;
    }
}