package plus.te4net.core.api;


public interface PluginAPI extends PluginBase, IRequestProcessor {
    String getApiRoute();
    Type type();
    enum Type{
        GET, POST
    }
    interface Get extends PluginAPI{
        @Override
        default Type type() {
            return Type.GET;
        }
    }
    interface Post extends PluginAPI{
        @Override
        default Type type(){
            return Type.POST;
        }
    }
}