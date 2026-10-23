import org.tensorflow.*;
import org.tensorflow.framework.optimizers.GradientDescent;
import org.tensorflow.framework.optimizers.Optimizer;
import org.tensorflow.ndarray.Shape;

import org.tensorflow.op.Op;
import org.tensorflow.op.Ops;
import org.tensorflow.op.Scope;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.core.Variable;
import org.tensorflow.op.math.*;
import org.tensorflow.proto.DataType;
import org.tensorflow.types.TBfloat16;
import org.tensorflow.types.TFloat32;

import java.util.Random;

public class TestTf {
    private static int TF_SIZE = 5; //4096

    public static void main(String[] args) {
        if(1==1){
            main2();
            return;
        }

        try (Graph graph = new Graph()) {
            Session session = new Session(graph);

            Ops tf = Ops.create(graph);

            //tf.image.
//            Operation a = graph.opBuilder("Const", "a")
//                    .setAttr("dtype", DataType.fromClass(Double.class))
//                    .setAttr("value", Tensor.<Double>create(3.0, Double.class))
//                    .build();
//            Operation b = graph.opBuilder("Const", "b")
//                    .setAttr("dtype", DataType.fromClass(Double.class))
//                    .setAttr("value", Tensor.<Double>create(2.0, Double.class))
//                    .build();

            //graph.add


            TFloat32 tensorA = TFloat32.scalarOf(0.5f);
            TFloat32 tensorB = TFloat32.scalarOf(1.5f);
                
//                TBfloat16 tensorA = TBfloat16.tensorOf(Shape.of(TF_SIZE, TF_SIZE));
//                tensorA.setFloat(1, 0, 0);
//
//                TBfloat16 tensorB = TBfloat16.tensorOf(Shape.of(TF_SIZE, TF_SIZE));
//                tensorB.setFloat(5, 1, 1);
//
////                Tensor<Float> tensorA = Tensor.of(TType., Shape.of(1, 1, 1));
////                Tensor<Float> tensorB = Tensor.create(b);
//
//                //Tensor t1 = Tensor.of()
//
//
//                session.runner().addTarget(Mul.create(Scope.))

                // Операция сложения
                try (Tensor result = session.runner()
                        .feed("Add", tensorA)
                        .feed("Add", tensorB)
                        .fetch("Add")
                        .run()
                        .get(0)) {
                    System.out.println("Результат: " + result);
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main2(){
        float[] xValues = {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};
        float[] yValues = new float[10];

        Random rnd = new Random(42);

        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = (float) (10 * xValues[i] + 2 + 0.1 * (rnd.nextDouble() - 0.5));
        }


        try (Graph graph = new Graph()) {
            Ops tf = Ops.create(graph);

            // Define placeholders
            Placeholder<TFloat32> xData = tf.placeholder(TFloat32.class, Placeholder.shape(Shape.scalar()));
            Placeholder<TFloat32> yData = tf.placeholder(TFloat32.class, Placeholder.shape(Shape.scalar()));

            // Define variables
            Variable<TFloat32> weight = tf.withName("w").variable(tf.constant(1f));
            Variable<TFloat32> bias = tf.withName("b").variable(tf.constant(1f));

            // Define the model function weight*x + bias
            Mul<TFloat32> mul = tf.math.mul(xData, weight);
            Add<TFloat32> yPredicted = tf.math.add(mul, bias);

            // Define loss function MSE
            Pow<TFloat32> sum = tf.math.pow(tf.math.sub(yPredicted, yData), tf.constant(2f));
            Div<TFloat32> mse = tf.math.div(sum, tf.constant(2f * 10));

            // Back-propagate gradients to variables for training
            Optimizer optimizer = new GradientDescent(graph, 0.1f);
            Op minimize = optimizer.minimize(mse);


//            Operand<TFloat32> input = tf.constant(
//                    new float[] {-20f, -1.0f, 0.0f, 1.0f, 20f});
//            Sigmoid<TFloat32> sigmoid = new Sigmoid<>(tf);

            try (Session session = new Session(graph)) {

                // Train the model on data
                for (int r = 0; r <20; r++){
                    for (int i = 0; i < xValues.length; i++) {
                        float y = yValues[i];
                        float x = xValues[i];

                        try (TFloat32 xTensor = TFloat32.scalarOf(x);
                             TFloat32 yTensor = TFloat32.scalarOf(y)) {

                            session.runner()
                                    .addTarget(minimize)
                                    .feed(xData.asOutput(), xTensor)
                                    .feed(yData.asOutput(), yTensor)
                                    .run();

                            System.out.println("Training phase");
                            System.out.println("x is " + x + " y is " + y);
                        }
                    }

                }
                // Extract linear regression model weight and bias values
                try (var result = session.runner()
                        .fetch("w")
                        .fetch("b")
                        .run()) {
                    System.out.println("Weight is " + result.get("w"));
                    System.out.println("Bias is " + result.get("b"));
                }

                // Let's predict y for x = 10f
                float x = 8.9f;
                float predictedY = 0f;

                try (TFloat32 xTensor = TFloat32.scalarOf(x);
                     TFloat32 yTensor = TFloat32.scalarOf(predictedY);
                     TFloat32 yPredictedTensor = (TFloat32)session.runner()
                             .feed(xData.asOutput(), xTensor)
                             .feed(yData.asOutput(), yTensor)
                             .fetch(yPredicted)
                             .run().get(0)) {

                    predictedY = yPredictedTensor.getFloat();

                    System.out.println("Predicted value: " + predictedY);
                }
            }
        }
    }
}
