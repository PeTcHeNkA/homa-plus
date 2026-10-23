package plus.bw.cli;

import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.util.TangentBinormalGenerator;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import plus.bw.utl.MatrixUtl;
import plus.bw.utl.collect.PointMap;
import plus.bw.world.Matrix;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class MapRenderManager {
    public final ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
    private final MatrixVisual visual = new MatrixVisual();
    private int chinkSize = 24;
    private final Node system = new Node();
    private Node root = new Node();
    private Matrix matrix;

    private final PointMap<Node> map = new PointMap<>();
    private final ConcurrentLinkedQueue<Runnable> callbacks = new ConcurrentLinkedQueue<>();
    private Int2ObjectFunction<Material> func; Int2FloatFunction hFunc;

    public MapRenderManager(){
        system.attachChild(root);
    }


    public void setMatrix(Matrix matrix){
        this.matrix = matrix;
        root.removeFromParent();
        root = new Node();
        system.attachChild(root);
        callbacks.clear();
    }


    public void setViFunc(Int2ObjectFunction<Material> func, Int2FloatFunction hFunc){
        this.func = func;
        this.hFunc = hFunc;
    }

//    private Matrix makeChunkMatrix(int dx, int dy, int size){
//        int w, h;
//        Matrix matrix = this.matrix;
//        Matrix chunk = new Matrix(w = chinkSize+2, h = chinkSize+2);
//        int xEnd = Math.min(dx + chinkSize, matrix.width()-1),
//            yEnd = Math.min(dy + chinkSize, matrix.height()-1);
//
//        MatrixUtl.copyOf(matrix, dx, dy, xEnd, yEnd, chunk, 1, 1);
//
//        if(dx > 0){
//            int relX = dx + chinkSize + 2, relX2 = dx - 1;
//            for (int y=0; y<w; y++){
//                chunk.set(relX2, y, matrix.get(relX, dy + y));
//            }
//        }
//    }

    public void show(int x, int y){
        if(x < 0 || y < 0)return;
        int dx = (x * chinkSize)+1, dy = (y * chinkSize)+1;
        if(dx >= matrix.width() || dy >= matrix.height())return;

        service.execute(() -> {
            try {

                Matrix chunk = MatrixUtl.copyOf(matrix, dx-1, dy-1,
                        Math.min(dx + chinkSize+1, matrix.width()-1),
                        Math.min(dy + chinkSize+1, matrix.height()-1)
                );

                chunk.apply(key -> {
                    return Math.round(Math.max(key, -5)*0.1f);
                });
                //chunk.apply(MatrixUtl.boost(0.1f));

                Node chunkNode = visual.make("chunk-x=" + x + ",y=" + y, chunk, func, hFunc);
                TangentBinormalGenerator.generate(chunkNode);

                chunkNode.setLocalTranslation(dx, -5, dy);

                chunkNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

                callbacks.add(() -> root.attachChild(chunkNode));

            } catch (Exception e){
                e.printStackTrace();
            }
            //cenMap.attachChild(chunkNode);
        });
    }


    public void onTick(){
        Runnable callBack = callbacks.poll();
        while (callBack != null){
            callBack.run();
            callBack = callbacks.poll();
        }
    }


    public Node root(){
        return root;
    }


    public Node system(){
        return system;
    }
}





