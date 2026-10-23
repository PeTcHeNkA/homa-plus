package plus.sol.api;


public interface IEnvironment<U> {
    boolean hasScript(int id);
    void addScript(int id, IScript<U> script);
    IScript<U> getScriptById(int id);
}
