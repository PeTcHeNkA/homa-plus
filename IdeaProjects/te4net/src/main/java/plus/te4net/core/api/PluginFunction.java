package plus.te4net.core.api;


public interface PluginFunction extends PluginBase {
    String name();
    String onCall(IRequestSession request);
}