package plus.ds;

import net.dv8tion.jda.api.hooks.InterfacedEventManager;


public class IrisEventManager extends InterfacedEventManager {
    public final IrisSystem system;

    public IrisEventManager(IrisSystem system) {
        this.system = system;
    }
}