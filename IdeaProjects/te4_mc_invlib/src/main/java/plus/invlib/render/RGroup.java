package plus.invlib.render;

public abstract class RGroup extends RenderableBasic implements Renderable{
    public Renderable[] layers;
    public int localXShift;
    public int localYShift;

    public RGroup() {
        localXShift = 0;
        localYShift = 0;
    }


    public void update() {
        for(Renderable r:layers){
            r.update();
        }
    }


    public void onDraw() {
        for(Renderable r:layers){
            r.draw();
        }
    }
}
