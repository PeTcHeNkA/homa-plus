package plus.bw.cli;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.AbstractBox;
import com.jme3.util.BufferUtils;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import jme3tools.optimize.GeometryBatchFactory;
import plus.bw.world.Matrix;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


public class MatrixVisual {
    private int isAllNear1Height(int[] raw, int width, int height, int x, int y) {
        //if (y == 0 || y >= width - 1 || x == 0 || x >= height - 1) return Integer.MIN_VALUE;
        int delta = (width * x) + y;
        int center = raw[delta];

        int temp;

        if ((temp = raw[delta + width]) < center) center = temp;
        if ((temp = raw[delta - width]) < center) center = temp;
        if ((temp = raw[delta + 1]) < center) center = temp;
        if ((temp = raw[delta - 1]) < center) center = temp;

        return center;
    }


    private static Vector3f[] computeVertices() {
        return new Vector3f[] {
                new Vector3f(-0.5f, 0, -0.5f),
                new Vector3f( 0.5f, 0, -0.5f),
                new Vector3f( 0.5f,  0, -0.5f),
                new Vector3f(-0.5f,  0, -0.5f),
                new Vector3f( 0.5f, 0,  0.5f),
                new Vector3f(-0.5f, 0,  0.5f),
                new Vector3f( 0.5f,  0,  0.5f),
                new Vector3f(-0.5f,  0,  0.5f)
        };
    }


    public Node make(String name, Matrix matrix, Int2ObjectFunction<Material> func, Int2FloatFunction hFunc){
        Node node = new Node(name);
        int[] raw = matrix.raw();
        //int size = matrix.size();
        int width = matrix.width(), w2 = width-1;
        int height = matrix.height(), h2 = height-1;

        float[] results = new float[matrix.size()];
        for (int i=0;i<results.length;i++){
            results[i] = hFunc.get(raw[i]);
        }

        Vector3f[] vertexes = computeVertices();

        for (int x = 1, cursor = width+1; x < h2; x++){
            for (int y = 1; y < w2; y++){
                float calculated = results[cursor];

                Geometry geo;
                int min = isAllNear1Height(raw, width, height, x, y);

                if(min == raw[cursor]){
                    geo = newQuad(raw[cursor], func);

                    geo.setLocalTranslation(x-1, calculated, y-1);
                } else {
                    if(min != Integer.MIN_VALUE) {
                        float shift = hFunc.get(min);
                        byte mask = 0;
                        if(calculated > results[cursor+width])mask |= 2;
                        if(calculated > results[cursor+1])    mask |= 4;
                        if(calculated > results[cursor-width])mask |= 8;
                        if(calculated > results[cursor-1])    mask |= 1;

                        geo = newBox(vertexes, raw[cursor], func, calculated - shift, mask);
                        geo.setLocalTranslation(x-1, (calculated / 2) + (shift / 2), y-1);

                    } else {
                        geo = newBox(vertexes, raw[cursor], func, calculated, (byte) 15);
                        geo.setLocalTranslation(x-1, (calculated / 2), y-1);
                    }
                }
                node.attachChild(geo);

                ++cursor;
            }
            cursor += 2;
        }

        GeometryBatchFactory.optimize(node);

        return node;
    }


    private static class FQuad extends AbstractBox {
        private static final byte[] GEOMETRY_INDICES_DATA_TOP  = {2,  1,  0,  3,  2,  0};
        private static final float[] GEOMETRY_NORMALS_DATA_TOP = {0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0};
        private static final float[] GEOMETRY_TEXTURE_DATA_TOP = {1, 0, 0, 0, 0, 1, 1, 1};

        public FQuad(float w, float h){
            super();
            updateGeometry(Vector3f.ZERO, w, 0, h);
        }


        @Override
        protected void doUpdateGeometryIndices() {
            if (getBuffer(VertexBuffer.Type.Index) == null){
                setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createByteBuffer(GEOMETRY_INDICES_DATA_TOP));
            }
        }


