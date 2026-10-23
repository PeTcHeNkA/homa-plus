package plus.sol.api;

import plus.sol.reader.CodeSector;


public interface IScriptCompiler<U> {
    IScript<U> compile(CodeSector<U> func);
}