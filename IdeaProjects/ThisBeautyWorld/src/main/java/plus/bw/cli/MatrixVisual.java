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
        if (y == 0 || y >= width - 1 || x == 0 || x >= height - 1) return Integer.MIN_VALUE;
        int delta = (width * x) + y;
        int center = raw[delta];

        int temp;

        if ((temp = raw[delta + width]) < center) center = temp;
        if ((temp = raw[delta - width]) < center) center = temp;
        if ((temp = raw[delta + 1]) < center) center = temp;
        if ((temp = raw[delta - 1]) < center) center = temp;

        return center;
    }


    public Node make(String name, Matrix matrix, Int2ObjectFunction<Material> func, Int2FloatFunction hFunc){
        Node node = new Node(name);
        int[] raw = matrix.raw();
        int size = matrix.size();
        int width = matrix.width();
        int height = matrix.height();

        float[] results = new float[matrix.size()];
        for (int i=0;i<results.length;i++){
            results[i] = hFunc.get(raw[i]);
        }

        for (int x = 0, cursor = 0; x < height; x++){
            for (int y = 0; y < width; y++){
                float calculated = results[cursor];

                Geometry geo;
                int min = isAllNear1Height(raw, width, height, x, y);

                if(min == raw[cursor]){
                    geo = newQuad(raw[cursor], func);

                    geo.setLocalTranslation(x, calculated, y);
                } else {
                    if(min != Integer.MIN_VALUE) {
                        float shift = hFunc.get(min);
                        byte mask = 0;
                        if(calculated > results[cursor+width])mask |= 2;
                        if(calculated > results[cursor+1])    mask |= 4;
                        if(calculated > results[cursor-width])mask |= 8;
                        if(calculated > results[cursor-1])    mask |= 1;

                        geo = newBox(raw[cursor], func, calculated - shift, mask);
                        geo.setLocalTranslation(x, (calculated / 2) + (shift / 2), y);

                    } else {
                        geo = newBox(raw[cursor], func, calculated, (byte) 15);
                        geo.setLocalTranslation(x, (calculated / 2), y);
                    }
                }
                node.attachChild(geo);

                ++cursor;
            }
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


    private static final byte[] GEOMETRY_INDICES_DATA = {
            2,  1,  0,  3,  2,  0, // back
            6,  5,  4,  7,  6,  4, // right
            10,  9,  8, 11, 10,  8, // front
            14, 13, 12, 15, 14, 12, // left
            18, 17, 16, 19, 18, 16, // top
    };

    private static final float[] GEOMETRY_NORMALS_DATA = {
            0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1, // back
            1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0, // right
            0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1, // front
            -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, // left
            0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0, // top
    };

    private static final float[] GEOMETRY_TEXTURE_DATA = {
            1, 0, 0, 0, 0, 1, 1, 1, // back
            1, 0, 0, 0, 0, 1, 1, 1, // right
            1, 0, 0, 0, 0, 1, 1, 1, // front
            1, 0, 0, 0, 0, 1, 1, 1, // left
            1, 0, 0, 0, 0, 1, 1, 1, // top
    };

    public static class FBox extends AbstractBox {
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

        //TODO FIXED
        private static final float[] GEOMETRY_TEXTURE_DATA = {
                1, 0, 0, 0, 0, 1, 1, 1, // back
                1, 0, 0, 0, 0, 1, 1, 1, // right
                1, 0, 0, 0, 0, 1, 1, 1, // front
                1, 0, 0, 0, 0, 1, 1, 1, // left
                1, 0, 0, 0, 0, 1, 1, 1, // top
        };

        //TODO ERROR
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
        //TODO ERROR

        private final byte mask;
        public FBox(float x, float y, float z, byte mask) {
            super();
            this.mask = mask;
//            this.b = b;
//            this.r = r;
//            this.f = f;
//            this.l = l;
            updateGeometry(Vector3f.ZERO, x, y, z);
        }


        protected void doUpdateGeometryIndices() {//stabile
            if (getBuffer(VertexBuffer.Type.Index) == null){
                int size = 6; // GEOMETRY_INDICES_DATA_0.length == 6
                if((mask & 1) == 1)size += 6;
                if((mask & 2) == 2)size += 6;
                if((mask & 4) == 4)size += 6;
                if((mask & 8) == 8)size += 6;

                ByteBuffer buffer = BufferUtils.createByteBuffer(size);
                size = 0;
                if((mask & 1) == 1)buffer.put(GEOMETRY_INDICES_DATA_ALL[size++]);
                if((mask & 2) == 2)buffer.put(GEOMETRY_INDICES_DATA_ALL[size++]);
                if((mask & 4) == 4)buffer.put(GEOMETRY_INDICES_DATA_ALL[size++]);
                if((mask & 8) == 8)buffer.put(GEOMETRY_INDICES_DATA_ALL[size++]);
                buffer.put(GEOMETRY_INDICES_DATA_ALL[size]);

                setBuffer(VertexBuffer.Type.Index, 3, buffer);

                //setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createByteBuffer(GEOMETRY_INDICES_DATA));
            }
        }


        protected void doUpdateGeometryNormals() {
            //stabile
            if (getBuffer(VertexBuffer.Type.Normal) == null){

                int size = 12; // GEOMETRY_NORMALS_DATA == 12
                if((mask & 1) == 1)size += 12;
                if((mask & 2) == 2)size += 12;
                if((mask & 4) == 4)size += 12;
                if((mask & 8) == 8)size += 12;

                FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
                //size = 0;
                if((mask & 1) == 1)buffer.put(GEOMETRY_NORMALS_DATA_B);
                if((mask & 2) == 2)buffer.put(GEOMETRY_NORMALS_DATA_R);
                if((mask & 4) == 4)buffer.put(GEOMETRY_NORMALS_DATA_F);
                if((mask & 8) == 8)buffer.put(GEOMETRY_NORMALS_DATA_L);
                buffer.put(GEOMETRY_NORMALS_DATA_T);

                setBuffer(VertexBuffer.Type.Normal, 3, buffer);

                //setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(GEOMETRY_NORMALS_DATA));
            }
        }


        protected void doUpdateGeometryTextures() {//stabile
            if (getBuffer(VertexBuffer.Type.TexCoord) == null){

                int size = 8; // GEOMETRY_TEXTURE_DATA_T == 8
                if((mask & 1) == 1)size += 8;
                if((mask & 2) == 2)size += 8;
                if((mask & 4) == 4)size += 8;
                if((mask & 8) == 8)size += 8;

                FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
                //size = 0;
                if((mask & 1) == 1)buffer.put(GEOMETRY_TEXTURE_DATA_B);
                if((mask & 2) == 2)buffer.put(GEOMETRY_TEXTURE_DATA_R);
                if((mask & 4) == 4)buffer.put(GEOMETRY_TEXTURE_DATA_F);
                if((mask & 8) == 8)buffer.put(GEOMETRY_TEXTURE_DATA_L);
                buffer.put(GEOMETRY_TEXTURE_DATA_T);

                setBuffer(VertexBuffer.Type.TexCoord, 2, buffer);
                //setBuffer(VertexBuffer.Type.TexCoord, 2, buf2);
            }
        }
//
//
//        private String toStr(FloatBuffer buf){
//            int size = buf.capacity();
//            StringBuilder builder = new StringBuilder("[");
//            for (int i=0;i<size;i++){
//                builder.append(buf.get(i));
//                builder.append(',');
//            }
//            builder.setCharAt(builder.length()-1, ']');
//            return builder.toString();
//        }


        protected void doUpdateGeometryVertices() {
            int size = 4;
            if((mask & 1) == 1)size += 4;
            if((mask & 2) == 2)size += 4;
            if((mask & 4) == 4)size += 4;
            if((mask & 8) == 8)size += 4;

            FloatBuffer fpb = BufferUtils.createVector3Buffer(size);
            Vector3f[] v = computeVertices();

            if((mask & 1) == 1) {
                float[] back = {
                        v[0].x, v[0].y, v[0].z, v[1].x, v[1].y, v[1].z, v[2].x, v[2].y, v[2].z, v[3].x, v[3].y, v[3].z, // back
                };
                fpb.put(back);
            }

            if((mask & 2) == 2) {
                float[] right = {
                        v[1].x, v[1].y, v[1].z, v[4].x, v[4].y, v[4].z, v[6].x, v[6].y, v[6].z, v[2].x, v[2].y, v[2].z, // right
                };
                fpb.put(right);
            }

            if((mask & 4) == 4) {
                float[] front = {
                        v[4].x, v[4].y, v[4].z, v[5].x, v[5].y, v[5].z, v[7].x, v[7].y, v[7].z, v[6].x, v[6].y, v[6].z, // front
                };
                fpb.put(front);
            }

            if((mask & 8) == 8) {
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
        }
    }


    private Geometry newQuad(int num, Int2ObjectFunction<Material> func){
        Mesh quad = new FQuad(0.5f, 0.5f);
        Geometry geo = new Geometry("Quad", quad);
        geo.setMaterial(func.get(num));
        return geo;
    }


    public static void main(String[] args) {
//        Node node = new Node();
//        node.attachChild(newBox(1, 5, null));
//        node.attachChild(newBox(1, 7, null));
//        //node
//
//        GeometryBatchFactory.optimize(node);


        //System.out.println(aBox);
    }


    private static Geometry newBox(int num, Int2ObjectFunction<Material> func, float height, byte mask){
        Mesh aBox = new FBox(0.5f, height/2, 0.5f, mask);//todo
        Geometry geo = new Geometry("Box", aBox);
        geo.setMaterial(func.get(num));
        return geo;
    }
}




