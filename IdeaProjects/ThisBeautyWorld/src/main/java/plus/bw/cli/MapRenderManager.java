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
    private Node root = new Node();
    private Matrix matrix;

    private final PointMap<Node> map = new PointMap<>();
    private final ConcurrentLinkedQueue<Runnable> callbacks = new ConcurrentLinkedQueue<>();
    private Int2ObjectFunction<Material> func; Int2FloatFunction hFunc;

    public void setMatrix(Matrix matrix){
        this.matrix = matrix;
        root = new Node();
        callbacks.clear();
    }


    public void setViFunc(Int2ObjectFunction<Material> func, Int2FloatFunction hFunc){
        this.func = func;
        this.hFunc = hFunc;
    }


    public void show(int x, int y){
        if(x < 0 || y < 0)return;
        int dx = x * chinkSize, dy = y * chinkSize;
        if(dx >= matrix.width() || dy >= matrix.height())return;

        service.execute(() -> {
            try {

                Matrix chunk = MatrixUtl.copyOf(matrix, dx, dy,
                        Math.min(dx + chinkSize, matrix.width()-1),
                        Math.min(dy + chinkSize, matrix.height()-1)
                );

                chunk.apply(key -> {
                    return Math.max(key, -5);
                });
                chunk.apply(MatrixUtl.boost(0.1f));

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
}





