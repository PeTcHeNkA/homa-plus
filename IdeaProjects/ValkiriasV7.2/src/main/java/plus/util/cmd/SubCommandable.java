package plus.util.cmd;

import java.util.Collection;
import java.util.Collections;


public interface SubCommandable {
    YandeReCommandExucator[] injectSubCommands();
    default Collection<YandeReCommandExucator> getSubCommands(){
        return Collections.EMPTY_LIST;
    }
}