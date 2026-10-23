package plus.sol.utls.flux;

import plus.sol.api.IScript;
import plus.sol.utls.FileEnv;


public class EStrFlux<U> extends StrFlux implements FileEnv<U> {
    public EStrFlux(String s) {
        super(s);
    }


    @Override
    public boolean hasScript(int id) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addScript(int id, IScript<U> script) {
        throw  new UnsupportedOperationException();
    }


    @Override
    public IScript<U> getScriptById(int id) {
        throw new UnsupportedOperationException();
    }
}