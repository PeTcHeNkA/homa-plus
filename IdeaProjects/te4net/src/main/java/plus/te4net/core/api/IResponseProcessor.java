package plus.te4net.core.api;

@PublicUsage
public interface IResponseProcessor {
    void handle(IResponse response);
}