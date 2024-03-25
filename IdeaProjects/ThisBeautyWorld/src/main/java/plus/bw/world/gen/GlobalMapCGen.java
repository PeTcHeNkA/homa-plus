package plus.bw.world.gen;

import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.world.Matrix;


public class GlobalMapCGen {
    private int baseX, baseY, rescale;
    private RandomCtx ctx;

    public GlobalMapCGen(int baseX, int baseY, int rescale, RandomCtx ctx) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.rescale = rescale;
        this.ctx = ctx;
    }


    public Matrix make(){
        Matrix matrix = new Matrix(baseX*2, baseY*2);
        matrix.apply(ctx.rand(2));

        Matrix ciMask = new IslandCGen(baseX, baseY, rescale, ctx).make();


        Matrix mask = new Matrix(baseX*2*rescale, baseY*2*rescale);
        mask.apply(ctx.rand(27));

        int[] reUse, reuse2;

        Matrix scaled = MatrixUtl.zoom(matrix, rescale);
        scaled.apply(MatrixUtl.boost(100));
        reUse = scaled.raw();
        scaled = MatrixUtl.smooth9(scaled);
        reuse2 = scaled.raw();
        scaled = MatrixUtl.smooth9(scaled, reUse);
        scaled = MatrixUtl.smooth9(scaled, reuse2);
        scaled = MatrixUtl.smooth9(scaled, reUse);

        MatrixUtl.DEC_FUNC.apply(scaled, ciMask);
        MatrixUtl.ADD_FUNC.apply(scaled, mask);
        //scaled = MatrixUtl.smooth5(scaled);
        //scaled.update(MatrixUtl.boost(0.07f));

        Matrix rivers = new RiverMixCGen(baseX, baseY, rescale, ctx).make();

        MatrixUtl.DEC_FUNC.apply(scaled, rivers);

        return scaled;
    }
}
