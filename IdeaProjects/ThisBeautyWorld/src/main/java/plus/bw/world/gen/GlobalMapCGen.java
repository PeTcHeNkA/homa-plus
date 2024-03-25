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

        //System.out.println(matrix.toString(MatrixUtl.viFunc(1, "#"," ")));

        Matrix ciMask = new IslandCGen(baseX, baseY, rescale, ctx).make();
        //TsonFile.write(new File("render2.txt"), print(ciMask));


        Matrix mask = new Matrix(baseX*2*rescale, baseY*2*rescale);
        mask.apply(ctx.rand(27));


        Matrix scaled = MatrixUtl.zoom(matrix, rescale);
        scaled.apply(MatrixUtl.boost(100));
        scaled = MatrixUtl.smooth9(scaled);
        scaled = MatrixUtl.smooth9(scaled);
        scaled = MatrixUtl.smooth9(scaled);
        scaled = MatrixUtl.smooth9(scaled);

        MatrixUtl.DEC_FUNC.apply(scaled, ciMask);

        MatrixUtl.ADD_FUNC.apply(scaled, mask);
        //scaled = MatrixUtl.smooth5(scaled);
        //scaled.update(MatrixUtl.boost(0.07f));

        Matrix rivers = new RiverMixCGen(baseX, baseY, rescale, ctx).make();

        MatrixUtl.DEC_FUNC.apply(scaled, rivers);

        return scaled;
    }
}
