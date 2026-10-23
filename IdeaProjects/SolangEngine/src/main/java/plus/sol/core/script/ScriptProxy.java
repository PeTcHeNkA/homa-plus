package plus.sol.core.script;

import plus.sol.api.IScript;


public class ScriptProxy<U> implements IScript<U> {
    private final int ID;
    private final IScript<U> parent;

    public ScriptProxy(int id, IScript<U> parent) {
        ID = id;
        this.parent = parent;
    }


    public IScript<U> getParent(){
        return parent;
    }


    @Override
    public boolean run(U user) {
        return parent.run(user);
    }


    @Override
    public int getID() {
        return ID;
    }
}