        @Override
        protected void doUpdateGeometryNormals() {
            if (getBuffer(VertexBuffer.Type.Normal) == null){
                setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(GEOMETRY_NORMALS_DATA_TOP));
            }
        }


        @Override
        protected void doUpdateGeometryTextures() {
            if (getBuffer(VertexBuffer.Type.TexCoord) == null){
                setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(GEOMETRY_TEXTURE_DATA_TOP));
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

    public static class FBox extends Mesh {
        private static final byte[] GEOMETRY_INDICES_DATA_0 = {
                2,  1,  0,  3,  2,  0, // back
        };
        private static final byte[] GEOMETRY_INDICES_DATA_4 = {
                6,  5,  4,  7,  6,  4, // right
        };
        private static final byte[] GEOMETRY_INDICES_DATA_8 = {
                10,  9,  8, 11, 10,  8, // front
        };
        private static final byte[] GEOMETRY_INDICES_DATA_12 = {
                14, 13, 12, 15, 14, 12, // left
        };
        private static final byte[] GEOMETRY_INDICES_DATA_16 = {
                18, 17, 16, 19, 18, 16, // top
        };
        private static final byte[][] GEOMETRY_INDICES_DATA_ALL = {
                GEOMETRY_INDICES_DATA_0,
                GEOMETRY_INDICES_DATA_4,
                GEOMETRY_INDICES_DATA_8,
                GEOMETRY_INDICES_DATA_12,
                GEOMETRY_INDICES_DATA_16
        };
        //total 20, step = 4

        private static final float[] GEOMETRY_NORMALS_DATA_B = {
                0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1, // back
        };
        private static final float[] GEOMETRY_NORMALS_DATA_R = {
                1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0, // right
        };
        private static final float[] GEOMETRY_NORMALS_DATA_F = {
                0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1, // front
        };
        private static final float[] GEOMETRY_NORMALS_DATA_L = {
                -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, // left
        };
        private static final float[] GEOMETRY_NORMALS_DATA_T = {
                0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0, // top
        };
        private static final float[] GEOMETRY_TEXTURE_DATA_B = {
                1, 0, 0, 0, 0, 1, 1, 1, // back
        };

        private static final float[] GEOMETRY_TEXTURE_DATA_R = {
                1, 0, 0, 0, 0, 1, 1, 1, // right
        };
        private static final float[] GEOMETRY_TEXTURE_DATA_F = {
                1, 0, 0, 0, 0, 1, 1, 1, // front
        };

        private static final float[] GEOMETRY_TEXTURE_DATA_L = {
                1, 0, 0, 0, 0, 1, 1, 1, // left
        };

        private static final float[] GEOMETRY_TEXTURE_DATA_T = {
                1, 0, 0, 0, 0, 1, 1, 1, // top
        };

        public FBox(Vector3f[] temp, float y, byte mask) {
            super();

            temp[0].y = -y;
            temp[1].y = -y;
            temp[2].y =  y;
            temp[3].y =  y;
            temp[4].y = -y;
            temp[5].y = -y;
            temp[6].y =  y;
            temp[7].y =  y;

            doUpdateGeometryVertices(temp, mask);
            setStatic();
        }


        protected void doUpdateGeometryVertices(Vector3f[] v, byte mask) {
            int size = 4;
            if ((mask & 1) == 1) size += 4;
            if ((mask & 2) == 2) size += 4;
            if ((mask & 4) == 4) size += 4;
            if ((mask & 8) == 8) size += 4;
            FloatBuffer fpb = BufferUtils.createVector3Buffer(size);

            if ((mask & 1) == 1) {
                float[] back = {
                        v[0].x, v[0].y, v[0].z, v[1].x, v[1].y, v[1].z, v[2].x, v[2].y, v[2].z, v[3].x, v[3].y, v[3].z, // back
                };
                fpb.put(back);
            }

            if ((mask & 2) == 2) {
                float[] right = {
                        v[1].x, v[1].y, v[1].z, v[4].x, v[4].y, v[4].z, v[6].x, v[6].y, v[6].z, v[2].x, v[2].y, v[2].z, // right
                };
                fpb.put(right);
            }

            if ((mask & 4) == 4) {
                float[] front = {
                        v[4].x, v[4].y, v[4].z, v[5].x, v[5].y, v[5].z, v[7].x, v[7].y, v[7].z, v[6].x, v[6].y, v[6].z, // front
                };
                fpb.put(front);
            }

            if ((mask & 8) == 8) {
                float[] left = {
                        v[5].x, v[5].y, v[5].z, v[0].x, v[0].y, v[0].z, v[3].x, v[3].y, v[3].z, v[7].x, v[7].y, v[7].z, // left
                };
                fpb.put(left);
            }

            float[] top = {
                    v[2].x, v[2].y, v[2].z, v[6].x, v[6].y, v[6].z, v[7].x, v[7].y, v[7].z, v[3].x, v[3].y, v[3].z, // top
            };
            fpb.put(top);

            setBuffer(VertexBuffer.Type.Position, 3, fpb);
            updateBound();

            //doUpdateGeometryNormals()
            //if (getBuffer(VertexBuffer.Type.Normal) == null){
            size = 12;
            if ((mask & 1) == 1) size += 12;
            if ((mask & 2) == 2) size += 12;
            if ((mask & 4) == 4) size += 12;
            if ((mask & 8) == 8) size += 12;

            FloatBuffer flBuf1 = BufferUtils.createFloatBuffer(size);
            if ((mask & 1) == 1) flBuf1.put(GEOMETRY_NORMALS_DATA_B);
            if ((mask & 2) == 2) flBuf1.put(GEOMETRY_NORMALS_DATA_R);
            if ((mask & 4) == 4) flBuf1.put(GEOMETRY_NORMALS_DATA_F);
            if ((mask & 8) == 8) flBuf1.put(GEOMETRY_NORMALS_DATA_L);
            flBuf1.put(GEOMETRY_NORMALS_DATA_T);

            setBuffer(VertexBuffer.Type.Normal, 3, flBuf1);
            //}

            //doUpdateGeometryTextures();
            //if (getBuffer(VertexBuffer.Type.TexCoord) == null){
            size = 8;
            if ((mask & 1) == 1) size += 8;
            if ((mask & 2) == 2) size += 8;
            if ((mask & 4) == 4) size += 8;
            if ((mask & 8) == 8) size += 8;

            FloatBuffer flBuf2 = BufferUtils.createFloatBuffer(size);
            if ((mask & 1) == 1) flBuf2.put(GEOMETRY_TEXTURE_DATA_B);
            if ((mask & 2) == 2) flBuf2.put(GEOMETRY_TEXTURE_DATA_R);
            if ((mask & 4) == 4) flBuf2.put(GEOMETRY_TEXTURE_DATA_F);
            if ((mask & 8) == 8) flBuf2.put(GEOMETRY_TEXTURE_DATA_L);
            flBuf2.put(GEOMETRY_TEXTURE_DATA_T);

            setBuffer(VertexBuffer.Type.TexCoord, 2, flBuf2);
            //}

            //doUpdateGeometryIndices
            //if (getBuffer(VertexBuffer.Type.Index) == null){
            size = 6;
            if ((mask & 1) == 1) size += 6;
            if ((mask & 2) == 2) size += 6;
            if ((mask & 4) == 4) size += 6;
            if ((mask & 8) == 8) size += 6;

            ByteBuffer byteBuf = BufferUtils.createByteBuffer(size);
            size = 0;
            if ((mask & 1) == 1) byteBuf.put(GEOMETRY_INDICES_DATA_ALL[size++]);
            if ((mask & 2) == 2) byteBuf.put(GEOMETRY_INDICES_DATA_ALL[size++]);
            if ((mask & 4) == 4) byteBuf.put(GEOMETRY_INDICES_DATA_ALL[size++]);
            if ((mask & 8) == 8) byteBuf.put(GEOMETRY_INDICES_DATA_ALL[size++]);
            byteBuf.put(GEOMETRY_INDICES_DATA_ALL[size]);

            setBuffer(VertexBuffer.Type.Index, 3, byteBuf);
            //}
        }
    }


    private Geometry newQuad(int num, Int2ObjectFunction<Material> func){
        Mesh quad = new FQuad(0.5f, 0.5f);
        Geometry geo = new Geometry("Quad", quad);
        geo.setMaterial(func.get(num));
        return geo;
    }


    public static void main(String[] args) {
//        Vector3f center = new Vector3f(0,0,0);
//        Vector3f[] axes = {
//                new Vector3f(0.5f, 0, 0),
//                new Vector3f(0, 2, 0),
//                new Vector3f(0, 0, 0.5f),
//        };
//        Vector3f[] vector3fs = new Vector3f[] {
//                center.subtract(axes[0]).subtractLocal(axes[1]).subtractLocal(axes[2]),
//                center.add(axes[0]).subtractLocal(axes[1]).subtractLocal(axes[2]),
//                center.add(axes[0]).addLocal(axes[1]).subtractLocal(axes[2]),
//                center.subtract(axes[0]).addLocal(axes[1]).subtractLocal(axes[2]),
//                center.add(axes[0]).subtractLocal(axes[1]).addLocal(axes[2]),
//                center.subtract(axes[0]).subtractLocal(axes[1]).addLocal(axes[2]),
//                center.add(axes[0]).addLocal(axes[1]).addLocal(axes[2]),
//                center.subtract(axes[0]).addLocal(axes[1]).addLocal(axes[2])
//        };
//        System.out.println(vector3fs);
    }


    private static Geometry newBox(Vector3f[] vector3fs, int num, Int2ObjectFunction<Material> func, float height, byte mask){
        Mesh aBox = new FBox(vector3fs, height/2, mask);
        Geometry geo = new Geometry("FBox", aBox);
        geo.setMaterial(func.get(num));
        return geo;
    }
}