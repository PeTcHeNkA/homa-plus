package plus.invlib.render;

public abstract class UpdatePoint<T extends Renderable> {
    public abstract T update(T renderable, int width, int height);
}
