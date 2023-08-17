package plus.client.camera.animation;

import plus.utls.MathUtils;


public class Point1f<T> extends ABCPoint<Point1f<T>, T>{
    public final float value;

    public Point1f(float f) {
        this.value = f;
    }

    @Override
    public Point1f<T> range(Point1f<T> p) {
        return newPoint(MathUtils.range(value, p.value));
    }


    @Override
    public Point1f<T> copy() {
        return newPoint(value);
    }


    protected Point1f<T> newPoint(float f){
        return new Point1f<>(f);
    }


    @Override
    public Point1f<T> add(Point1f<T> p) {
        return newPoint(value +p.value);
    }


    @Override
    public Point1f<T> pow(float f) {
        return newPoint(this.value *f);
    }


    @Override
    public void render(T t) {}
}