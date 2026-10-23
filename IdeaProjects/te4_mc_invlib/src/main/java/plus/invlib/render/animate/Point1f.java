package plus.invlib.render.animate;


public class Point1f<T> extends ABCPoint<Point1f<T>, T>{
    public final float value;

    public Point1f(float f) {
        this.value = f;
    }

    @Override
    public Point1f<T> range(Point1f<T> p) {
        return new Point1f<>(range(value, p.value));
    }

    @Override
    public Point1f<T> copy() {
        return new Point1f<>(value);
    }

    @Override
    public Point1f<T> add(Point1f<T> p) {
        return new Point1f<>(value +p.value);
    }

    @Override
    public Point1f<T> pow(float f) {
        return new Point1f<>(this.value *f);
    }

    @Override
    public void render(T t) {}
}