package plus.bw;

import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.utl.collect.PointMap;
import plus.bw.world.Matrix;
import plus.bw.world.gen.GlobalMapCGen;
import plus.bw.world.gen.IslandCGen;
import plus.bw.world.gen.RiverMixCGen;
import plus.tson.TsonFile;
import java.io.File;


public class Main {
    static int baseX = 64, baseY = 64, rescale = 3;

    private static void printL(int n){
        for (int i=0;i<n;i++) System.out.print('-');
        System.out.print('\n');
    }


    private static void test(Matrix matrix, int x, int y){

        int chinkSize = 24;

        if(x < 0 || y < 0)return;
        int dx = x * chinkSize, dy = y * chinkSize;
        if(dx >= matrix.width() || dy >= matrix.height())return;

        int eX = Math.min(dx + chinkSize, matrix.height()-1);
        int eY = Math.min(dy + chinkSize, matrix.width()-1);

        System.out.println("dx="+dx+",dy="+dy+" -> x"+eX+",y"+eY);

        Matrix chunk = MatrixUtl.copyOf(matrix, dx, dy,
                eX,
                eY
        );

        //print(chunk);
        //System.out.println(chunk);
    }


    public static void main(String[] args) {
        RandomCtx ctx = new RandomCtx();
        Matrix orig = new GlobalMapCGen(64, 64, 4, ctx).make();

//        test(orig, 21, 21);
//        test(orig, 20, 20);
//
        //test(orig, 20, 21);
        //test(orig, 21, 20);

        Matrix matrix = new GlobalMapCGen(baseX, baseY, rescale, ctx).make();

        //matrix = MatrixUtl.copyOf(matrix, 0, 25, 25, 50);



        String res = print(matrix);

        //System.out.println(res);
        TsonFile.write(new File("render.txt"), res);
    }


    private static String print(Matrix m){
        printL(131);
        String res = m.toString(" ", MatrixUtl.viFunc(5, 27, 70," ",".", "+", "#"));
        System.out.println(res);
        printL(131);
        return res;
    }

}

