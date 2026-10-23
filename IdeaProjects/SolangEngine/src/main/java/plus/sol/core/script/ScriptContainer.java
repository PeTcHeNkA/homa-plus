package plus.sol.core.script;

import plus.sol.api.IScript;
import plus.sol.utls.ILinkable;
import java.util.ArrayList;
import java.util.Collection;


public class ScriptContainer<U> implements IScript<U> {
    protected final IScript<U>[] scripts;
    private final int ID;

    public ScriptContainer(IScript<U>[] scripts, int id) {
        this.scripts = scripts;
        ID = id;
    }


    public ScriptContainer(Collection<IScript<U>> scripts, int id) {
        ID = id;
        ArrayList<IScript<U>> finalScripts = new ArrayList<>();

        for (IScript<U> cur:scripts){
            if(finalScripts.isEmpty()){
                finalScripts.add(cur);
                continue;
            }
            IScript<U> prev = finalScripts.get(finalScripts.size()-1);

            if(cur instanceof ILinkable && cur.getClass()==prev.getClass()){
                prev = (IScript<U>) ((ILinkable) prev).link((ILinkable) cur);
                finalScripts.remove(finalScripts.size()-1);
                finalScripts.add(prev);
            } else {
                finalScripts.add(cur);
            }
        }

        IScript<U>[] empty = new IScript[]{};
        this.scripts = finalScripts.toArray(empty);
    }


    @Override
    public boolean run(U user) {
        for(IScript<U> script:scripts){
            if(!script.run(user)){
                return false;
            }
        }
        return true;
    }


    @Override
    public int getID() {
        return ID;
    }
}