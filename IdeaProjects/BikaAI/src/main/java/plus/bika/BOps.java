package plus.bika;

import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Constant;
import org.tensorflow.types.TUint8;


public class BOps {
    public final Ops tf;

    public BOps(Ops tf) {
        this.tf = tf;
    }


    public BOps(){
        this(Ops.create());
    }


    public Constant<TUint8> const0(InMatrix matrix){
        return tf.constant(matrix.getRaw());
    }
}