package plus.bw.cli.utl.collision;

import com.jme3.collision.CollisionResults;
import com.jme3.scene.Node;
import plus.bw.utl.collect.FList;


public class NodeCollisionResults extends CollisionResults {
    final FList<NodeCollisionResults> childs = new FList<>();
    NodeCollisionResults parent;
    Node node;

    NodeCollisionResults() {}


    NodeCollisionResults(NodeCollisionResults parent, Node node) {
        this.node = node;
        if (parent != null) (this.parent = parent).childs.safeAdd(this);
    }


    public Node getNode() {
        return node;
    }


    void def(NodeCollisionResults parent, Node node) {
        clear();
        this.node = node;
        childs.clear();
        if (parent != null) (this.parent = parent).childs.safeAdd(this);
    }


    public FList<NodeCollisionResults> getChilds() {
        return childs;
    }


    public NodeCollisionResults getParent() {
        return parent;
    }
}
