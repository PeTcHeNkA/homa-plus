package plus.bw.cli.utl;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;


public class NodeUtl {


    public static Vector3f getShift(Node parent, Node someChild){
        Vector3f vec1 = parent.getWorldTranslation();
        Vector3f vec2 = someChild.getWorldTranslation();
        return new Vector3f(vec2.x - vec1.x, vec2.y- vec1.y, vec2.z-vec1.z);
    }



}