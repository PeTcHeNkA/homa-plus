package plus.invlib.render.r2d;

import static org.lwjgl.opengl.GL11.glVertex2f;

public class Point2f {
    public final float X;
    public final float Y;

    public Point2f(Number x, Number y){
        this(x.floatValue(), y.floatValue());
    }

    public Point2f(float x, float y) {
        X = x;
        Y = y;
    }

    public final Point2f add(Point2f f){
        return add(f.X, f.Y);
    }


    public final Point2f add(float x, float y){
        return new Point2f(X+x, Y+y);
    }


    public void draw(){
        glVertex2f(Y, X);
    }
}
