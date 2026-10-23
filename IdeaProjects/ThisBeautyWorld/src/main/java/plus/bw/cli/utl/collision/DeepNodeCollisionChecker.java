package plus.bw.cli.utl.collision;

import com.jme3.collision.Collidable;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;
import plus.bw.utl.collect.FList;


public class DeepNodeCollisionChecker extends NodeCollisionResults {
    private final FList<NodeCollisionResults> pooledResults = new FList<>();
    private final FList<NodeCollisionResults> pooledLatest  = new FList<>();
    private int cursor = 0;

    public DeepNodeCollisionChecker(){}


    public void clean(){
        for (NodeCollisionResults results:pooledResults) results.node = null;
    }


    public void cleanAll(){
        pooledResults.free();
        pooledLatest.free();
    }


    public DeepNodeCollisionChecker searchCollisions(Node node, Collidable other){
        cursor = 0;
        pooledLatest.clear();
        this.def(null, node);
        fillResults(other, this);
        return this;
    }


    public FList<NodeCollisionResults> getResults(){
        return pooledLatest;
    }


    private void fillResults(Collidable other, NodeCollisionResults results){
        Spatial[] list = ((SafeArrayList<Spatial>) results.node.getChildren()).getArray();
        boolean needAdd = false;
        for (Spatial child : list) {
            if(child instanceof Node){
                ++cursor;
                NodeCollisionResults newResults;
                if(cursor >= this.pooledResults.size()){
                    this.pooledResults.add(newResults = new NodeCollisionResults(results, (Node) child));
                } else {
                    newResults = this.pooledResults.get(cursor);
                    newResults.def(results, (Node) child);
                }
                fillResults(other, newResults);
            } else {
                if(child.collideWith(other, results)>0){
                    needAdd = true;
                }
            }
        }
        if(needAdd) pooledLatest.add(results);
    }
}