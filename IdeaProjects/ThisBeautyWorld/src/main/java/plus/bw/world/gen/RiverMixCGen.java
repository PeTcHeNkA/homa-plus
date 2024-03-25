package plus.bw.world.gen;

import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.world.Matrix;

/**
 * The size of a final matrix will resemble this.
 * <p>
 * Matrix matrix = new Matrix(baseX * rescale * 2, baseY * rescale * 2);
 */
public class RiverMixCGen {
    private int baseX, baseY, rescale;
    private RandomCtx ctx;

    public RiverMixCGen(int baseX, int baseY, int rescale, RandomCtx ctx) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.rescale = rescale;
        this.ctx = ctx;
    }


    public Matrix make(){
        Matrix matrix = new Matrix(baseX*rescale, baseY*rescale);

        int factor = baseX * baseY * rescale * rescale/250;

        RiverGen gen = new RiverGen(Math.max(0,(int)(factor*0.8)-10), factor+5, 30, 70, MatrixUtl.add(100));
        gen.setCtx(ctx);

        matrix.apply(gen);

        matrix = MatrixUtl.zoom(matrix, 2);
        matrix = MatrixUtl.smooth9(matrix);
        matrix = MatrixUtl.smooth9(matrix);

        return matrix;
    }
}
