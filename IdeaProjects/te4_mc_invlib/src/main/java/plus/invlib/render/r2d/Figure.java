package plus.invlib.render.r2d;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;


public abstract class Figure {
    public abstract void draw();
    public abstract void addPos(float x, float y);
    public abstract Figure copy();

    public static class Base extends Figure{
        private final Point2f[] points;

        public Base(Point2f... points) {
            this.points = points;
        }

        @Override
        public void draw() {
            glBegin(GL_QUADS);
            for(Point2f p:points){
                p.draw();
            }
            glEnd();
        }


        @Override
        public void addPos(float x, float y) {
            for(int i=0;i < points.length;i++){
                points[i] = points[i].add(x,y);
            }
        }


        @Override
        public Figure copy() {
            return new Base(points.clone());
        }
    }


    public static class Quad extends Figure{
        private Point2f A;
        private Point2f B;
        private Point2f C;
        private Point2f D;

        public Quad(Point2f a, Point2f b, Point2f c, Point2f d) {
            A = a;
            B = b;
            C = c;
            D = d;
        }

        @Override
        public void draw() {
            glBegin(GL_QUADS);
            A.draw();
            B.draw();
            C.draw();
            D.draw();
            glEnd();
        }

        @Override
        public void addPos(float x, float y) {
            A = A.add(x,y);
            B = B.add(x,y);
            C = C.add(x,y);
            D = D.add(x,y);
        }

        @Override
        public Figure copy() {
            return new Quad(A, B, C, D);
        }
    }
}