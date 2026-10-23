package plus.te4net.core.api;

import plus.te4net.core.node.HEWorker;


public interface TempRequsetProcessor extends PluginRoute{
    boolean onRemove(HEWorker worker);
    /**
     * Mark route as maybe removed if not used
     */
}