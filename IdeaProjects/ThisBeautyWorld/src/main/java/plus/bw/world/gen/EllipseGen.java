package plus.bw.world.gen;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import plus.bw.utl.MatrixUtl;
import plus.bw.world.Matrix;
import java.util.function.Consumer;


/**
 * Describes and fills an ellipse in a matrix
 */
public class EllipseGen implements Consumer<Matrix> {
    private final Int2IntFunction function;
    private final Int2IntFunction other;

    public EllipseGen(Int2IntFunction function) {
        this(function, MatrixUtl.EMPTY_FUNC);
    }

    public EllipseGen(Int2IntFunction function, Int2IntFunction other) {
        this.function = function;
        this.other = other;
    }


    @Override
    public void accept(Matrix matrix) {
        int cenX = (matrix.height()>>1)-1, cenY = (matrix.width()>>1)-1;

        for (int cX=0;cX<matrix.height();cX++){
            for (int cY=0;cY<matrix.width();cY++){

                if(isPointInsideEllipse(cX, cY, cenX, cenY, cenX, cenY)){
                    matrix.apply(cX, cY, function);
                } else {
                    matrix.apply(cX, cY, other);
                }
            }
        }
    }


    public static boolean isPointInsideEllipse(int x, int y, int centerX, int centerY, double sizeX, double sizeY) {
        // Equation of ellipse: ((x - h)^2 / a^2) + ((y - k)^2 / b^2) = 1
        double value = Math.pow((x - centerX) / sizeX, 2) + Math.pow((y - centerY) / sizeY, 2);
        return value <= 1;
    }
}