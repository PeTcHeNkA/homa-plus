package plus.sol.core.script;

import plus.sol.api.IEnvironment;
import plus.sol.api.IScript;
import plus.sol.api.IVarProxy;
import plus.sol.core.math.Conduction;
import plus.sol.utls.ScriptInitArgs;
import java.util.function.Function;


public class ScriptLogic<U> implements IScript<U> {
    private final Conduction<U> conduction;
    private final IEnvironment<U> env;
    private final IScript<U> normal;
    private final IScript<U> alt;

    public ScriptLogic(Function<String, IVarProxy<U, ?>> builder, ScriptInitArgs<U> args){
        env = args.environment;
        IScript<U> script = (IScript<U>) args.args[args.args.length-1];
        conduction = Conduction.build(builder, remEnd(args.args, 1));
        if(script instanceof ScriptContainer){
            ScriptContainer<U> sc = (ScriptContainer<U>) script;
            if(sc.scripts.length == 1){
                if(script.getID() == -1){
                    normal = sc.scripts[0];
                } else {
                    normal = new ScriptProxy<>(script.getID(), sc.scripts[0]);
                }
                alt = null;
            } else {
                ScriptLogicPart<U> when = null;
                ScriptLogicPart<U> else0 = null;
                for(IScript<U> script1:sc.scripts){
                    if(script1 instanceof ScriptLogicPart){
                        ScriptLogicPart<U> slp = (ScriptLogicPart<U>) script1;
                        if(slp.getMode()){
                            if(when != null)throw new RuntimeException("Label [WHEN] ready exist in logic!");
                            when = slp;
                        } else {
                            if(else0 != null)throw new RuntimeException("Label [ELSE] ready exist in logic!");
                            else0 = slp;
                        }
                    }
                }
                normal = when;
                alt = else0;
            }
        } else {
            normal = script;
            alt = null;
        }
    }


    protected static Object[] remEnd(Object[] array, int count){
        Object[] a2 = new Object[array.length-count];
        System.arraycopy(array, 0, a2, 0, array.length-count);
        return a2;
    }


    @Override
    public boolean run(U user) {
        if(conduction.checkFor(user, env)){
            if(normal != null) normal.run(user);
        } else {
            if(alt != null) alt.run(user);
        }
        return true;
    }
}