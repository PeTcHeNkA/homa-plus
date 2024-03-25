package plus.bw.world.gen;

import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.world.Matrix;

/**
 * The size of a final matrix will resemble this.
 * <p>
 * Matrix matrix = new Matrix(baseX * rescale * 2, baseY * rescale * 2);
 */
public class IslandCGen {
    private int baseX, baseY, rescale;
    private RandomCtx ctx;

    public IslandCGen(int baseX, int baseY, int rescale, RandomCtx ctx) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.rescale = rescale;
        this.ctx = ctx;
    }


    public Matrix make(){
        Matrix ciMask = new Matrix(baseX, baseY);
        EllipseGen ellipseGen = new EllipseGen(MatrixUtl.EMPTY_FUNC, MatrixUtl.use(75));
        ciMask.apply(ellipseGen);

        ciMask = MatrixUtl.smooth9(ciMask);
        ciMask = MatrixUtl.smooth9(ciMask);

        RiverGen riMask = new RiverGen(0,0, (baseX+baseY)>>1, (baseX+baseY), MatrixUtl.use(250));
        riMask.setCtx(ctx);
        RiverGen.SingleCreator creator = riMask.creator();

        int shiftX = ciMask.width()>>3;
        int shiftY = ciMask.height()>>3;

        for (int i=2;i<5;i++) {
            creator.makeRiverAt(shiftX+i, shiftY+i, ciMask);
            creator.makeRiverAt(shiftX+i, ciMask.height() - shiftY-i, ciMask);
            creator.makeRiverAt(ciMask.width() - shiftX-i, shiftY+i, ciMask);
            creator.makeRiverAt(ciMask.width() - shiftX-i, ciMask.height() - shiftY-i, ciMask);
        }

        ciMask = MatrixUtl.zoom(ciMask, rescale);
        MatrixUtl.applyBounds(ciMask, MatrixUtl.use(150));

        ciMask = MatrixUtl.smooth9(ciMask);
        ciMask = MatrixUtl.smooth9(ciMask);

        MatrixUtl.applyBounds(ciMask, MatrixUtl.use(250));

        ciMask = MatrixUtl.zoom(ciMask, 2);

        ciMask = MatrixUtl.smooth9(ciMask);
        ciMask = MatrixUtl.smooth9(ciMask);
        ciMask = MatrixUtl.smooth9(ciMask);

        return ciMask;
    }
}
