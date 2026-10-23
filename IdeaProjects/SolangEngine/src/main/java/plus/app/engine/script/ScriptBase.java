package plus.app.engine.script;


public abstract class ScriptBase implements IAppScript {
    public final int ID;

    public ScriptBase(int id) {
        ID = id;
    }


    @Override
    public int getID() {
        return ID;
    }
}