import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import plus.invlib.render.animate.Animation;
import plus.invlib.render.r2d.Figure;
import plus.invlib.render.r2d.Point2f;
import plus.render.r2d.Solid2d;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static plus.invlib.render.r2d.GlUtls.noCenter;


public class test {
    public test(){
        Display.setTitle("CAPTION"); //Установка заголовка окна
        try{
            Display.setDisplayMode(new DisplayMode((int) width, (int) height)); //Задаем размеры окна
            Display.create(); //Создаем окно
        }catch(LWJGLException e){
            e.printStackTrace(); //Вывод возникшей ошибки в консоль
            System.exit(0); //Остановка программы
        }
        a.setTarget(point2);
        while(!Display.isCloseRequested()){
            glClear(GL_COLOR_BUFFER_BIT);
            onTick();
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        System.exit(0);
    }
    public static void main(String[] args){
        System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
        try {
            new test();
        }catch (Exception | Error e){
            e.printStackTrace();
        }

    }

    private static float width= 300;
    private static float height = 300;

    private static Figure f = new Figure.Base(
            new Point2f(0,0),
            new Point2f(0,0),
            new Point2f(0,0.5),
            new Point2f((Math.sqrt(3)/2f)*(0.25), 0.25)
    );

    private static Solid2d.APoint point = new Solid2d.APoint(1,1,1,0.9f);
    private static Solid2d.APoint point2 = new Solid2d.APoint(1,1,0.1f,0.1f);

    private static Animation<Solid2d.APoint, Figure> a =
            new Animation<>(1500, point);


    private static void onTick(){
        glPushMatrix();
        noCenter();

        a.get().render(f);
        //point.render(f);

        //f.addPos(0.001f, 0);
        //f.draw();

        glPopMatrix();
    }


    private static float zIndex = 0;
    public static void pos(Number x, Number y){
        glVertex2f(y.floatValue()*2f, x.floatValue()*2f);
    }
}