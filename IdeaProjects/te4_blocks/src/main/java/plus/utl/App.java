package plus.utl;


public abstract class App<Obj, Val> {
    public final String name;

    public App(String name) {
        this.name = name;
    }

    public abstract void apply(Obj obj, Val val);
}