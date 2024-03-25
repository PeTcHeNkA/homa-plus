package plus.bw.cli;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.AbstractBox;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import jme3tools.optimize.GeometryBatchFactory;
import plus.bw.world.Matrix;
import java.nio.FloatBuffer;


public class MatrixVisual {

    public Node make(String name, Matrix matrix, Int2ObjectFunction<Material> func, Int2FloatFunction hFunc){
        Node node = new Node(name);
        int[] raw = matrix.raw();
        int size = matrix.size();
        int width = matrix.width();
        int height = matrix.height();

        for (int cw = 0, cursor = 0; cw < height; cw++){
            for (int ch = 0; ch < width; ch++){
                float calculated = hFunc.get(raw[cursor]);

                Geometry geo;
                int min = isAllNear1Height(raw, width, height, cw, ch);

                if(min == raw[cursor]){
                    geo = newQuad(raw[cursor], func);

                    geo.setLocalTranslation(cw, calculated, ch);
                } else {
                    if(min != Integer.MIN_VALUE) {
                        float shift = hFunc.get(min);
                        geo = newBox(raw[cursor], calculated - shift, func);
                        geo.setLocalTranslation(cw, (calculated / 2) + (shift / 2), ch);
                    } else {
                        geo = newBox(raw[cursor], calculated, func);
                        geo.setLocalTranslation(cw, (calculated / 2), ch);
                    }
                }
                node.attachChild(geo);

                ++cursor;
            }
        }
        GeometryBatchFactory.optimize(node);

        return node;
    }


    private int isAllNear1Height(int[] raw, int w, int h, int x, int y){
        try {


            if (y == 0 || y >= w - 1 || x == 0 || x >= h - 1) return Integer.MIN_VALUE;
            int delta = (w * x) + y;
            int center = raw[delta];

            int temp;

            if ((temp = raw[delta + w]) < center) center = temp;
            if ((temp = raw[delta - w]) < center) center = temp;
            if ((temp = raw[delta + 1]) < center) center = temp;
            if ((temp = raw[delta - 1]) < center) center = temp;

            return center;

        } catch (Exception e){
            System.err.println("NEN_ERR");
            e.printStackTrace();

            return Integer.MIN_VALUE;
        }
        //return raw[delta+w] == center && raw[delta-w] == center && raw[delta+1] == center && raw[delta-1] == center;
    }


    private static class FQuad extends AbstractBox {
        private static final short[] GEOMETRY_INDICES_DATA = {
                2,  1,  0,  3,  2,  0
        };

        private static final float[] GEOMETRY_NORMALS_DATA = {
                0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0
        };

        private static final float[] GEOMETRY_TEXTURE_DATA = {
                1, 0, 0, 0, 0, 1, 1, 1
        };

        public FQuad(float w, float h){
            super();
            updateGeometry(Vector3f.ZERO, w, 0, h);
        }


        @Override
        protected void doUpdateGeometryIndices() {
            if (getBuffer(VertexBuffer.Type.Index) == null){
                setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createShortBuffer(GEOMETRY_INDICES_DATA));
            }
        }

        @Override
        protected void doUpdateGeometryNormals() {
            if (getBuffer(VertexBuffer.Type.Normal) == null){
                setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(GEOMETRY_NORMALS_DATA));
            }
        }

        @Override
        protected void doUpdateGeometryTextures() {
            if (getBuffer(VertexBuffer.Type.TexCoord) == null){
                setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(GEOMETRY_TEXTURE_DATA));
            }
        }

        @Override
        protected void doUpdateGeometryVertices() {
            FloatBuffer fpb = BufferUtils.createVector3Buffer(24/6);
            Vector3f[] v = computeVertices();
            fpb.put(new float[] {
                    v[2].x, v[2].y, v[2].z, v[6].x, v[6].y, v[6].z, v[7].x, v[7].y, v[7].z, v[3].x, v[3].y, v[3].z,
            });
            setBuffer(VertexBuffer.Type.Position, 3, fpb);
            updateBound();
        }
    }


    private Geometry newQuad(int num, Int2ObjectFunction<Material> func){
        Mesh quad = new FQuad(0.5f, 0.5f);
        Geometry geo = new Geometry("Quad", quad);

        geo.setMaterial(func.get(num));

        return geo;
    }


    private Geometry newBox(int num, float height, Int2ObjectFunction<Material> func){
        Box aBox = new Box(0.5f, height/2, 0.5f);
        Geometry geo = new Geometry("Box", aBox);
        geo.setMaterial(func.get(num));
        return geo;
    }
}
